package com.easemob.helpdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.history.HistoryChatActivity;
import com.easemob.helpdesk.adapter.HistoryListAdapter;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.bean.HistorySessionEntity;
import com.hyphenate.kefusdk.entity.HDVisitorUser;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by liyuzhao on 16/3/15.
 */
public class SearchHistorySessionActivity extends BaseActivity {


    private static final String TAG = SearchHistorySessionActivity.class.getSimpleName();

    private EasyRecyclerView recyclerView;
    private ImageButton btnClear;
    private EditText query;
    private ArrayList<HistorySessionEntity> mList = new ArrayList<>();
    private HistoryListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_search_history_session);
        initView();
        ArrayList<HistorySessionEntity> list = getIntent().getParcelableArrayListExtra("list");
        if(list != null && list.size() > 0){
            mList.addAll(list);
        }

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(mAdapter = new HistoryListAdapter(this));
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                HistorySessionEntity entty = mAdapter.getItem(i);
                String sServiceId = entty.serviceSessionId;
                HDVisitorUser toUser = entty.visitorUser;
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), HistoryChatActivity.class);
                intent.putExtra("user", toUser);
                intent.putExtra("visitorid", sServiceId);
                intent.putExtra("chatGroupId", Long.parseLong(String.valueOf(entty.chatGroupId)));
                intent.putExtra("originType", entty.orginType);
                intent.putExtra("techChannelName", entty.techChannelName);
                startActivity(intent);
                finish();
            }
        });

        mAdapter.addAll(mList);
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

        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mAdapter.clear();
                mAdapter.addAll(filter(mList, editable.toString()));
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query.getText().clear();
                finish();
            }
        });
    }

    private void initView(){
        recyclerView = $(R.id.recyclerView);
        btnClear = $(R.id.search_clear);
        query = $(R.id.query);
        query.setHint("搜索");
    }

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


    private List<HistorySessionEntity> filter(List<HistorySessionEntity> models, String query){
        query = query.toLowerCase();

        final List<HistorySessionEntity> filteredModelList = new ArrayList<>();
        for (HistorySessionEntity model:models) {
            final String text = model.visitorUser.getNicename().toLowerCase();
            if(text.contains(query)){
                filteredModelList.add(model);
            }else if(model.agentUserNiceName.toLowerCase().contains(query)){
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }




}
