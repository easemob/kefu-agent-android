package com.easemob.helpdesk.activity.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.EMValueCallBack;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.TicketAdapter;
import com.hyphenate.kefusdk.gsonmodel.ticket.LeaveMessageResponse;
import com.hyphenate.kefusdk.gsonmodel.ticket.TicketStatusResponse;
import com.easemob.helpdesk.utils.OnFreshCallbackListener;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDBaseUser;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.manager.main.LeaveMessageManager;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * 留言界面
 * Created by liyuzhao on 16/8/17.
 */
public class LeaveMessageFragment extends Fragment implements OnFreshCallbackListener, SwipeRefreshLayout.OnRefreshListener, RecyclerArrayAdapter.OnItemClickListener{

    private static final String TAG = LeaveMessageFragment.class.getSimpleName();

    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;
    private static final int REQUEST_CODE_TICKET_DETAIL = 0x04;

    private List<LeaveMessageResponse.EntitiesBean> ticketList = Collections.synchronizedList(new ArrayList<LeaveMessageResponse.EntitiesBean>());

    @BindView(R.id.recyclerView)
    public EasyRecyclerView recyclerView;
    @BindView(R.id.tv_label_total_count)
    public TextView tvLabelTotalCount;
    @BindView(R.id.title)
    public TextView title;
    @BindView(R.id.rl_back)
    public View itvBack;
    @BindView(R.id.handleAll)
    public TextView handleAll;
    @BindView(R.id.iv_filter)
    public View itvRight;
    @BindView(R.id.left_action)
    public TextView leftAction;
    @BindView(R.id.right_action)
    public TextView rightAction;

    private SimplePickerView simplePickerView;
    private ProgressDialog pd = null;
    private int mCurPageNo;
    private TicketAdapter adapter;
    private WeakHandler mWeakHandler;

    public static OnFreshCallbackListener callback = null;
    private static final int PER_PAGE_WAIT_COUNT = 20;
    private int total_count = 0;
    private boolean isSelectionMode = false;
    private ArrayList<String> assigneeList = new ArrayList<>();
    private List<HDBaseUser> agentUsers = Collections.synchronizedList(new ArrayList<HDBaseUser>());

    private volatile long mProjectId;
    private HDUser loginUser;
    private TicketStatusResponse ticketStatusResponse;
    private boolean customMode = false;
    private int statusIdIndex = -1;
    private int assigned = 0;

    private TimeInfo currentTimeInfo = new TimeInfo();
    private String visitorName = "";
    private int originTypeIndex;
    private int selectedAgentIndex;
    private int selectedStatusIndex;

    private Unbinder unbinder;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = this;
        mWeakHandler = new WeakHandler(this);
        mCurPageNo = 0;
        isSelectionMode = false;
        loginUser = HDClient.getInstance().getCurrentUser();
        String titleText = getActivity().getIntent().getStringExtra("Title");
        if (titleText != null) {
            title.setText(titleText);
        }
        statusIdIndex = getActivity().getIntent().getIntExtra("statusIdIndex", -1);
        assigned = getActivity().getIntent().getIntExtra("assigned", 0);
        customMode = getActivity().getIntent().getBooleanExtra("CustomMode", false);

