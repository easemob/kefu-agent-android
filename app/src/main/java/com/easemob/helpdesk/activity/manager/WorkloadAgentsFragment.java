package com.easemob.helpdesk.activity.manager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.manager.WorkloadAgentAdapter;
import com.easemob.helpdesk.entity.WorkloadAgent;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.JsonUtils;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.option.WorkloadScreenEntity;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by liyuzhao on 16/6/22.
 */
public class WorkloadAgentsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerArrayAdapter.OnItemClickListener {

    private static final String TAG = "WorkloadAgentsFragment";

    private static final int PER_PAGE_COUNT = 15;
    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;
    private int mCurPageNo;
    private WeakHandler mWeakHandler;

    private int totalElements = 0;
    @BindView(R.id.recyclerView)
    protected EasyRecyclerView recyclerView;

    private WorkloadAgentAdapter adapter;

    private List<WorkloadAgent> customers = Collections.synchronizedList(new ArrayList<WorkloadAgent>());

    private WorkloadScreenEntity screenEntity = new WorkloadScreenEntity();

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_fragment_workload_agent, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeakHandler = new WeakHandler(this);
        screenEntity.setCurrentTimeInfo(DateUtils.getTimeInfoByCurrentWeek().getStartTime(), DateUtils.getTimeInfoByCurrentWeek().getEndTime());
        initView();
        loadDataFromRemote();
    }

    private void initView(){

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(adapter = new WorkloadAgentAdapter(getActivity()));

        adapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
        adapter.setNoMore(R.layout.view_nomore);
        adapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.resumeMore();
            }
        });
        adapter.setOnItemClickListener(this);
        recyclerView.setRefreshListener(this);
        adapter.addAll(customers);

    }

    @Override
    public void onItemClick(int i) {
        WorkloadAgent entty = adapter.getItem(i);
        Intent intent = new Intent(getContext(), OverviewActivity.class);
        intent.putExtra("agent", entty);
        intent.putExtra("title", entty.getName());
        intent.putExtra(OverviewActivity.INDEX_INTENT_KEY, OverviewActivity.INDEX_WORK_LOAD_AGENT_DETAIL);
        startActivity(intent);
    }


    private static class WeakHandler extends Handler {
        WeakReference<WorkloadAgentsFragment> weakReference;
        public WeakHandler(WorkloadAgentsFragment fragment){
            this.weakReference = new WeakReference<WorkloadAgentsFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WorkloadAgentsFragment fragment = weakReference.get();
            if (null != fragment){
                switch (msg.what){
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<WorkloadAgent>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<WorkloadAgent>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        fragment.recyclerView.setRefreshing(false);
                        HDApplication.getInstance().logout();
                        break;
                }
            }
        }
    }


    private void updateView(List<WorkloadAgent> data){
        if (data != null){
            if (data.size() == 0){
                adapter.stopMore();
                return;
            }
            customers.addAll(data);
            adapter.addAll(data);
            adapter.pauseMore();
        }else{
            recyclerView.setRefreshing(false);
        }
    }


    private void refreshView(List<WorkloadAgent> data){
        if (null != data){
            adapter.clear();
            adapter.addAll(data);
            customers.clear();
            customers.addAll(data);
            if (data.size() < PER_PAGE_COUNT){
                adapter.stopMore();
            }
            adapter.pauseMore();
        } else {
            recyclerView.setRefreshing(false);
        }
    }


    private void loadMoreData(){
        if (totalElements <= customers.size()){
            adapter.stopMore();
            return;
        }

        final int nextPage = mCurPageNo + 1;
        HDClient.getInstance().adminCommonManager().getStatisticsWorkloadAgent(screenEntity, nextPage, PER_PAGE_COUNT, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                HDLog.d(TAG, "getStatisticsWorkloadAgent-value:" + value);
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Pair<Integer, List<WorkloadAgent>> dataPair = JsonUtils.getWorkloadAgentsFromJson(value);
                totalElements = dataPair.first;
                mCurPageNo = nextPage;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = dataPair.second;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                HDLog.e(TAG, "getStatisticsWorkloadAgent-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });

    }

    private void loadDataFromRemote(){
        if (getActivity() == null){
            return;
        }

        HDClient.getInstance().adminCommonManager().getStatisticsWorkloadAgent(screenEntity, 1, PER_PAGE_COUNT, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                HDLog.d(TAG, "getStatisticsWorkloadAgent-value:" + value);
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Pair<Integer, List<WorkloadAgent>> dataPair = JsonUtils.getWorkloadAgentsFromJson(value);
                totalElements = dataPair.first;
                mCurPageNo = 1;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = dataPair.second;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null){
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null){
                    return;
                }
                HDLog.e(TAG, "getStatisticsWorkloadAgent-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });


    }


    public void setScreenEntity(WorkloadScreenEntity screenEntity) {
        this.screenEntity = screenEntity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    @Override
    public void onRefresh() {
        loadDataFromRemote();
    }

    @Override
    public void onDestroy() {
        if (mWeakHandler != null) {
            mWeakHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
