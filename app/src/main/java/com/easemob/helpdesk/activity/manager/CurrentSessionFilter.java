package com.easemob.helpdesk.activity.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.ModifyActivity;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/7/4.
 */
public class CurrentSessionFilter extends BaseActivity implements SimplePickerView.SimplePickSelectItemListener {

	private static final String TAG = CurrentSessionFilter.class.getSimpleName();
	private static final int REQUEST_CODE_VISITORNAME = 0x01;

	private static final String[] dataStrings = {"今天", "昨天", "本周", "本月", "上月"};

	private ArrayList<AgentUser> agentUsers = new ArrayList<>();
	private ArrayList<String> agentUserShows = new ArrayList<>();

	@BindView(R.id.rl_back)
	protected View btnBack;

	@BindView(R.id.right)
	protected TextView btnFilter;

	@BindView(R.id.rl_time)
	protected RelativeLayout rlTimeLayout;

	@BindView(R.id.rl_begintime)
	protected RelativeLayout rlBeginTimeLayout;
	@BindView(R.id.rl_endtime)
	protected RelativeLayout rlEndTimeLayout;


	@BindView(R.id.rl_agent)
	protected RelativeLayout rlAgent;

	@BindView(R.id.rl_visitorname)
	protected RelativeLayout rlVisitorName;

	@BindView(R.id.tv_visitorname_text)
	protected TextView tvVisitorNameText;

	@BindView(R.id.tv_agent_text)
	protected TextView tvAgentText;


	@BindView(R.id.tv_time_text)
	protected TextView tvTimeText;
	@BindView(R.id.tv_begin_time)
	protected TextView tvBeginTime;
	@BindView(R.id.tv_end_time)
	protected TextView tvEndTime;

	private TimeInfo currentTimeInfo;
//    private String currentSessionTagValue = "all";

	private SimplePickerView simplePickerView;
	private TimePickerView pvTime;
	private Context mContext;


	private PickCategory currentPickCategory = PickCategory.TIME;
	private PickTime currentPickTime = PickTime.BEGINTIME;

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
	private String agentUserId = null;
	private String currentVisitorName;

