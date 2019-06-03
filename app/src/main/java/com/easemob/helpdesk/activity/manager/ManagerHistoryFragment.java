package com.easemob.helpdesk.activity.manager;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.HistoryListAdapter;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HistorySessionEntity;
import com.hyphenate.kefusdk.entity.TechChannel;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.entity.user.HDVisitorUser;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.JsonUtils;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/21.
 */
public class ManagerHistoryFragment extends Fragment implements RecyclerArrayAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ManagerHistoryFragment";

    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;

    @BindView(R.id.recyclerView) protected EasyRecyclerView recyclerView;
    private static final int PER_PAGE_COUNT = 15;
    private static final int REQUEST_CODE_SCREENING = 16;
    private int total_count = 0;
    private int mCurPageNo;
    private WeakHandler mWeakHandler;

    @BindView(R.id.iv_back) protected ImageView back;

    private HistoryListAdapter adapter;
    private List<HistorySessionEntity> customers = Collections.synchronizedList(new ArrayList<HistorySessionEntity>());

    private Dialog pd;
    //    private String beginDateTime = null;
    //    private String endDateTime = null;

    @BindView(R.id.tv_filter) protected View tvFilter;

    private TextView tvLabelTotalCount;
    private TimeInfo currentTimeInfo;
    private String currentOriginType;
    private String currentVisitorName;
    private TechChannel currentTechChannel;
    private String currentTagIds;
    private String currentEvalString;
    private String currentAgentUserId;
    private Unbinder unbinder;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_fragment_history, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeakHandler = new WeakHandler(this);
        mCurPageNo = 1;
        //        long currentTime = System.currentTimeMillis();
        //        long sixtyDay = 60L * 24L * 3600L * 1000L;
        //        long startTime = currentTime - sixtyDay;
        //        beginDateTime = DateUtils.getStartDateTimeString(startTime);
        //        endDateTime = DateUtils.getEndDateTimeString(currentTime);
        initView();
        currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
        loadTheFirstPageData();

        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ((ManagerHomeActivity) getActivity()).back();
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

        recyclerView.setAdapterWithProgress(adapter = new HistoryListAdapter(getActivity()));

        adapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
            @Override public void onLoadMore() {
                loadMoreData();
            }
        });
        adapter.setNoMore(R.layout.view_nomore);
        adapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                adapter.resumeMore();
            }
        });

        adapter.setOnItemClickListener(this);
        recyclerView.setRefreshListener(this);
        adapter.addAll(customers);
        adapter.sort(new Comparator<HistorySessionEntity>() {
            @Override public int compare(HistorySessionEntity lhs, HistorySessionEntity rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                }
                long ltime = convertStringTimeToMillonSecond(lhs.createDatetime);
                long rtime = convertStringTimeToMillonSecond(rhs.createDatetime);
                if (ltime > rtime) {
                    return 1;
                } else if (ltime > rtime) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    // 2015-05-26 11:30:20
    private long convertStringTimeToMillonSecond(String stringTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date parseTime = dateFormat.parse(stringTime);
            return parseTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            HDLog.e(TAG, e.getMessage());
        }
        return 0;
    }

    @OnClick(R.id.tv_filter) public void onClickByFilter(View view) {
        Intent intent = new Intent();
        intent.setClass(getContext(), HistoryFilter.class);
        intent.putExtra("timeinfo", currentTimeInfo);
        intent.putExtra("showtag", true);
        intent.putExtra("showtechchannel", true);
        startActivityForResult(intent, REQUEST_CODE_SCREENING);
    }

    @Override public void onItemClick(int i) {
        if (i < 0 || i > adapter.getCount()) {
            return;
        }
        HistorySessionEntity entity = adapter.getItem(i);
        String sessionId = entity.serviceSessionId;
        HDVisitorUser toUser = entity.visitorUser;
        Intent intent = new Intent();
        intent.setClass(getContext(), ManagerChatActivity.class);
        intent.putExtra("user", toUser);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("originType", entity.orginType);
        intent.putExtra("techChannelName", entity.techChannelName);
        intent.putExtra("chatGroupId", Long.parseLong(String.valueOf(entity.chatGroupId)));
        intent.putExtra("callback", true);
        startActivity(intent);
    }

    @Override public void onRefresh() {
        loadTheFirstPageData();
    }

    private static class WeakHandler extends Handler {
        WeakReference<ManagerHistoryFragment> weakReference;

        public WeakHandler(ManagerHistoryFragment fragment) {
            this.weakReference = new WeakReference<ManagerHistoryFragment>(fragment);
        }

        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ManagerHistoryFragment fragment = weakReference.get();
            if (null != fragment) {
                switch (msg.what) {
                    case MSG_LOAD_MORE_DATA:
                        fragment.updateView((List<HistorySessionEntity>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        fragment.refreshView((List<HistorySessionEntity>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        fragment.recyclerView.setRefreshing(false);
                        HDApplication.getInstance().logout();
                        break;
                }
            }
        }
    }

    private void updateView(List<HistorySessionEntity> data) {
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

    private void refreshView(List<HistorySessionEntity> data) {
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

    private void loadMoreData() {
        final int nextPage = mCurPageNo + 1;
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(currentVisitorName)) {
            stringBuilder.append("visitorName=").append(currentVisitorName).append("&");
        }
        if (!TextUtils.isEmpty(currentOriginType)) {
            stringBuilder.append("originType=").append(currentOriginType).append("&");
        }
        if (!TextUtils.isEmpty(currentTagIds)) {
            stringBuilder.append("summaryIds=").append(currentTagIds).append("&");
        }
        if (!TextUtils.isEmpty(currentEvalString)) {
            stringBuilder.append("enquirySummary=").append(currentEvalString).append("&");
        }
        if (currentAgentUserId != null) {
            stringBuilder.append("agentUserId=").append(currentAgentUserId).append("&");
        }
        if (currentTechChannel != null) {
            stringBuilder.append("techChannelId=").append(currentTechChannel.techChannelId).append("&");
            stringBuilder.append("techChannelType=").append(currentTechChannel.techChannelType).append("&");
        }
        stringBuilder.append("state=Terminal&isAgent=false&");
        stringBuilder.append("page=").append(nextPage).append("&per_page=").append(PER_PAGE_COUNT).append("&");
        if (currentTimeInfo != null) {
            stringBuilder.append("beginDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getStartTime())).append("&");
            stringBuilder.append("endDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getEndTime()));
        }

        HelpDeskManager.getInstance().getManageHistorySession(stringBuilder.toString(), new HDDataCallBack<String>() {
            @Override public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Pair<Integer, List<HistorySessionEntity>> dataPair = JsonUtils.getHistorySessionsFromJson(value);
                total_count = dataPair.first;
                mCurPageNo = nextPage;

                List<HistorySessionEntity> sessionEntities = dataPair.second;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = sessionEntities;
                mWeakHandler.sendMessage(message);
            }

            @Override public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override public void onAuthenticationException() {
                if (getActivity() == null) {
                    return;
                }
                HDLog.e(TAG, "getManageHistorySession-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    //http://kefu.easemob.com/v1/Tenant/me/ServiceSessionHistorys?page=1&per_page=8&state=Terminal&isAgent=false&originType=&techChannelId=
    // &techChannelType=&visitorName=&agentUserId=0c408b72-e7c1-4337-9b69-12c7b20d85ef&orderType=&summaryIds=&enquirySummary=&sortOrder=desc
    // &beginDate=2016-06-01T00%3A00%3A00.000Z&endDate=2016-06-30T23%3A59%3A00.000Z&_=1467859646847
    private void loadTheFirstPageData() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(currentVisitorName)) {
            stringBuilder.append("visitorName=").append(currentVisitorName).append("&");
        }
        if (!TextUtils.isEmpty(currentOriginType)) {
            stringBuilder.append("originType=").append(currentOriginType).append("&");
        }
        if (!TextUtils.isEmpty(currentTagIds)) {
            stringBuilder.append("summaryIds=").append(currentTagIds).append("&");
        }
        if (currentTechChannel != null) {
            stringBuilder.append("techChannelId=").append(currentTechChannel.techChannelId).append("&");
            stringBuilder.append("techChannelType=").append(currentTechChannel.techChannelType).append("&");
        }
        if (currentAgentUserId != null) {
            stringBuilder.append("agentUserId=").append(currentAgentUserId).append("&");
        }

        if (!TextUtils.isEmpty(currentEvalString)) {
            stringBuilder.append("enquirySummary=").append(currentEvalString).append("&");
        }
        stringBuilder.append("state=Terminal&isAgent=false&");
        stringBuilder.append("page=").append(1).append("&per_page=").append(PER_PAGE_COUNT).append("&");
        if (currentTimeInfo != null) {
            stringBuilder.append("beginDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getStartTime())).append("&");
            stringBuilder.append("endDate=").append(DateUtils.getDateTimeString(currentTimeInfo.getEndTime()));
        }

        HelpDeskManager.getInstance().getManageHistorySession(stringBuilder.toString(), new HDDataCallBack<String>() {
            @Override public void onSuccess(String value) {
                HDLog.d(TAG, "getManageHistorySession-value:" + value);
                if (getActivity() == null) {
                    return;
                }
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Pair<Integer, List<HistorySessionEntity>> dataPair = JsonUtils.getHistorySessionsFromJson(value);
                total_count = dataPair.first;
                mCurPageNo = 1;
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = dataPair.second;
                mWeakHandler.sendMessage(message);
            }

            @Override public void onError(int error, String errorMsg) {
                HDLog.d(TAG, "getManageHistorySession-value:" + error + errorMsg);
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
                HDLog.e(TAG, "getManageHistorySession-onAuthenticationException");
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SCREENING) {
                currentTimeInfo = (TimeInfo) data.getSerializableExtra("timeinfo");
                if (data.hasExtra("originType")) {
                    currentOriginType = data.getStringExtra("originType");
                } else {
                    currentOriginType = "";
                }
                if (data.hasExtra("techChannel")) {
                    currentTechChannel = (TechChannel) data.getSerializableExtra("techChannel");
                } else {
                    currentTechChannel = null;
                }
                if (data.hasExtra("visitorName")) {
                    currentVisitorName = data.getStringExtra("visitorName");
                } else {
                    currentVisitorName = "";
                }
                if (data.hasExtra("ids")) {
                    currentTagIds = data.getStringExtra("ids");
                    currentTagIds = currentTagIds.replace("[", "").replace("]", "");
                } else {
                    currentTagIds = null;
                }
                if (data.hasExtra("agentUserId")) {
                    currentAgentUserId = data.getStringExtra("agentUserId");
                } else {
                    currentAgentUserId = null;
                }

                if (data.hasExtra("eval")) {
                    currentEvalString = data.getStringExtra("eval");
                } else {
                    currentEvalString = "";
                }

                loadTheFirstPageData();
            }
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private void closeDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    @Override public void onDestroy() {
        if (mWeakHandler != null) {
            mWeakHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
        closeDialog();
    }
}
