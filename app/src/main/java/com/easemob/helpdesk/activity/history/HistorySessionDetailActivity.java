package com.easemob.helpdesk.activity.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.hyphenate.kefusdk.bean.HistorySessionEntity;

/**
 * Created by lyuzhao on 2016/1/7.
 */
public class HistorySessionDetailActivity extends BaseActivity {

    private static final String TAG = "HistorySessionDetailActivity";
    private HistorySessionEntity historySessionEntity;
    private TextView tvVisitorName, tvAgentName, tvStartTime, tvCategoryDetail, tvChannelName, tvGuanlian, tvCategoryNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_history_sessiondetail);
        historySessionEntity = (HistorySessionEntity) getIntent().getSerializableExtra("entity");

        initView();
        loadEntityData();
    }

    private void loadEntityData() {
        if(historySessionEntity == null){
            return;
        }
        tvVisitorName.setText(historySessionEntity.visitorUser.getNicename());
        tvAgentName.setText(historySessionEntity.agentUserNiceName);
        tvStartTime.setText(historySessionEntity.startDateTime);
        tvCategoryDetail.setText(historySessionEntity.summarysDetail);
        tvChannelName.setText(historySessionEntity.orginType);
        tvGuanlian.setText(historySessionEntity.techChannelName);
        tvCategoryNote.setText(historySessionEntity.comment);
    }

    private void initView() {
        tvVisitorName = (TextView) findViewById(R.id.right_visitor_name);
        tvAgentName = (TextView) findViewById(R.id.right_agent_name);
        tvStartTime = (TextView) findViewById(R.id.right_start_time);
        tvCategoryDetail = (TextView) findViewById(R.id.right_category_detail);
        tvChannelName = (TextView) findViewById(R.id.right_channel_name);
        tvGuanlian = (TextView) findViewById(R.id.right_guanlian);
        tvCategoryNote = (TextView) findViewById(R.id.tv_category_note);
    }

    public void back(View view){
        finish();
    }


}
