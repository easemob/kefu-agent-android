package com.easemob.helpdesk.activity.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.NoticeListAdapter;
import com.hyphenate.kefusdk.gsonmodel.main.NoticesResponse;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.manager.main.NoticeManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/3/14.
 */
public class NoticeListFragment extends Fragment implements RecyclerArrayAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = NoticeListFragment.class.getSimpleName();

    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;
    private static final int REQUEST_CODE_ALERT_DIALOG = 0x04;

    @BindView(R.id.recyclerView)
    protected EasyRecyclerView recyclerView;

    private NoticeListAdapter adapter;
    private static final int PER_PAGE_COUNT = 20;
    private WeakHandler mWeakHandler;
    private final ArrayList<NoticesResponse.EntitiesBean> noticeEntities = new ArrayList<NoticesResponse.EntitiesBean>();

    private boolean isReadyed = false;
    private long lastUpdateTime = 0;
    private boolean isUnreadSettings;
    private String typeSettings;

    @BindView(R.id.tv_label_count)
    protected TextView tvLabelCount;

    @BindView(R.id.tv_mark_all)
    protected TextView tvMarkAll;

    private Dialog pd;

    private NoticeManager noticeManager;

    Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noticelist, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HDLog.d(TAG, "onActivityCreated");
        noticeManager = new NoticeManager();
        Bundle bundle = getArguments();
        if (bundle != null) {
            isUnreadSettings = bundle.getBoolean("isUnreadSettings", false);
            typeSettings = bundle.getString("typeSettings", "all");
        }
        mWeakHandler = new WeakHandler(this);
        initView();
        isReadyed = true;
    }

    private void initView() {
        if (isUnreadSettings) {
            tvMarkAll.setVisibility(View.VISIBLE);
        } else {
            tvMarkAll.setVisibility(View.GONE);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(adapter = new NoticeListAdapter(getActivity()));

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
        synchronized (noticeEntities){
            adapter.addAll(noticeEntities);
        }
        loadTheFirstPageData();
    }


    private static class WeakHandler extends Handler {

        WeakReference<NoticeListFragment> weakReference;

        public WeakHandler(NoticeListFragment fragment) {
            this.weakReference = new WeakReference<NoticeListFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NoticeListFragment fragment = weakReference.get();
            if (null != fragment) {
                switch (msg.what) {
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<NoticesResponse.EntitiesBean>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<NoticesResponse.EntitiesBean>) msg.obj);
                        if(fragment.isUnreadSettings){
                            if(fragment.getActivity() != null){
                                ((NoticeFragment)fragment.getParentFragment()).refreshTabUnreadCount();
                            }
                        }
                        break;
                    case MSG_AUTHENTICATION:
                        HDApplication.getInstance().logout();
                        break;
                }
            }


        }
    }

    private synchronized void updateView(List<NoticesResponse.EntitiesBean> data) {
        if (data != null) {
            if (data.size() == 0) {
                adapter.stopMore();
                return;
            }
            synchronized (noticeEntities){
                noticeEntities.addAll(data);
                adapter.addAll(data);
                adapter.notifyDataSetChanged();
            }

            adapter.pauseMore();
        } else {
            recyclerView.setRefreshing(false);
        }
        refreshShowLabel();
    }

    private synchronized void refreshView(List<NoticesResponse.EntitiesBean> data) {
        HDLog.d(TAG, "refreshView");
        long currentUpdateTime = System.currentTimeMillis();
        if (currentUpdateTime - lastUpdateTime < 1000){
            recyclerView.setRefreshing(false);
            return;
        }
        lastUpdateTime = currentUpdateTime;
        if (data != null) {
            synchronized (noticeEntities){
                noticeEntities.clear();
                noticeEntities.addAll(data);
                adapter.clear();
                adapter.addAll(noticeEntities);
                adapter.notifyDataSetChanged();
                if (data.size() < PER_PAGE_COUNT) {
                    adapter.stopMore();
                }
                adapter.pauseMore();
            }
        }else{
            if(recyclerView != null){
                recyclerView.setRefreshing(false);
            }

        }
        refreshShowLabel();
    }

    private void loadMoreData() {
        noticeManager.loadMoreData(typeSettings, isUnreadSettings, new HDDataCallBack<List<NoticesResponse.EntitiesBean>>() {
            @Override
            public void onSuccess(List<NoticesResponse.EntitiesBean> value) {
                if (getActivity() == null) {
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = value;
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
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }


    void loadTheFirstPageData() {
        noticeManager.loadTheFirstPageData(typeSettings, isUnreadSettings, new HDDataCallBack<List<NoticesResponse.EntitiesBean>>() {
            @Override
            public void onSuccess(List<NoticesResponse.EntitiesBean> value) {
                if (getActivity() == null) {
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = value;
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
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    @Override
    public void onDestroy() {
        mWeakHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        closeDialog();
        if (recyclerView != null){
            recyclerView.clear();
            recyclerView = null;
        }

    }

    private void closeDialog(){
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }

    @Override
    public void onItemClick(final int i) {
        if (i >= noticeEntities.size() || i < 0){
            return;
        }
        NoticesResponse.EntitiesBean noticeEntity = noticeEntities.get(i);
        Intent intent = new Intent();
        intent.setClass(getContext(), NoticeDetailActivity.class);
        intent.putExtra("notice", (Serializable) noticeEntity);
        startActivity(intent);
        if (noticeEntity.getStatus().equals("unread")){
            noticeEntity.setStatus("read");
            markNoticeRead(noticeEntity);
        }

    }

    public void markNoticeRead(final NoticesResponse.EntitiesBean noticeEntity){
        noticeManager.markNoticeRead(noticeEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.remove(noticeEntity);
                        noticeEntities.remove(noticeEntity);
                        refreshShowLabel();
                        ((NoticeFragment)getParentFragment()).diminishingUnreadCount();
                        ((NoticeFragment)getParentFragment()).refreshTabUnreadCount();

                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });

    }




    @OnClick(R.id.tv_mark_all)
    public void makeAllMarkRead(){
        if(!isUnreadSettings){
            return;
        }

        pd = DialogUtils.getLoadingDialog(getActivity(), "请等待...");
        pd.show();

        noticeManager.makeAllMarkRead(noticeEntities, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if(getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        synchronized (noticeEntities){
                            noticeEntities.clear();
                            adapter.clear();
                        }
                        adapter.notifyDataSetChanged();
                        refreshShowLabel();
                        if (getActivity() instanceof MainActivity) {
                            if (isUnreadSettings) {
                                ((NoticeFragment)getParentFragment()).refreshTabUnreadCount();
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if(getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                    }
                });
            }

        });

    }

    public int getCount() {
        return noticeManager.getTotal_count();
    }

    private void refreshShowLabel(){
        int totalCount = getCount();
        int currentCount = noticeEntities.size();
        int unreadCount = noticeManager.getUnread_count();
        if (tvLabelCount != null){
            if (isUnreadSettings) {
                tvLabelCount.setText(String.format("当前展示数%d (未读%d)", currentCount, unreadCount));
            } else {
                tvLabelCount.setText(String.format("当前展示数%d (总共%d)", currentCount, totalCount));

            }
        }
    }


    @Override
    public void onRefresh() {
        if (!isReadyed){
            return;
        }
        loadTheFirstPageData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
        adapter = null;
    }
}
