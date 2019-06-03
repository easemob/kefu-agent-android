package com.easemob.helpdesk.activity.history;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.ScreeningActivity;
import com.easemob.helpdesk.activity.SearchHistorySessionActivity;
import com.easemob.helpdesk.adapter.HistoryListAdapter;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HistorySessionEntity;
import com.hyphenate.kefusdk.entity.TechChannel;
import com.hyphenate.kefusdk.entity.user.HDVisitorUser;
import com.hyphenate.kefusdk.entity.option.HistorySessionScreenEntity;
import com.hyphenate.kefusdk.manager.session.HistorySessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 历史会话
 */
public class HistorySessionActivity extends BaseActivity {

    private static final String TAG = HistorySessionActivity.class.getSimpleName();

    private static final int MSG_LOAD_MORE_DATA = 0x01;
    private static final int MSG_REFRESH_DATA = 0x02;
    private static final int MSG_AUTHENTICATION = 0x03;
    private static final int PER_PAGE_SESSION_COUNT = 15;
    private static final int REQUEST_CODE_SCREENING = 16;

    private HistorySessionManager historySessionManager;

    private EasyRecyclerView recyclerView;
    private HistoryListAdapter mAdapter;
    private WeakHandler mWeakHandler;
    private TextView tvLabelTotalCount;

    private RelativeLayout search_button;
    private View viewFilter, viewBack;
    private TimeInfo currentTimeInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_history_sessions);
        historySessionManager = new HistorySessionManager();
        /** 默认获取本周的会话 */
        currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
        historySessionManager.setCurrentTimeInfo(currentTimeInfo.getStartTime(), currentTimeInfo.getEndTime());
        initView();
        mWeakHandler = new WeakHandler(this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(mAdapter = new HistoryListAdapter(this));
        mAdapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMoreMethod();
            }
        });

        mAdapter.setNoMore(R.layout.view_nomore);
        mAdapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mAdapter.resumeMore();
            }
        });

        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                if (i < 0 || i > mAdapter.getCount()){
                    return;
                }
                HistorySessionEntity entty = mAdapter.getItem(i);
                String sServiceId = entty.serviceSessionId;
                HDVisitorUser toUser = entty.visitorUser;
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), HistoryChatActivity.class);
                intent.putExtra("user", toUser);
                intent.putExtra("visitorid", sServiceId);
                intent.putExtra("originType", entty.orginType);
                intent.putExtra("techChannelName", entty.techChannelName);
                intent.putExtra("chatGroupId", Long.parseLong(String.valueOf(entty.chatGroupId)));
                startActivity(intent);
            }
        });

        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onFreshData();
            }
        });
        mAdapter.addAll(historySessionManager.getList());

        mAdapter.sort(new Comparator<HistorySessionEntity>() {
            @Override
            public int compare(HistorySessionEntity lhs, HistorySessionEntity rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                }
                long ltime = convertStringTimeToLongTime(lhs.createDatetime);
                long rtime = convertStringTimeToLongTime(rhs.createDatetime);
                if (ltime < rtime) {
                    return 1;
                } else if (ltime > rtime) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        onFreshData();
    }


    public void refreshLabelTotalCount(int count) {
        if (tvLabelTotalCount != null) {
            tvLabelTotalCount.setText("当前筛选结果 " + count + " (共 " + historySessionManager.getTotal_entries() + ")");
        }
    }


    private static class WeakHandler extends android.os.Handler {
        WeakReference<HistorySessionActivity> weakReference;

        public WeakHandler(HistorySessionActivity activity) {
            this.weakReference = new WeakReference<HistorySessionActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HistorySessionActivity activity = weakReference.get();
            if (null != activity) {
                switch (msg.what) {
                    case MSG_LOAD_MORE_DATA:
                        activity.updateView((List<HistorySessionEntity>) msg.obj);
                        break;
                    case MSG_REFRESH_DATA:
                        activity.refreshView((List<HistorySessionEntity>) msg.obj);
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


    private void initView() {
        viewFilter = $(R.id.iv_filter);
        viewBack = $(R.id.iv_back);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recyclerView);
        tvLabelTotalCount = (TextView) findViewById(R.id.tv_label_total_count);
        search_button = $(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(historySessionManager.getList().size() == 0){
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(HistorySessionActivity.this, SearchHistorySessionActivity.class);
                intent.putParcelableArrayListExtra("list", historySessionManager.getList());
                startActivityForResult(intent, -1);
            }
        });
        viewFilter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HistorySessionActivity.this, ScreeningActivity.class);
                intent.putExtra("timeinfo", currentTimeInfo);
                intent.putExtra("showtag", true);
                startActivityForResult(intent, REQUEST_CODE_SCREENING);
            }
        });
        viewBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }



    private void updateView(List<HistorySessionEntity> data) {
        if (data != null) {
            if (data.size() == 0) {
                mAdapter.stopMore();
                return;
            }
            mAdapter.addAll(data);
            refreshLabelTotalCount(mAdapter.getCount());
            if (data.size() < PER_PAGE_SESSION_COUNT) {
                mAdapter.stopMore();
                return;
            }
            mAdapter.pauseMore();
        }
    }

    private void refreshView(List<HistorySessionEntity> data) {
        if (data != null) {
            mAdapter.clear();
            mAdapter.addAll(data);
            refreshLabelTotalCount(mAdapter.getCount());
            if (data.size() < PER_PAGE_SESSION_COUNT) {
                mAdapter.stopMore();
                return;
            }
            mAdapter.pauseMore();
        }
    }


//    public void back(View view) {
//        this.finish();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SCREENING) {
                HistorySessionScreenEntity screenEntity = new HistorySessionScreenEntity();
                currentTimeInfo = (TimeInfo) data.getSerializableExtra("timeinfo");
                if (currentTimeInfo != null) {
                    screenEntity.startTime = currentTimeInfo.getStartTime();
                    screenEntity.endTime = currentTimeInfo.getEndTime();
                }
                if(data.hasExtra("originType")){
                    screenEntity.currentOriginType = data.getStringExtra("originType");
                }
                if(data.hasExtra("techChannel")){
                    screenEntity.currentTechChannel = (TechChannel) data.getSerializableExtra("techChannel");
                }
                if(data.hasExtra("visitorName")){
                    screenEntity.currentVisitorName = data.getStringExtra("visitorName");
                }
                if (data.hasExtra("ids")) {
                    screenEntity.currentTagIds = data.getStringExtra("ids");
                }
                historySessionManager.setScreenOption(screenEntity);
                onFreshData();
            }

        }

    }


    // 2015-05-26 11:30:20
    private long convertStringTimeToLongTime(String stringTime) {
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

    @Override
    protected void onDestroy() {
        if (mWeakHandler != null) {
            mWeakHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    public void onFreshData() {
        historySessionManager.getFirstPageSessionHistory(new HDDataCallBack<List<HistorySessionEntity>>() {
            @Override
            public void onSuccess(List<HistorySessionEntity> value) {
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = value;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    private void loadMoreMethod() {
        historySessionManager.loadMoreData(new HDDataCallBack<List<HistorySessionEntity>>() {
            @Override
            public void onSuccess(List<HistorySessionEntity> value) {
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                message.obj = value;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_LOAD_MORE_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });
    }

}
