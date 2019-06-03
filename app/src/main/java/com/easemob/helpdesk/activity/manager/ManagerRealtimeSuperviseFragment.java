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
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.manager.RealtimeSuperviseAdapter;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.gsonmodel.manager.SuperviseAgentQueues;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by tiancruyff on 2017/12/4.
 */

public class ManagerRealtimeSuperviseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ManagerRealtimeSupervise";

    private static final int MSG_REFRESH = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;

    private static long refreshDelay = 60 * 1000; //60s

    @BindView(R.id.recyclerView) protected EasyRecyclerView recyclerView;
    @BindView(R.id.iv_back) protected ImageView back;
    @BindView(R.id.sort_text) protected TextView tvSortText;
    @BindView(R.id.sort_up_icon) protected ImageView ivSortIconUp;
    @BindView(R.id.sort_down_icon) protected ImageView ivSortIconDown;

    private WeakHandler mWeakHandler;

    private HDUser currentUser;
    private RealtimeSuperviseAdapter adapter;

    private Unbinder unbinder;

    private Comparator<SuperviseAgentQueues.EntitiesBean> sortUp = new Comparator<SuperviseAgentQueues.EntitiesBean>() {
        @Override public int compare(SuperviseAgentQueues.EntitiesBean o1, SuperviseAgentQueues.EntitiesBean o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            if (o1.getSession_wait_count() > o2.getSession_wait_count()) {
                return 1;
            } else if (o1.getSession_wait_count() < o2.getSession_wait_count()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private Comparator<SuperviseAgentQueues.EntitiesBean> sortDown = new Comparator<SuperviseAgentQueues.EntitiesBean>() {
        @Override public int compare(SuperviseAgentQueues.EntitiesBean o1, SuperviseAgentQueues.EntitiesBean o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            if (o1.getSession_wait_count() > o2.getSession_wait_count()) {
                return -1;
            } else if (o1.getSession_wait_count() < o2.getSession_wait_count()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_fragment_realtime_supervise, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currentUser = HDClient.getInstance().getCurrentUser();
        mWeakHandler = new WeakHandler(this);
        initView();

        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ((ManagerHomeActivity) getActivity()).back();
            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        loadTheFirstPageData();
    }

    private void loadTheFirstPageData() {
        HelpDeskManager.getInstance().getMonitorAgentQueues(new HDDataCallBack<String>() {
            @Override public void onSuccess(String value) {
                HDLog.d(TAG, "getMonitorAgentQueues-value:" + value);
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Gson gson = new Gson();
                SuperviseAgentQueues response = gson.fromJson(value, SuperviseAgentQueues.class);
                if (response == null) {
                    HDLog.e(TAG, "getMonitorAgentQueues response is null");
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = response.getEntities();
                mWeakHandler.sendMessage(message);
            }

            @Override public void onError(int error, String errorMsg) {
                HDLog.d(TAG, "getMonitorAgentQueues-value:" + error + errorMsg);
                if (getActivity() == null) {
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                HDLog.e(TAG, "getMonitorAgentQueues-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    private void initView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(adapter = new RealtimeSuperviseAdapter(getActivity()));

        adapter.setNoMore(R.layout.view_nomore);
        adapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                adapter.resumeMore();
            }
        });
        recyclerView.setRefreshListener(this);
    }

    private static class WeakHandler extends Handler {
        WeakReference<ManagerRealtimeSuperviseFragment> weakReference;

        public WeakHandler(ManagerRealtimeSuperviseFragment fragment) {
            this.weakReference = new WeakReference<ManagerRealtimeSuperviseFragment>(fragment);
        }

        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ManagerRealtimeSuperviseFragment fragment = weakReference.get();
            if (null != fragment) {
                switch (msg.what) {
                    case MSG_REFRESH:
                        fragment.onRefresh();
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<SuperviseAgentQueues.EntitiesBean>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        fragment.recyclerView.setRefreshing(false);
                        HDApplication.getInstance().logout();
                        break;
                }
            }
        }
    }

    private void refreshView(List<SuperviseAgentQueues.EntitiesBean> entitiesBeans) {
        if (null != entitiesBeans) {
            adapter.clear();
            adapter.addAll(entitiesBeans);
            adapter.stopMore();
            adapter.pauseMore();
            if (ivSortIconUp != null && ivSortIconUp.getVisibility() == View.VISIBLE) {
                adapter.sort(sortUp);
            } else {
                adapter.sort(sortDown);
            }
        } else {
            if (recyclerView != null) {
                recyclerView.setRefreshing(false);
            }
        }

        if (mWeakHandler.hasMessages(MSG_REFRESH)) {
            mWeakHandler.removeMessages(MSG_REFRESH);
        }

        Message message = mWeakHandler.obtainMessage();
        message.what = MSG_REFRESH;
        mWeakHandler.sendMessageDelayed(message, refreshDelay);
    }

    @OnClick(R.id.sort_layout) public void onClickBySort(View view) {
        if (ivSortIconDown.getVisibility() == View.VISIBLE) {
            ivSortIconUp.setVisibility(View.VISIBLE);
            ivSortIconDown.setVisibility(View.GONE);
            tvSortText.setText("排队人数由低到高");
            adapter.sort(sortUp);
        } else {
            ivSortIconUp.setVisibility(View.GONE);
            ivSortIconDown.setVisibility(View.VISIBLE);
            tvSortText.setText("排队人数由高到低");
            adapter.sort(sortDown);
        }
    }

    @Override public void onRefresh() {
        loadTheFirstPageData();
    }

    @Override public void onDestroyView() {
        unbinder.unbind();
        if (mWeakHandler != null && mWeakHandler.hasMessages(MSG_REFRESH)) {
            mWeakHandler.removeMessages(MSG_REFRESH);
        }
        super.onDestroyView();
    }
}
