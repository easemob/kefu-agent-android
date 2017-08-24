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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.EMValueCallBack;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.TicketAdapter;
import com.hyphenate.kefusdk.entity.LeaveMessageConfigEntity;
import com.hyphenate.kefusdk.gsonmodel.ticket.LeaveMessageResponse;
import com.easemob.helpdesk.utils.OnFreshCallbackListener;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
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
    private int total_count = 0;
    private boolean isSelectionMode = false;
    private ArrayList<String> assigneeList = new ArrayList<>();
    private List<HDBaseUser> agentUsers = Collections.synchronizedList(new ArrayList<HDBaseUser>());

    private HDUser loginUser;
    private Unbinder unbinder;
    private Handler handler = new Handler();

    private LeaveMessageConfigEntity configEntity = new LeaveMessageConfigEntity();

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
        configEntity.statusIdIndex = getActivity().getIntent().getIntExtra("statusIdIndex", -1);
        configEntity.customMode = getActivity().getIntent().getBooleanExtra("CustomMode", false);

        assigneeList.add("未分配");

        getOtherAgents();
        if (configEntity.customMode) {
            itvRight.setVisibility(View.VISIBLE);
            getTicketsScreeningResult();
        } else {
            itvRight.setVisibility(View.GONE);
        }
        setUpView();
    }

    private void getTicketsScreeningResult() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ticketScreening", MODE_PRIVATE);
        configEntity.currentTimeInfo.setStartTime(sharedPreferences.getLong("TimeInfoStart", 0));
        configEntity.currentTimeInfo.setEndTime(sharedPreferences.getLong("TimeInfoEnd", 0));
        configEntity.selectedAgentIndex = sharedPreferences.getInt("AgentIndex", 1);
        configEntity.visitorName = sharedPreferences.getString("CreateBy", "");
        configEntity.selectedStatusIndex = sharedPreferences.getInt("Status", -1);
        configEntity.originTypeIndex = sharedPreferences.getInt("Channel", -1);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        ticketList.clear();
        updateSelectionMode();
        refreshLabelTotalCount();
        loadTheFirstPageData();
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

    @Override
    public void onRefresh() {
        loadTheFirstPageData();
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

        LeaveMessageResponse.EntitiesBean bean = ticketList.get(position);
        Intent intent = new Intent(getContext(), TicketDetailActivity.class);
        intent.putExtra("ticket", bean);
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
            if (data.size() < configEntity.PER_PAGE_WAIT_COUNT) {
                adapter.stopMore();
            }
            updateListCount();
            adapter.pauseMore();
        } else{
            recyclerView.setRefreshing(false);
        }

    }

    private void updateListCount() {
        if (configEntity.customMode) {
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("screeningCount", MODE_PRIVATE).edit();
            editor.putInt("screeningCount", total_count).apply();
        }
    }

    private synchronized void loadTheFirstPageData() {

        LeaveMessageManager.getInstance().getTicketsList(0, configEntity, agentUsers, new HDDataCallBack<LeaveMessageResponse>() {
            @Override
            public void onSuccess(LeaveMessageResponse value) {
                if (getActivity() == null) {
                    return;
                }
                total_count = value.getTotalElements();
                mCurPageNo = 0;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = value.getEntities();
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


    private void loadMoreData() {
        final int nextPage = mCurPageNo + 1;

        LeaveMessageManager.getInstance().getTicketsList(nextPage, configEntity, agentUsers, new HDDataCallBack<LeaveMessageResponse>() {
            @Override
            public void onSuccess(LeaveMessageResponse value) {
                if (getActivity() == null) {
                    return;
                }
                total_count = value.getTotalElements();
                mCurPageNo = nextPage;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = value.getEntities();
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
            if(data == null) {
                return;
            }
            configEntity.currentTimeInfo.setStartTime(((TimeInfo) data.getSerializableExtra("TimeInfo")).getStartTime());
            configEntity.currentTimeInfo.setEndTime(((TimeInfo) data.getSerializableExtra("TimeInfo")).getEndTime());
            configEntity.visitorName = data.getStringExtra("CreateBy");
            configEntity.originTypeIndex = data.getIntExtra("Channel", -1);
            configEntity.selectedStatusIndex = data.getIntExtra("Status", -1);
            configEntity.selectedAgentIndex = data.getIntExtra("AgentIndex", -1);

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
        }
    }

    public void simplePickerSelect(int position){
        if(position >=0 && position < assigneeList.size()){
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

        LeaveMessageManager.getInstance().batAssignTicketAssignee(baseUser, getSelectedListIds(), new HDDataCallBack<String>() {
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

        LeaveMessageManager.getInstance().batDeleteTicketAssignee(getSelectedListIds(), new HDDataCallBack<String>() {
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