        if(loginUser != null){
            assigneeList.add("未分配");
            assigneeList.add(loginUser.getNicename());
            agentUsers.add(loginUser);
        }
        getOtherAgents();
        if (customMode) {
            itvRight.setVisibility(View.VISIBLE);
            getTicketsScreeningResult();
        }
        setUpView();
    }

    private void getTicketsScreeningResult() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ticketScreening", MODE_PRIVATE);
        currentTimeInfo.setStartTime(sharedPreferences.getLong("TimeInfoStart", 0));
        currentTimeInfo.setEndTime(sharedPreferences.getLong("TimeInfoEnd", 0));
        selectedAgentIndex = sharedPreferences.getInt("AgentIndex", 1);
        visitorName = sharedPreferences.getString("CreateBy", "");
        selectedStatusIndex = sharedPreferences.getInt("Status", -1);
        originTypeIndex = sharedPreferences.getInt("Channel", -1);
    }

    private void getOtherAgents(){
        LeaveMessageManager.getInstance().getOtherAgents(new HDDataCallBack<List<HDBaseUser>>() {
            @Override
            public void onSuccess(List<HDBaseUser> value) {
                for (HDBaseUser bUser: value) {
                    assigneeList.add(bUser.getNicename());
                    agentUsers.add(bUser);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });


    }

    private String getStatusId() {
        if (ticketStatusResponse == null || ticketStatusResponse.getNumberOfElements() < 3) {
            return "";
        }

        switch (statusIdIndex) {
            case 0:
            case 1:
            case 2:
                return String.valueOf(ticketStatusResponse.getEntities().get(statusIdIndex).getId());
        }
        if (customMode) {
            switch (selectedStatusIndex) {
                case -1:
                case 0: //全部留言
                    break;
                case 1: //处理中
                case 2: //已解决
                case 3: //未处理
                    return String.valueOf(ticketStatusResponse.getEntities().get(selectedStatusIndex - 1).getId());
            }
        }
        return "";
    }

    private void setUpView() {
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(adapter = new TicketAdapter(getActivity()));
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
        adapter.addAll(ticketList);
        adapter.setOnCheckBoxClick(new TicketAdapter.CallBack(){
            @Override
            public void callBack() {
                refreshLabelTotalCount();
            }
        });
        loadFirstStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        ticketList.clear();
        updateSelectionMode();
        refreshLabelTotalCount();
        getProjectIds();
    }

    public void closePickerView(){
        if(simplePickerView != null && simplePickerView.isShowing()){
            simplePickerView.dismiss();
        }
    }

    public void refreshLabelTotalCount() {
        if (tvLabelTotalCount != null) {
            tvLabelTotalCount.setText("已选择 " + adapter.getSelectedCount() + " (已加载 " + adapter.getCheckRecordList().size() + ", 总共"
                    + total_count +")");
            if (adapter.getSelectedCount() > 0) {
                rightAction.setTextColor(getResources().getColor(R.color.ticket_action_btn_enable));
                rightAction.setEnabled(true);
            } else {
                rightAction.setTextColor(getResources().getColor(R.color.ticket_action_btn_disable));
                rightAction.setEnabled(false);
            }
            if (adapter.getSelectedCount() < adapter.getCheckRecordList().size()) {
                handleAll.setText("全选");
            } else {
                handleAll.setText("取消全选");
            }
            if (adapter.getCheckRecordList().size() > 0) {
                leftAction.setVisibility(View.VISIBLE);
            }else {
                leftAction.setVisibility(View.GONE);
            }
        }
    }

    private void loadFirstStatus() {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
    }

    @Override
    public void onRefresh() {
        if(mProjectId > 0){
            loadTheFirstPageData();
        }else{
            getProjectIds();
        }
    }

    @Override
    public void onFresh(EMValueCallBack callback) {
        onRefresh();
    }

    @Override
    public void onItemClick(final int position) {
        if (position < 0 || position >= ticketList.size()){
            return;
        }
        if (ticketStatusResponse == null || ticketStatusResponse.getEntities() == null || ticketStatusResponse.getEntities().isEmpty()){
            loadTicketStatus();
            return;
        }

        LeaveMessageResponse.EntitiesBean bean = ticketList.get(position);
        Intent intent = new Intent(getContext(), TicketDetailActivity.class);
        intent.putExtra("ticket", bean);
        intent.putExtra("projectId", mProjectId);
        intent.putExtra("status", ticketStatusResponse);
        startActivityForResult(intent, REQUEST_CODE_TICKET_DETAIL);
    }


    private static class WeakHandler extends Handler {
        WeakReference<LeaveMessageFragment> weakReference;

        public WeakHandler(LeaveMessageFragment fragment) {
            this.weakReference = new WeakReference<LeaveMessageFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LeaveMessageFragment fragment = weakReference.get();
            if (null != fragment) {
                switch (msg.what) {
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<LeaveMessageResponse.EntitiesBean>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<LeaveMessageResponse.EntitiesBean>) msg.obj);
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
        adapter = null;
    }

    private void updateView(List<LeaveMessageResponse.EntitiesBean> data) {
        if (data != null) {
            if (data.size() == 0) {
                adapter.stopMore();
                return;
            }
            adapter.addAll(data);
            ticketList.addAll(data);
            adapter.notifyDataSetChanged();
            updateListCount();
            refreshLabelTotalCount();
            adapter.pauseMore();
        } else {
            recyclerView.setRefreshing(false);
        }

    }


    private void refreshView(List<LeaveMessageResponse.EntitiesBean> data) {
        if (data != null) {
            adapter.clear();
            ticketList.clear();
            ticketList.addAll(data);
            adapter.addAll(data);
            isSelectionMode = false;
            updateSelectionMode();
            refreshLabelTotalCount();
            if (data.size() < PER_PAGE_WAIT_COUNT) {
                adapter.stopMore();
            }
            updateListCount();
            adapter.pauseMore();
        } else{
            recyclerView.setRefreshing(false);
        }

    }

    private void updateListCount() {
        if (customMode) {
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("screeningCount", MODE_PRIVATE).edit();
            editor.putInt("screeningCount", total_count).apply();
        }
//        if (getActivity() != null) {
//            ((MainActivity) getActivity()).refreshWaitUnreadCount();
//        }
    }

    private synchronized  void getProjectIds(){
        if (loginUser == null){
            return;
        }

        mProjectId = getActivity().getIntent().getLongExtra("projectId", -1);

        if (mProjectId > 0) {
            loadTicketStatus();
            mWeakHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadTheFirstPageData();
                }
            }, 50);
            return;
        }

        LeaveMessageManager.getInstance().getProjectIds(new HDDataCallBack<Long>() {
            @Override
            public void onSuccess(Long value) {
                mProjectId = value;
                if (mProjectId > 0) {
                    loadTheFirstPageData();
                    loadTicketStatus();
                }
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });



    }

    private synchronized void loadTicketStatus(){
        if (loginUser == null || mProjectId == 0){
            return;
        }

        ticketStatusResponse = (TicketStatusResponse) getActivity().getIntent().getSerializableExtra("TicketStatusResponse");

        if (ticketStatusResponse != null) {
            return;
        }

        LeaveMessageManager.getInstance().getTicketStatus(mProjectId, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Gson gson = new Gson();
                ticketStatusResponse = gson.fromJson(value, TicketStatusResponse.class);
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });


    }

    private synchronized void loadTheFirstPageData() {

        if (loginUser == null || mProjectId == 0){
            return;
        }
        String stringBuilder = getRequestString(0);

        LeaveMessageManager.getInstance().getTicketsList(mProjectId, stringBuilder, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Gson gson = new Gson();
                LeaveMessageResponse response = gson.fromJson(value, LeaveMessageResponse.class);
                total_count = response.getTotalElements();
                mCurPageNo = 0;
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
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    private String getAgentIds() {
        if (customMode) {
            if (selectedAgentIndex <= 0 || selectedAgentIndex > agentUsers.size()) {
                assigned = 0;
                return "";
            }
            assigned = 1;
            return agentUsers.get(selectedAgentIndex - 1).getUserId();
        }
        if (assigned == 0) {
            return "0";
        }
        return loginUser.getUserId();
    }

    private void loadMoreData() {
        if (loginUser == null || mProjectId == 0){
            adapter.stopMore();
            return;
        }
        final int nextPage = mCurPageNo + 1;

        String stringBuilder = getRequestString(nextPage);

        LeaveMessageManager.getInstance().getTicketsList(mProjectId, stringBuilder, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                Gson gson = new Gson();
                LeaveMessageResponse response = gson.fromJson(value, LeaveMessageResponse.class);
                total_count = response.getTotalElements();
                mCurPageNo = nextPage;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = response.getEntities();
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

    @NonNull
    private String getRequestString(int p) {
        int page = p;
        String agentIds = getAgentIds();
        String requestString = "tenantId=" + loginUser.getTenantId() + "&userId=" + loginUser.getUserId() + "&userRoles=" + loginUser.getRoles() +
                "&page=" + page + "&size=" + PER_PAGE_WAIT_COUNT + "&ticketId=&sort=createdAt%2Cdesc&assigned=" +
                assigned + "&agentIds=" + agentIds + "&statusId=" + getStatusId() + "&visitorName=" + visitorName;
        if(customMode) {
            switch (originTypeIndex) {
                case -1:
                case 0: //全部渠道
                    requestString += "&originType=";
                    break;
                case 1: //网页
                    requestString += "&originType=webim";
                    break;
                case 2: //App
                    requestString += "&originType=app";
                    break;
                case 3: //微博
                    requestString += "&originType=weibo";

                    break;
            }
            if(currentTimeInfo != null && currentTimeInfo.getStartTime() != 0 && currentTimeInfo.getEndTime() != 0) {
                requestString += "&startTime=" + currentTimeInfo.getStartTime() + "&endTime=" + currentTimeInfo.getEndTime();
            }
        }
        return requestString;
    }

    @OnClick({R.id.rl_back, R.id.iv_filter, R.id.left_action, R.id.right_action, R.id.handleAll})
    public void onItemClick(View view){
        switch (view.getId()) {
            case R.id.rl_back:
                getActivity().finish();
                break;
            case R.id.iv_filter:
                Intent intent = new Intent(getContext(), TicketsScreeningActivity.class);
                intent.putExtra("assigneeList", assigneeList);
                startActivityForResult(intent, 0);
                break;
            case R.id.left_action:
                isSelectionMode = !isSelectionMode;
                updateSelectionMode();
                break;
            case R.id.right_action:
                closePickerView();
                simplePickerView = new SimplePickerView(getContext(), assigneeList);
                simplePickerView.setCancelable(true);
                simplePickerView.show();
                break;
            case R.id.handleAll:
                if (adapter.getSelectedCount() < adapter.getCheckRecordList().size()) {
                    adapter.selectAllItem();
                } else {
                    adapter.unselectAllItem();
                }
                refreshLabelTotalCount();
                break;
        }
    }

    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
         //   if(requestCode == 100) {
                if(data == null) {
                    return;
                }
                currentTimeInfo = (TimeInfo) data.getSerializableExtra("TimeInfo");
                visitorName = data.getStringExtra("CreateBy");
                originTypeIndex = data.getIntExtra("Channel", -1);
                selectedStatusIndex = data.getIntExtra("Status", -1);
                selectedAgentIndex= data.getIntExtra("AgentIndex", -1);

                adapter.clear();
                ticketList.clear();
                isSelectionMode = false;
                updateSelectionMode();
                refreshLabelTotalCount();
                mCurPageNo = 0;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    loadTheFirstPageData();
                    }
                }, 100);
       //     }
        }
    }

    public void simplePickerSelect(int position){
            if(position >=0 && position < assigneeList.size()){
//                tvDist.setText(assigneeList.get(position));
                if (position == 0){
                    deleteTicketAssignee();
                }else if (position > 0){
                    if (position > agentUsers.size()){
                        return;
                    }
                    HDBaseUser bUser = agentUsers.get(position - 1);


                    putTicketTask(bUser);
                }
            }
    }

    private void updateSelectionMode(){
        if(isSelectionMode) {
            leftAction.setText("取消");
            itvBack.setVisibility(View.GONE);
            handleAll.setVisibility(View.VISIBLE);
            tvLabelTotalCount.setVisibility(View.VISIBLE);
            rightAction.setVisibility(View.VISIBLE);
        } else {
            leftAction.setText("选择");
            itvBack.setVisibility(View.VISIBLE);
            handleAll.setVisibility(View.GONE);
            tvLabelTotalCount.setVisibility(View.GONE);
            rightAction.setVisibility(View.GONE);
        }
        adapter.setIsSelectionMode(isSelectionMode);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    private void closeDialog(){
        if (pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }

    private void putTicketTask(HDBaseUser baseUser){

        if (pd == null){
            pd = new ProgressDialog(getContext());
            pd.setCanceledOnTouchOutside(false);
        }

        pd.setMessage("请求中...");
        pd.show();

        LeaveMessageManager.getInstance().batAssignTicketAssignee(baseUser, mProjectId, getSelectedListIds(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        ticketList.clear();
                        isSelectionMode = false;
                        updateSelectionMode();
                        refreshLabelTotalCount();
                        mCurPageNo = 0;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                loadTheFirstPageData();
                            }
                        }, 100);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getContext(), "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getContext(), "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }

    private void deleteTicketAssignee(){

        if (loginUser == null){
            return;
        }

        if (pd == null){
            pd = new ProgressDialog(getContext());
            pd.setCanceledOnTouchOutside(false);
        }

        pd.setMessage("请求中...");
        pd.show();

        LeaveMessageManager.getInstance().batDeleteTicketAssignee(mProjectId, getSelectedListIds(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        ticketList.clear();
                        isSelectionMode = false;
                        updateSelectionMode();
                        refreshLabelTotalCount();
                        mCurPageNo = 0;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                loadTheFirstPageData();
                            }
                        }, 100);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getContext(), "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getContext(), "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    @NonNull
    private List getSelectedListIds() {
        List list = adapter.getCheckRecordList();
        List requestList = new ArrayList();

        for (int i = 0; i < list.size(); i++) {
            if((boolean)list.get(i)) {
                requestList.add(adapter.getItem(i).getId());
            }
        }
        return requestList;
    }
}
