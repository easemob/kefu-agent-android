package com.easemob.helpdesk.activity.manager;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.Toast;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.CategoryShowActivity;
import com.easemob.helpdesk.activity.transfer.TransferActivity;
import com.easemob.helpdesk.adapter.manager.CurrentSessionAdapter;
import com.easemob.helpdesk.utils.AvatarManager;
import com.hyphenate.kefusdk.gsonmodel.manager.CurrentSessionResponse;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.entity.user.HDVisitorUser;
import com.hyphenate.kefusdk.manager.main.UserCustomInfoManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;



/**
 * Created by liyuzhao on 16/6/20.
 */
public class ManagerCurrentSessionFragment extends Fragment implements RecyclerArrayAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ManagerCurrentSessionFragment";

    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;

    @BindView(R.id.recyclerView)
    protected EasyRecyclerView recyclerView;
    private static final int PER_PAGE_COUNT = 15;
    private static final int REQUEST_CODE_SCREENING = 16;
    private int total_count = 0;
    private int mCurPageNo;
    private WeakHandler mWeakHandler;


    @BindView(R.id.iv_avatar)
    protected ImageView ivAvatar;
    @BindView(R.id.iv_status)
    protected ImageView ivStatus;

    private CurrentSessionAdapter adapter;
    private List<CurrentSessionResponse.ItemsBean> customers = Collections.synchronizedList(new ArrayList<CurrentSessionResponse.ItemsBean>());

    private Dialog pd;
//    private String beginDateTime = null;
//    private String endDateTime = null;
    private Unbinder unbinder;
    private HDUser currentUser;
    private TimeInfo currentTimeInfo;
    private String currentVisitorName;
    private String currentAgentUserId;

    public static final int REQUEST_CODE_ALERT_DIALOG_TRANSFER = 0x07;
    public static final int REQUEST_CODE_ALERT_DIALOG_CLOSE = 0x08;
    public static final int REQUEST_CODE_CATEGORY_SHOW = 0x017;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_fragment_currentsession, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currentUser = HDClient.getInstance().getCurrentUser();
        mWeakHandler = new WeakHandler(this);
        mCurPageNo = 1;
