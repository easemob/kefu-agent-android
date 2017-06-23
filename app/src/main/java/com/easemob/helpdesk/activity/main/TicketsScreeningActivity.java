package com.easemob.helpdesk.activity.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.hyphenate.kefusdk.chat.HDClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tiancruyff on 2017/5/19.
 */

public class TicketsScreeningActivity extends BaseActivity
		implements View.OnClickListener , SimplePickerView.SimplePickSelectItemListener{
	private static final String[] dateStrings = {"今天", "昨天", "本周", "本月", "上月", "指定时间"};
	private ArrayList<String> agent = new ArrayList<>();
	private ArrayList<String> status = new ArrayList<>();
	private ArrayList<String> channels = new ArrayList<>();


	private RelativeLayout rlTimeLayout;
	private RelativeLayout rlBeginTimeLayout;
	private RelativeLayout rlEndTimeLayout;
	private RelativeLayout rlAgentLayout;
	private RelativeLayout rlStatusLayout;
	private RelativeLayout rlChannelLayout;

	private View ibtnBack;
	private TextView tvAction;
	private TextView tvTimeText;
	private TextView tvBeginTime;
	private TextView tvEndTime;
	private TextView tvAgent;
	private TextView tvStatus;
	private TextView tvChannel;
	private EditText etCreateBy;

	private Button button;

	private PickCategory currentPickCategory = PickCategory.TIME;
	private PickTime currentPickTime = PickTime.BEGINTIME;

	private SimplePickerView simplePickerView;
	private TimePickerView pvTime;

	private TimeInfo currentTimeInfo;
	private int agentIndex = 1;
	private int statusIndex = 0;
	private int channelIndex = 0;
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_tickets_screening);
		mContext = this;
		initView();
		initListener();
		initData();
	}

	private void initView(){
		ibtnBack = $(R.id.rl_back);
		tvAction = $(R.id.btn_action);

		rlTimeLayout = $(R.id.rl_time);
		tvTimeText = $(R.id.tv_time_text);

		rlBeginTimeLayout = $(R.id.rl_begintime);
		rlEndTimeLayout = $(R.id.rl_endtime);
		tvBeginTime = $(R.id.tv_begin_time);
		tvEndTime = $(R.id.tv_end_time);

		rlAgentLayout = $(R.id.rl_agent);
		tvAgent = $(R.id.tv_agent_text);

		rlStatusLayout = $(R.id.rl_status);
		tvStatus = $(R.id.tv_status_text);

		rlChannelLayout = $(R.id.rl_channel);
		tvChannel = $(R.id.tv_channel_text);

		etCreateBy = $(R.id.et_created_by_text);
		button = $(R.id.btn_reset);

		pvTime = new TimePickerView(this, TimePickerView.Type.ALL);
		pvTime.setCyclic(false);
		pvTime.setCancelable(true);
		//时间选择后回调
		pvTime.setOnTimeSelectListener(new PVTimeSelectListener());
	}

	private void initListener(){
		ibtnBack.setOnClickListener(this);
		tvAction.setOnClickListener(this);
		button.setOnClickListener(this);
		rlTimeLayout.setOnClickListener(new RlClickListener());
		rlBeginTimeLayout.setOnClickListener(new PVTimeOnClickListener());
		rlEndTimeLayout.setOnClickListener(new PVTimeOnClickListener());

		rlAgentLayout.setOnClickListener(new RlClickListener());
		rlStatusLayout.setOnClickListener(new RlClickListener());
		rlChannelLayout.setOnClickListener(new RlClickListener());
	}

	private void initData(){
		loadScreeningValue();

		if (currentTimeInfo != null && currentTimeInfo.getStartTime() != 0 && currentTimeInfo.getEndTime() != 0) {
			timeMatch(currentTimeInfo);
			tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
			tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
		} else {
			tvTimeText.setText("指定时间");
			tvBeginTime.setText("");
			tvEndTime.setText("");
		}

		//agent = new ArrayList(getIntent().getParcelableArrayListExtra("assigneeList"));
		agent.add("未分配");
		agent.add(HDClient.getInstance().getCurrentUser().getNicename());
		if (agentIndex >= agent.size()) {
			agentIndex = 1;
		}
		tvAgent.setText(agent.get(agentIndex));

		status.add("全部留言");
		status.add("处理中");
		status.add("已解决");
		status.add("未处理");
		if (statusIndex >= status.size()) {
			statusIndex = 0;
		}
		tvStatus.setText(status.get(statusIndex));

		channels.add("全部渠道");
		channels.add("网页");
		channels.add("App");
		channels.add("微博");
		if (channelIndex >= channels.size()) {
			channelIndex = 0;
		}
		tvChannel.setText(channels.get(channelIndex));
	}

	class RlClickListener implements View.OnClickListener{

		@Override
		public void onClick(View view) {
			if(simplePickerView != null && simplePickerView.isShowing()){
				simplePickerView.dismiss();
			}
			switch (view.getId()){
				case R.id.rl_time:
					currentPickCategory = PickCategory.TIME;
					simplePickerView = new SimplePickerView(mContext, dateStrings);
					simplePickerView.setCancelable(true);
					simplePickerView.show();
					break;
				case R.id.rl_agent:
					currentPickCategory = PickCategory.AGENT;
					simplePickerView = new SimplePickerView(mContext, agent);
					simplePickerView.setCancelable(true);
					simplePickerView.show();
					break;
				case R.id.rl_status:
					currentPickCategory = PickCategory.STATUS;
					simplePickerView = new SimplePickerView(mContext, status);
					simplePickerView.setCancelable(true);
					simplePickerView.show();
					break;
				case R.id.rl_channel:
					currentPickCategory = PickCategory.CHANNEL;
					simplePickerView = new SimplePickerView(mContext, channels);
					simplePickerView.setCancelable(true);
					simplePickerView.show();
					break;
			}

		}
	}


	class PVTimeSelectListener implements TimePickerView.OnTimeSelectListener{

		@Override
		public void onTimeSelect(Date date) {
			if(currentTimeInfo == null){
				currentTimeInfo = DateUtils.getTodayStartAndEndTime();
			}
			tvTimeText.setText("指定时间");
			if (currentPickTime == PickTime.BEGINTIME) {
				if(currentTimeInfo.getEndTime()<date.getTime()){
					currentTimeInfo.setEndTime(date.getTime());
					tvEndTime.setText(dateFormat.format(date));
				}
				currentTimeInfo.setStartTime(date.getTime());
				tvBeginTime.setText(dateFormat.format(date));
			} else if (currentPickTime == PickTime.ENDTIME) {
				if(date.getTime() < currentTimeInfo.getStartTime()){
					currentTimeInfo.setStartTime(date.getTime());
					tvBeginTime.setText(dateFormat.format(date));
				}
				currentTimeInfo.setEndTime(date.getTime());
				tvEndTime.setText(dateFormat.format(date));
			}

		}
	}


	class PVTimeOnClickListener implements View.OnClickListener{

		@Override
		public void onClick(View view) {
			if(pvTime != null && pvTime.isShowing()){
				pvTime.dismiss();
			}
			switch (view.getId()) {
				case R.id.rl_begintime:
					currentPickTime = PickTime.BEGINTIME;
					if (currentTimeInfo != null) {
						pvTime.setTime(new Date(currentTimeInfo.getStartTime()));
					} else {
						pvTime.setTime(new Date(System.currentTimeMillis()));
					}
					pvTime.show();
					break;
				case R.id.rl_endtime:
					currentPickTime = PickTime.ENDTIME;
					if (currentTimeInfo != null) {
						pvTime.setTime(new Date(currentTimeInfo.getEndTime()));
					} else {
						pvTime.setTime(new Date(System.currentTimeMillis()));
					}
					pvTime.show();
					break;
			}
		}
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
			case R.id.rl_back:
				finish();
				break;
			case R.id.btn_action:
				setTicketsResult();
				saveScreeningValue();
				finish();
				break;
			case R.id.btn_reset:
				currentTimeInfo.setStartTime(0);
				currentTimeInfo.setEndTime(0);
				tvTimeText.setText("指定时间");
				tvBeginTime.setText("");
				tvEndTime.setText("");
				agentIndex = 1;
				tvAgent.setText(agent.get(1));
				etCreateBy.setText("");
				statusIndex = 0;
				tvStatus.setText("全部留言");
				channelIndex = 0;
				tvChannel.setText("全部渠道");
				saveScreeningValue();
				setTicketsResult();
				break;
		}
	}

	private void setTicketsResult() {
		Intent data = new Intent();
		if (currentTimeInfo != null) {
			data.putExtra("TimeInfo", currentTimeInfo);
		}
		data.putExtra("AgentIndex", agentIndex);
		data.putExtra("CreateBy", etCreateBy.getText().toString());
		data.putExtra("Status", statusIndex);
		data.putExtra("Channel", channelIndex);
		setResult(RESULT_OK, data);
	}

	private void loadScreeningValue() {
		SharedPreferences sp = getSharedPreferences("ticketScreening", MODE_PRIVATE);
		if (currentTimeInfo == null) {
			currentTimeInfo = new TimeInfo();
		}
		currentTimeInfo.setStartTime(sp.getLong("TimeInfoStart", 0));
		currentTimeInfo.setEndTime(sp.getLong("TimeInfoEnd", 0));
		agentIndex = sp.getInt("AgentIndex", 1);
		String ct = sp.getString("CreateBy", "");
		etCreateBy.setText(ct);
		if (ct.length() > 0) {
			etCreateBy.setSelection(ct.length());
		}
		statusIndex = sp.getInt("Status", 0);
		channelIndex = sp.getInt("Channel", 0);
	}

	private void saveScreeningValue() {
		SharedPreferences.Editor editor = getSharedPreferences("ticketScreening", MODE_PRIVATE).edit();
		if (currentTimeInfo != null) {
			editor.putLong("TimeInfoStart", currentTimeInfo.getStartTime());
			editor.putLong("TimeInfoEnd", currentTimeInfo.getEndTime());
		}
		editor.putInt("AgentIndex", agentIndex);
		editor.putString("CreateBy", etCreateBy.getText().toString());
		editor.putInt("Status", statusIndex);
		editor.putInt("Channel", channelIndex);
		editor.apply();
	}

	public void simplePickerSelect(int position){
		if(currentPickCategory == PickCategory.TIME){
			tvTimeText.setText(dateStrings[position]);
			//{"今天", "昨天", "本周", "本月", "上月", "指定时间"};
			switch (position) {
				case 0:
					currentTimeInfo = DateUtils.getTodayStartAndEndTime();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
					break;
				case 1:
					currentTimeInfo = DateUtils.getYesterdayStartAndEndTime();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
					break;
				case 2:
					currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
					break;
				case 3:
					currentTimeInfo = DateUtils.getTimeInfoByCurrentMonth();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
					break;
				case 4:
					currentTimeInfo = DateUtils.getTimeInfoByLastMonth();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
					break;
				case 5:
					currentTimeInfo.setStartTime(0);
					currentTimeInfo.setEndTime(0);
//                    rlBeginTimeLayout.setEnabled(true);
//                    rlEndTimeLayout.setEnabled(true);
					break;
			}

			if(currentTimeInfo != null && currentTimeInfo.getStartTime() != 0 && currentTimeInfo.getEndTime() != 0){
				tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
				tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
			}else{
				tvBeginTime.setText("");
				tvEndTime.setText("");
			}
		} else if (currentPickCategory == PickCategory.AGENT) {
			tvAgent.setText(agent.get(position));
			agentIndex = position;
		} else if (currentPickCategory == PickCategory.STATUS) {
			tvStatus.setText(status.get(position));
			statusIndex = position;
		} else if (currentPickCategory == PickCategory.CHANNEL) {
			tvChannel.setText(channels.get(position));
			channelIndex = position;
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(simplePickerView != null && simplePickerView.isShowing()){
			simplePickerView.dismiss();
		}
		if(pvTime != null && pvTime.isShowing()){
			pvTime.dismiss();
		}
	}

	private void timeMatch(TimeInfo info1) {
		if (timeEqual(info1, DateUtils.getTodayStartAndEndTime())) {
			tvTimeText.setText("今天");
		} else if (timeEqual(info1, DateUtils.getYesterdayStartAndEndTime())) {
			tvTimeText.setText("昨天");
		} else if (timeEqual(info1, DateUtils.getTimeInfoByCurrentWeek())) {
			tvTimeText.setText("本周");
		} else if (timeEqual(info1, DateUtils.getTimeInfoByCurrentMonth())) {
			tvTimeText.setText("本月");
		} else if (timeEqual(info1, DateUtils.getTimeInfoByLastMonth())) {
			tvTimeText.setText("上月");
		} else {
			tvTimeText.setText("指定时间");
		}
	}

	private boolean timeEqual(TimeInfo info1, TimeInfo info2){
		return info1.getStartTime() / 60000 == info2.getStartTime() / 60000 && info1.getEndTime() / 60000 == info2.getEndTime() / 60000;
	}

	public enum PickCategory{
		TIME,
		AGENT,
		STATUS,
		CHANNEL
	}

	public enum PickTime{
		BEGINTIME,
		ENDTIME
	}
}