	private Unbinder unbinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.manage_activity_currentsession_filter);
		unbinder = ButterKnife.bind(this);
		mContext = this;
		initView();
		initData();

	}

	private void initView() {
		pvTime = new TimePickerView(this, TimePickerView.Type.ALL);
		pvTime.setCyclic(false);
		pvTime.setCancelable(true);
		//时间选择后回调
		pvTime.setOnTimeSelectListener(new PVTimeSelectListener());
	}


	class PVTimeSelectListener implements TimePickerView.OnTimeSelectListener {

		@Override
		public void onTimeSelect(Date date) {
			if (currentTimeInfo == null) {
				currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
			}
			if (currentPickTime == PickTime.BEGINTIME) {
				if (currentTimeInfo.getEndTime() < date.getTime()) {
					currentTimeInfo.setEndTime(date.getTime());
					tvEndTime.setText(dateFormat.format(date));
				}
				currentTimeInfo.setStartTime(date.getTime());
				tvBeginTime.setText(dateFormat.format(date));

			} else if (currentPickTime == PickTime.ENDTIME) {
				if (date.getTime() < currentTimeInfo.getStartTime()) {
					currentTimeInfo.setStartTime(date.getTime());
					tvBeginTime.setText(dateFormat.format(date));
				}
				currentTimeInfo.setEndTime(date.getTime());
				tvEndTime.setText(dateFormat.format(date));
			}
			timeMatch(currentTimeInfo);
		}
	}

	@OnClick(R.id.rl_back)
	public void onClickByBack(View view) {
		finish();
	}

	@OnClick(R.id.right)
	public void onClickByFilter(View view) {
		Intent sIntent = new Intent();
		sIntent.putExtra("timeinfo", currentTimeInfo);
		if (!TextUtils.isEmpty(currentVisitorName)) {
			sIntent.putExtra("visitorName", currentVisitorName);
		}
		if (!TextUtils.isEmpty(agentUserId)) {
			sIntent.putExtra("agentUserId", agentUserId);
		}
		setResult(RESULT_OK, sIntent);
		finish();
	}

	@OnClick(R.id.rl_time)
	public void onClickByRlTime(View view) {
		closePickerView();
		currentPickCategory = PickCategory.TIME;
		simplePickerView = new SimplePickerView(mContext, dataStrings);
		simplePickerView.setCancelable(true);
		simplePickerView.show();
	}

	@OnClick(R.id.rl_begintime)
	public void onClickByBeginTime(View view) {
		closePVTime();
		currentPickTime = PickTime.BEGINTIME;
		if (currentTimeInfo != null) {
			pvTime.setTime(new Date(currentTimeInfo.getStartTime()));
		} else {
			pvTime.setTime(new Date(System.currentTimeMillis()));
		}
		pvTime.show();
	}

	@OnClick(R.id.rl_agent)
	public void onClickByRlAgent(View view) {
		closePickerView();
		currentPickCategory = PickCategory.AGENT;
		simplePickerView = new SimplePickerView(mContext, agentUserShows);
		simplePickerView.setCancelable(true);
		simplePickerView.show();
	}

	@OnClick(R.id.rl_visitorname)
	public void onClickByVisitorName(View view) {
		Intent modifyIntent = new Intent();
		modifyIntent.setClass(mContext, ModifyActivity.class);
		modifyIntent.putExtra("index", ModifyActivity.SCREENING_MODIFY_VISITORNAME);
		if (!TextUtils.isEmpty(currentVisitorName)) {
			modifyIntent.putExtra("content", currentVisitorName);
		}
		startActivityForResult(modifyIntent, REQUEST_CODE_VISITORNAME);
	}


	@OnClick(R.id.rl_endtime)
	public void onClickByEndTime(View view) {
		closePVTime();
		currentPickTime = PickTime.ENDTIME;
		if (currentTimeInfo != null) {
			pvTime.setTime(new Date(currentTimeInfo.getEndTime()));
		} else {
			pvTime.setTime(new Date(System.currentTimeMillis()));
		}
		pvTime.show();
	}

	private void initData() {
		Intent intent = getIntent();
		currentTimeInfo = (TimeInfo) intent.getSerializableExtra("timeinfo");
		if (currentTimeInfo != null) {
			timeMatch(currentTimeInfo);
			tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
			tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
		} else {
			tvTimeText.setText("指定时间");
			tvBeginTime.setText("");
			tvEndTime.setText("");
		}
		agentUserShows.add("全部客服");
		asyncLoadAllAgents();
	}


	private boolean timeEqual(TimeInfo info1, TimeInfo info2) {
		return info1.getStartTime() / 60000 == info2.getStartTime() / 60000 && info1.getEndTime() / 60000 == info2.getEndTime() / 60000;
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

	public void closePickerView() {
		if (simplePickerView != null && simplePickerView.isShowing()) {
			simplePickerView.dismiss();
		}
	}

	public void closePVTime() {
		if (pvTime != null && pvTime.isShowing()) {
			pvTime.dismiss();
		}
	}

	public void simplePickerSelect(int position) {
		if (currentPickCategory == PickCategory.TIME) {
			if (position >= dataStrings.length) {
				return;
			}
			tvTimeText.setText(dataStrings[position]);
			switch (position) {//今天， 昨天", "本周", "本月", "上月
				case 0:
					currentTimeInfo = DateUtils.getTodayStartAndEndTime();
					break;
				case 1:
					currentTimeInfo = DateUtils.getYesterdayStartAndEndTime();
					break;
				case 2:
					currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
					break;
				case 3:
					currentTimeInfo = DateUtils.getTimeInfoByCurrentMonth();
					break;
				case 4:
					currentTimeInfo = DateUtils.getTimeInfoByLastMonth();
					break;
			}
			timeMatch(currentTimeInfo);
			tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
			tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
		} else if (currentPickCategory == PickCategory.AGENT) {
			if (position > 0 && position <= agentUsers.size()) {
				tvAgentText.setText(agentUserShows.get(position));
				agentUserId = agentUsers.get(position - 1).user.getUserId();
			} else {
				tvAgentText.setText("全部客服");
				agentUserId = "";

			}

		}
	}


	private void asyncLoadAllAgents() {
		agentUsers.clear();
		HDClient.getInstance().agentManager().getAgentList(true, new HDDataCallBack<List<AgentUser>>() {
			@Override
			public void onSuccess(final List<AgentUser> value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						agentUsers.addAll(value);
						for (AgentUser agentUser : agentUsers) {
							agentUserShows.add(agentUser.user.getNicename());
						}
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_VISITORNAME) {
				currentVisitorName = data.getStringExtra("content");
				if (TextUtils.isEmpty(currentVisitorName)) {
					tvVisitorNameText.setText("全部");
				} else {
					tvVisitorNameText.setText(currentVisitorName);
				}
			}

		}


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (unbinder != null) {
			unbinder.unbind();
		}
		closePickerView();
		closePVTime();
	}

	enum PickCategory {
		TIME,
		AGENT,
	}

	enum PickTime {
		BEGINTIME,
		ENDTIME
	}


}