//        long currentTime = System.currentTimeMillis();
//        long sixtyDay = 60L * 24L * 3600L * 1000L;
//        long startTime = currentTime - sixtyDay;
//        beginDateTime = DateUtils.getStartDateTimeString(startTime);
//        endDateTime = DateUtils.getEndDateTimeString(currentTime);
//        currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
        initView();
        loadFirstStatus();
        refreshAgentAvatar();
        loadTheFirstPageData();
    }

    private void initView(){

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(adapter = new CurrentSessionAdapter(getActivity()));

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
        if (i < 0 || i > adapter.getCount()){
            return;
        }
        CurrentSessionResponse.ItemsBean entity = adapter.getItem(i);
        String sessionId = entity.getServiceSessionId();
        HDVisitorUser visitorUser = entity.getVisitorUser();
        Intent intent = new Intent();
        intent.setClass(getContext(), ManagerChatActivity.class);
        intent.putExtra("user", visitorUser);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("chatGroupId", Long.parseLong(String.valueOf(entity.getChatGroupId())));
        if (entity.getOriginType() != null && entity.getOriginType().size() > 0){
            intent.putExtra("originType", entity.getOriginType().get(0));
        }
        intent.putExtra("techChannelName", entity.getTechChannelName());
        intent.putExtra("callback",false);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        loadTheFirstPageData();
    }


    private static class WeakHandler extends Handler{
        WeakReference<ManagerCurrentSessionFragment> weakReference;
        public WeakHandler(ManagerCurrentSessionFragment fragment){
            this.weakReference = new WeakReference<ManagerCurrentSessionFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ManagerCurrentSessionFragment fragment = weakReference.get();
            if (null != fragment){
                switch (msg.what){
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<CurrentSessionResponse.ItemsBean>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<CurrentSessionResponse.ItemsBean>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        fragment.recyclerView.setRefreshing(false);
                        HDApplication.getInstance().logout();
                        break;
                }
            }
        }
    }


    @OnClick(R.id.tv_filter)
    public void onClickByFilter(View view){
        Intent intent = new Intent();
        intent.setClass(getContext(), CurrentSessionFilter.class);
        intent.putExtra("timeinfo", currentTimeInfo);
        intent.putExtra("showtag", true);
        intent.putExtra("showtechchannel", true);
        startActivityForResult(intent, REQUEST_CODE_SCREENING);

    }


    private void updateView(List<CurrentSessionResponse.ItemsBean> data){
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


    private void refreshView(List<CurrentSessionResponse.ItemsBean> data){
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
        if (currentUser == null){
            return;
        }
        final int nextPage = mCurPageNo + 1;
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(currentVisitorName)) {
            stringBuilder.append("visitorName=").append(currentVisitorName).append("&");
        }
        if (currentAgentUserId != null){
            stringBuilder.append("agentUserId=").append(currentAgentUserId).append("&");
        }
        stringBuilder.append("page=").append(nextPage).append("&per_page=").append(PER_PAGE_COUNT).append("&state=Processing,Resolved&isAgent=false&");
        stringBuilder.append("categoryId=-1&subCategoryId=-1&sortOrder=desc&");
        if (currentTimeInfo != null) {
            stringBuilder.append("beginDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getStartTime())).append("&");
            stringBuilder.append("endDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getEndTime()));
        }

        HelpDeskManager.getInstance().getManageCurrentSession(currentUser.getTenantId(), stringBuilder.toString(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null){
                    return;
                }
                if (TextUtils.isEmpty(value)){
                    return;
                }
                mCurPageNo = nextPage;

                Gson gson = new Gson();
                CurrentSessionResponse response = gson.fromJson(value, CurrentSessionResponse.class);
                if (response == null){
                    HDLog.e(TAG, "getManageCurrentSession loadmore response is null");
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = response.getItems();
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null){
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null){
                    return;
                }
                HDLog.e(TAG, "getManageCurrentSession-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });


    }



    private void loadFirstStatus(){
        if (currentUser != null){
            refreshOnline(currentUser.getOnLineState());
        }
    }

    public void refreshOnline(String status){
        CommonUtils.setAgentStatusView(ivStatus, status);
    }

    public void refreshAgentAvatar(){
        if (ivAvatar != null) {
            AvatarManager.getInstance().refreshAgentAvatar(getActivity(), ivAvatar);
        }
    }

    private void loadTheFirstPageData(){
        if (currentUser == null){
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();


        stringBuilder.append("page=1&per_page=").append(PER_PAGE_COUNT).append("&state=Processing,Resolved&isAgent=false&");
        stringBuilder.append("categoryId=-1&subCategoryId=-1&sortOrder=desc&");
	    if (currentAgentUserId != null){
		    stringBuilder.append("agentIds=").append(currentAgentUserId).append("&");
	    }

	    if (currentTimeInfo != null) {
            stringBuilder.append("beginDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getStartTime())).append("&");
            stringBuilder.append("endDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getEndTime()));
        }

	    if (!TextUtils.isEmpty(currentVisitorName)) {
		    stringBuilder.append("&customerName=").append(currentVisitorName).append("&");
	    }

        HelpDeskManager.getInstance().getManageCurrentSession(currentUser.getTenantId(), stringBuilder.toString(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                HDLog.d(TAG, "getManageCurrentSession-value:" + value);
                if (getActivity() == null){
                    return;
                }
                if (TextUtils.isEmpty(value)){
                    return;
                }
                Gson gson = new Gson();
                CurrentSessionResponse response = gson.fromJson(value, CurrentSessionResponse.class);
                if (response == null){
                    HDLog.e(TAG, "getManageCurrentSession response is null");
                    return;
                }
                total_count = response.getTotal_entries();
                mCurPageNo = 1;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = response.getItems();
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.d(TAG, "getManageCurrentSession-value:" + error + errorMsg);
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
                HDLog.e(TAG, "getManageCurrentSession-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == REQUEST_CODE_SCREENING){
                currentTimeInfo = (TimeInfo) data.getSerializableExtra("timeinfo");
                if(data.hasExtra("visitorName")){
                    currentVisitorName = data.getStringExtra("visitorName");
                }else{
                    currentVisitorName = "";
                }
                if (data.hasExtra("agentUserId")){
                    currentAgentUserId = data.getStringExtra("agentUserId");
                }else{
                    currentAgentUserId = null;
                }
                loadTheFirstPageData();
            } else if(requestCode == REQUEST_CODE_ALERT_DIALOG_TRANSFER){
		        transferActivityResult(data);
	        }else if(requestCode == REQUEST_CODE_CATEGORY_SHOW){
		        waitCloseActivityResult(data);
            }
        }


    }

    private void closeDialog(){
        if (pd != null && pd.isShowing()){
            pd.dismiss();
        }
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
        closeDialog();

    }


    public void currentSessionIntent(int position, int index) {
        if (index == 0) {
            startActivityForResult(new Intent(getActivity(), TransferActivity.class).putExtra("position", position).putExtra("manager", true), REQUEST_CODE_ALERT_DIALOG_TRANSFER);
        } else if (index == 1) {
	        if (HDClient.getInstance().isStopSessionNeedSummary) {
		        if (!UserCustomInfoManager.getInstance().getCategoryIsUpdated()) {
			        HDClient.getInstance().chatManager().asyncGetCategoryTree();
		        }
                startActivityForResult(new Intent(getContext(), CategoryShowActivity.class)
                        .putExtra("sessionId", adapter.getItem(position).getServiceSessionId())
                        .putExtra("position", position)
		                .putExtra("manager", true)
                        .putExtra("close", true), REQUEST_CODE_CATEGORY_SHOW);
	        } else {
		        waitCloseActivityResult(new Intent().putExtra("position", position)
				                                    .putExtra("close", true));
	        }
        }
    }

    public void transferActivityResult(Intent data){
        final int position = data.getIntExtra("position", -1);
        final String userId = data.getStringExtra("userId");
        final long queueId = data.getLongExtra("queueId", 0);
        if (position == -1){
            return;
        }
        if (position >= adapter.getCount()){
            return;
        }
        if (pd == null) {
            pd = new ProgressDialog(getActivity());
        }
	    ((ProgressDialog)pd).setMessage("转接中...");
        pd.show();

        if (!TextUtils.isEmpty(userId)){
            HelpDeskManager.getInstance().managerransferSession(currentUser.getTenantId(), adapter.getItem(position).getServiceSessionId(), userId,
		            queueId > 0 ? String.valueOf(queueId) : "", new HDDataCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
	                        adapter.clear();
	                        adapter.notifyDataSetChanged();
	                        new Handler().postDelayed(new Runnable() {
		                        @Override
		                        public void run() {
			                        onRefresh();
		                        }
	                        }, 50);
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
        }else if (queueId > 0){
            HelpDeskManager.getInstance().postManagerTransferSkillGroup(currentUser.getTenantId(), adapter.getItem(position).getServiceSessionId(), queueId,
                    new HDDataCallBack<String>() {
                        @Override
                        public void onSuccess(String value) {
                            if (getActivity() == null) {
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeDialog();
	                                adapter.clear();
	                                adapter.notifyDataSetChanged();
	                                new Handler().postDelayed(new Runnable() {
		                                @Override
		                                public void run() {
			                                onRefresh();
		                                }
	                                }, 50);
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
    }

    public void waitCloseActivityResult(Intent data){
        final int position = data.getIntExtra("position", -1);
        final boolean close = data.getBooleanExtra("close", false);
        if (position == -1) {
            return;
        }
        if (!close) {
            return;
        }
        if(position >= adapter.getCount()){
            return;
        }
        if (pd == null) {
            pd = new ProgressDialog(getActivity());
        }
	    ((ProgressDialog)pd).setMessage("关闭中...");
        pd.show();
        HelpDeskManager.getInstance().postManagerStopSession(currentUser.getTenantId(), adapter.getItem(position).getServiceSessionId(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
	                            adapter.clear();
	                            adapter.notifyDataSetChanged();
	                            new Handler().postDelayed(new Runnable() {
		                            @Override
		                            public void run() {
			                            onRefresh();
		                            }
	                            }, 200);
                            }
                        });
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

}
