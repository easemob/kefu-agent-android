package com.easemob.helpdesk.activity.manager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.manager.VisitorLoadAdapter;
import com.easemob.helpdesk.utils.DateUtils;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.option.VisitorsScreenEntity;
import com.hyphenate.kefusdk.gsonmodel.visitors.VisitorListResponse;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/24.
 */
public class VisitorLoadFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = VisitorLoadFragment.class.getSimpleName();
    private static final int PER_PAGE_COUNT = 999;
    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;

    private WeakHandler mWeakHandler;

    @BindView(R.id.recyclerView)
    protected EasyRecyclerView recyclerView;

    private VisitorLoadAdapter adapter;

    private List<VisitorListResponse.EntitiesBean> customers = Collections.synchronizedList(new ArrayList<VisitorListResponse.EntitiesBean>());

    private VisitorsScreenEntity screenEntity = new VisitorsScreenEntity();

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.manager_fragment_workload_agent, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeakHandler = new WeakHandler(this);
        screenEntity.setCurrentTimeInfo(DateUtils.getTimeInfoByCurrentWeek().getStartTime(), DateUtils.getTimeInfoByCurrentWeek().getEndTime());
        initView();
        loadDataFromRemote();
    }

    private void initView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapterWithProgress(adapter = new VisitorLoadAdapter(getActivity()));

        adapter.setNoMore(R.layout.view_nomore);
        adapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.resumeMore();
            }
        });
        recyclerView.setRefreshListener(this);
        adapter.addAll(customers);
    }

    private static class WeakHandler extends Handler {
        WeakReference<VisitorLoadFragment> weakReference;

        public WeakHandler(VisitorLoadFragment fragment) {
            this.weakReference = new WeakReference<VisitorLoadFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            VisitorLoadFragment fragment = weakReference.get();
            if (null != fragment) {
                switch (msg.what) {
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<VisitorListResponse.EntitiesBean>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<VisitorListResponse.EntitiesBean>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        fragment.recyclerView.setRefreshing(false);
                        HDApplication.getInstance().logout();
                        break;
                }
            }
        }
    }

    private void updateView(List<VisitorListResponse.EntitiesBean> data) {
        if (data != null) {
            if (data.size() == 0) {
                adapter.stopMore();
                return;
            }
            customers.addAll(data);
            adapter.addAll(data);
            adapter.pauseMore();
        } else {
            recyclerView.setRefreshing(false);
        }
    }

    private void refreshView(List<VisitorListResponse.EntitiesBean> data) {
        if (null != data) {
            adapter.clear();
            adapter.addAll(data);
            customers.clear();
            customers.addAll(data);
            if (data.size() < PER_PAGE_COUNT) {
                adapter.stopMore();
            }
            adapter.pauseMore();
        } else {
            recyclerView.setRefreshing(false);
        }
    }

    private void loadDataFromRemote() {
        if (getActivity() == null){
            return;
        }
        HDClient.getInstance().adminCommonManager().getManageVisitorCount(screenEntity, 1, PER_PAGE_COUNT, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                HDLog.d(TAG, "getManageVisitorCount-value:" + value);
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Gson gson = new Gson();
                VisitorListResponse response = gson.fromJson(value, VisitorListResponse.class);
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = response.getEntities();
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                HDLog.e(TAG, "getManageVisitorCount-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    public void setScreenEntity(VisitorsScreenEntity screenEntity) {
        this.screenEntity = screenEntity;
    }

    @Override
    public void onRefresh() {
        loadDataFromRemote();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        if (mWeakHandler != null) {
            mWeakHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
