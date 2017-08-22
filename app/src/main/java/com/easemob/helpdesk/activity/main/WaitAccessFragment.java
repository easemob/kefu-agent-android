package com.easemob.helpdesk.activity.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.EMValueCallBack;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.ScreeningActivity;
import com.easemob.helpdesk.activity.SearchWaitAccessActivity;
import com.easemob.helpdesk.activity.transfer.TransferActivity;
import com.easemob.helpdesk.adapter.WaitAccessAdapter;
import com.easemob.helpdesk.utils.AvatarManager;
import com.hyphenate.kefusdk.gsonmodel.main.SkillGroupResponse;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.OnFreshCallbackListener;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.TechChannel;
import com.hyphenate.kefusdk.gsonmodel.main.WaitQueueResponse;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.manager.main.WaitAccessManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 待接入页面
 * Created by lyuzhao on 2015/12/14.
 */
public class WaitAccessFragment extends Fragment implements OnFreshCallbackListener, SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = WaitAccessFragment.class.getSimpleName();

    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;
    public static final int REQUEST_CODE_ALERT_DIALOG_ACCESS = 0x04;
    private static final int REQUEST_CODE_SCREENING = 0x05;
    private static final int REQUEST_CODE_SEARCH = 0x06;
    public static final int REQUEST_CODE_ALERT_DIALOG_TRANSFER = 0x07;
    public static final int REQUEST_CODE_ALERT_DIALOG_CLOSE = 0x08;

    private List<WaitQueueResponse.ItemsBean> waitUsersList = Collections.synchronizedList(new ArrayList<WaitQueueResponse.ItemsBean>());
    @BindView(R.id.recyclerView)
    public EasyRecyclerView recyclerView;
    private ProgressDialog pd = null;
    private WaitAccessAdapter adapter;
    private WeakHandler mWeakHandler;

    public static OnFreshCallbackListener callback = null;
    private static final int PER_PAGE_WAIT_COUNT = 20;
    @BindView(R.id.iv_avatar)
    public ImageView ivAvatar;
    @BindView(R.id.iv_status)
    public ImageView ivStatus;
    @BindView(R.id.iv_filter)
    public View ibtnFilter;
    @BindView(R.id.tv_label_total_count)
    public TextView tvLabelTotalCount;

    private TimeInfo currentTimeInfo;
    private String currentOriginType;
    private String currentVisitorName;
    private TechChannel currentTechChannel;

    @BindView(R.id.search_layout)
    public RelativeLayout search_layout;
    private Unbinder unbinder;
    private boolean isSearch;
    private List<SkillGroupResponse.EntitiesBean> agentList = Collections.synchronizedList(new ArrayList<SkillGroupResponse.EntitiesBean>());
    private WaitAccessManager waitAccessManager;
    private int mCurPageNo;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wait, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = this;
        mWeakHandler = new WeakHandler(this);
        waitAccessManager = new WaitAccessManager();
        setUpView();
    }

    private void setUpView() {
        ibtnFilter.setOnClickListener(new OnFilterClickListener());
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(adapter = new WaitAccessAdapter(getActivity()));
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

        recyclerView.setRefreshListener(this);
        adapter.addAll(waitUsersList);
        loadTheFirstPageData();
        loadFirstStatus();
        refreshAgentAvatar();

        search_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(waitUsersList == null || waitUsersList.size() == 0){
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(getActivity(), SearchWaitAccessActivity.class);
                intent.putExtra("list", (Serializable) waitUsersList);
                startActivityForResult(intent, REQUEST_CODE_SEARCH);
            }
        });

    }


    public void clickAccess(int position, int index){
        if (index == 1){
            Intent intent = new Intent(getActivity(), AlertDialog.class);
            intent.putExtra("position", position);
            intent.putExtra("msg", "您确认要接待此会话吗？");
            intent.putExtra("okString", "确认");
            startActivityForResult(intent, REQUEST_CODE_ALERT_DIALOG_ACCESS);
        }else if (index == 2){
            startActivityForResult(new Intent(getActivity(), TransferActivity.class).putExtra("position", position), REQUEST_CODE_ALERT_DIALOG_TRANSFER);
        }else if (index == 3){
            //waitAbort
            Intent intent = new Intent(getActivity(), AlertDialog.class);
            intent.putExtra("position", position);
            intent.putExtra("msg", "您确认要关闭此会话吗？");
            intent.putExtra("okString", "确认");
            startActivityForResult(intent, REQUEST_CODE_ALERT_DIALOG_CLOSE);
        }

    }


    public void refreshLabelTotalCount(int count) {
        if (tvLabelTotalCount != null) {
            tvLabelTotalCount.setText("当前筛选结果 " + count + " (共 " + waitAccessManager.getTotal_count() + ")");
        }
    }

    private void loadFirstStatus() {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser != null) {
            refreshOnline(loginUser.getOnLineState());
        }
    }

    public void refreshOnline(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
    }

    public void refreshAgentAvatar() {
        if(ivAvatar != null)
            AvatarManager.getInstance(getContext()).refreshAgentAvatar(getActivity(), ivAvatar);
    }

    @Override
    public void onRefresh() {
        loadTheFirstPageData();
    }

    @Override
    public void onFresh(EMValueCallBack callback) {
        onRefresh();
    }


    public void waitCloseActivityResult(Intent data){
        final int position = data.getIntExtra("position", -1);
        if (position == -1) {
            return;
        }
        if(position >= adapter.getCount()){
            return;
        }
        if (pd == null) {
            pd = new ProgressDialog(getActivity());
        }
        pd.setMessage("关闭中...");
        pd.show();

        final WaitQueueResponse.ItemsBean bean = adapter.getItem(position);

        waitAccessManager.waitAbort(bean, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        adapter.remove(bean);
                        updateListCount();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }

    public void transferActivityResult(Intent data,final int position){
        String userId = data.getStringExtra("userId");
        long queueId = data.getLongExtra("queueId", 0);
        if (position == -1){
            return;
        }
        if (position >= adapter.getCount()){
            return;
        }
        if (pd == null) {
            pd = new ProgressDialog(getActivity());
        }
        pd.setMessage("转接中...");
        pd.show();

        final WaitQueueResponse.ItemsBean bean = adapter.getItem(position);

        waitAccessManager.transferWaitAccess(bean, userId, queueId, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        adapter.remove(bean);
                        updateListCount();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }

    public void closeDialog(){
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    public void waitAccessActivityResult(Intent data){
        final int position = data.getIntExtra("position", -1);
        if (position == -1) {
            return;
        }
        if(position >= adapter.getCount()){
            return;
        }
        if (pd == null) {
            pd = new ProgressDialog(getActivity());
        }
        pd.setMessage("加入中...");
        pd.show();

        final WaitQueueResponse.ItemsBean bean = adapter.getItem(position);

        waitAccessManager.accessWaitUser(bean, new HDDataCallBack<String>() {

            @Override
            public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        adapter.remove(bean);
                        updateListCount();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "errorMsg:" + errorMsg);
                if (getActivity() == null) {
                    return;
                }
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if(requestCode == REQUEST_CODE_SEARCH){
                WaitQueueResponse.ItemsBean entity = (WaitQueueResponse.ItemsBean) data.getSerializableExtra("entty");
                int pos = -1;
                for (int i = 0; i < waitUsersList.size(); i++) {
                    if(waitUsersList.get(i).getUserName().equals(entity.getUserName())){
                        pos = i;
                        break;
                    }
                }
                if (pos == -1) {
                    return;
                }
                if (pos >= adapter.getCount()) {
                    return;
                }

                int index = data.getIntExtra("index", 0);
                final int position = pos;
                if (index == 1){
                    if (pd == null) {
                        pd = new ProgressDialog(getActivity());
                        pd.setMessage("加入中...");
                    }
                    pd.show();

                    final WaitQueueResponse.ItemsBean bean = adapter.getItem(position);

                    waitAccessManager.accessWaitUser(bean, new HDDataCallBack<String>() {

                        @Override
                        public void onSuccess(String value) {
                            if (getActivity() == null) {
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeDialog();
                                    adapter.remove(bean);
                                    updateListCount();
                                }
                            });
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            if (getActivity() == null) {
                                return;
                            }
                            (getActivity()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    closeDialog();
                                    Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onAuthenticationException() {
                            if (getActivity() == null) {
                                return;
                            }
                            (getActivity()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    closeDialog();
                                    HDApplication.getInstance().logout();
                                }
                            });
                        }
                    });

                }else if (index == 2){
                    transferActivityResult(data, position);
                }else if (index == 3){
                    if (pd == null) {
                        pd = new ProgressDialog(getActivity());
                    }
                    pd.setMessage("关闭中...");
                    pd.show();

                    final WaitQueueResponse.ItemsBean bean = adapter.getItem(position);

                    waitAccessManager.waitAbort(bean, new HDDataCallBack<String>() {
                        @Override
                        public void onSuccess(String value) {
                            if (getActivity() == null) {
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeDialog();
                                    adapter.remove(bean);
                                    updateListCount();
                                }
                            });
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            if (getActivity() == null) {
                                return;
                            }
                            (getActivity()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    closeDialog();
                                    Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onAuthenticationException() {
                            if (getActivity() == null) {
                                return;
                            }
                            (getActivity()).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    closeDialog();
                                    HDApplication.getInstance().logout();
                                }
                            });
                        }
                    });

                }


            }else if(requestCode == REQUEST_CODE_ALERT_DIALOG_ACCESS){
                waitAccessActivityResult(data);
            }else if(requestCode == REQUEST_CODE_ALERT_DIALOG_TRANSFER){
                final int position = data.getIntExtra("position", -1);
                transferActivityResult(data, position);
            }else if(requestCode == REQUEST_CODE_ALERT_DIALOG_CLOSE){
                waitCloseActivityResult(data);
            }else if(requestCode == REQUEST_CODE_SCREENING){
                currentTimeInfo = (TimeInfo) data.getSerializableExtra("timeinfo");
                if(data.hasExtra("originType")){
                    currentOriginType = data.getStringExtra("originType");
                }else{
                    currentOriginType = "";
                }
                if(data.hasExtra("techChannel")){
                    currentTechChannel = (TechChannel) data.getSerializableExtra("techChannel");
                }else{
                    currentTechChannel = null;
                }
                if(data.hasExtra("visitorName")){
                    currentVisitorName = data.getStringExtra("visitorName");
                }else{
                    currentVisitorName = "";
                }
                waitAccessManager.setScreeningOption(currentTimeInfo.getStartTime(), currentTimeInfo.getEndTime(), currentOriginType, currentVisitorName, currentTechChannel);
                isSearch = true;
                onFresh(null);
            }

        }

    }

    private static class WeakHandler extends Handler {
        WeakReference<WaitAccessFragment> weakReference;

        public WeakHandler(WaitAccessFragment fragment) {
            this.weakReference = new WeakReference<WaitAccessFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WaitAccessFragment fragment = weakReference.get();
            if (null != fragment) {
                switch (msg.what) {
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<WaitQueueResponse.ItemsBean>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<WaitQueueResponse.ItemsBean>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        HDApplication.getInstance().logout();
                        break;
                    default:
                        break;
                }
            }


        }
    }

    @Override
    public void onDestroy() {
        mWeakHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        callback = null;
    }

    private void updateView(List<WaitQueueResponse.ItemsBean> data) {
        if (data != null) {
            if (data.size() == 0) {
                adapter.stopMore();
                return;
            }
            adapter.addAll(data);
            waitUsersList.addAll(data);
            adapter.notifyDataSetChanged();
            updateListCount();
            refreshLabelTotalCount(adapter.getCount());
            adapter.pauseMore();
        } else {
            recyclerView.setRefreshing(false);
        }

    }

    private void refreshView(List<WaitQueueResponse.ItemsBean> data) {
        if (data != null) {
            adapter.clear();
            waitUsersList.clear();
            waitUsersList.addAll(data);
            adapter.addAll(data);
            adapter.notifyDataSetChanged();
            refreshLabelTotalCount(adapter.getCount());
            if (data.size() < PER_PAGE_WAIT_COUNT) {
                adapter.stopMore();
            }
            updateListCount();
            adapter.pauseMore();
        } else{
            recyclerView.setRefreshing(false);
        }

    }

    private void loadMoreData() {
        waitAccessManager.getUserWaitQueues(isSearch, agentList, mCurPageNo + 1, new HDDataCallBack<List<WaitQueueResponse.ItemsBean>>() {
            @Override
            public void onSuccess(List<WaitQueueResponse.ItemsBean> value) {
                if (getActivity() == null) {
                    return;
                }
                mCurPageNo++;
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

    private void updateListCount() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).refreshWaitUnreadCount();
        }
    }

    private synchronized void loadSkillGroups(){
        waitAccessManager.loadSkillGroup(new HDDataCallBack<List<SkillGroupResponse.EntitiesBean>>() {
            @Override
            public void onSuccess(List<SkillGroupResponse.EntitiesBean> value) {
                agentList.clear();
                agentList.addAll(value);
                loadTheFirstPageData();
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "asyncGetSkillGroups-onError:" + errorMsg);
            }

            @Override
            public void onAuthenticationException() {
            }
        });
    }

    private synchronized void loadTheFirstPageData() {
        if (agentList == null || agentList.isEmpty()){
            loadSkillGroups();
            return;
        }
        waitAccessManager.getUserWaitQueues(isSearch, agentList, 1, new HDDataCallBack<List<WaitQueueResponse.ItemsBean>>() {
            @Override
            public void onSuccess(List<WaitQueueResponse.ItemsBean> value) {
                if (getActivity() == null) {
                    return;
                }
                mCurPageNo = 1;
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

    public int getWaitTotalCount() {
        if (waitAccessManager != null) {
            return waitAccessManager.getTotal_count();
        }
        return 0;
    }

    class OnFilterClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), ScreeningActivity.class);
            intent.putExtra("timeinfo", currentTimeInfo);
            startActivityForResult(intent, REQUEST_CODE_SCREENING);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
        callback = null;
        adapter = null;
    }


}
