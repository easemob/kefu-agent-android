package com.easemob.helpdesk.activity.visitor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.UsersTagEntity;
import com.hyphenate.kefusdk.chat.HDClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by tiancruyff on 2017/7/25.
 */

public class CustomersScreeningActivity extends BaseActivity implements View.OnClickListener , SimplePickerView.SimplePickSelectItemListener {

	private View ibtnBack;
	private TextView ibtnFilter;
	private Context mContext;

	private static final String[] dateStrings = {"今天", "昨天", "本周", "本月", "上月", "指定时间"};
	private ArrayList<String> tagsStrings = new ArrayList<>();
	private ArrayList<Integer> tagsKeys = new ArrayList<>();
	private int tagsSelectedIndex = -1;

	private TimeInfo currentTimeInfo;

	private TextView tvCusName;
	private TextView tvCusId;

	private RelativeLayout rlTimeLayout;
	private RelativeLayout rlBeginTimeLayout;
	private RelativeLayout rlEndTimeLayout;
	private RelativeLayout rlCusTagLayout;
	private RelativeLayout rlCusName;
	private RelativeLayout rlCusId;

	private TextView tvTimeText;
	private TextView tvBeginTime;
	private TextView tvEndTime;
	private TextView tvCusTag;

	private Button btnScreenClear;

	private PickCategory currentPickCategory = PickCategory.TIME;
	private PickTime currentPickTime = PickTime.BEGINTIME;


	private SimplePickerView simplePickerView;
	private TimePickerView pvTime;

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_customers_screening);
		mContext = this;
		initView();
		initListener();
		initData();
		getUserTags();
	}

	private void initView(){
		ibtnBack = $(R.id.rl_back);
		ibtnFilter = $(R.id.right);
		rlTimeLayout = $(R.id.rl_time);
		tvTimeText = $(R.id.tv_time_text);
		rlBeginTimeLayout = $(R.id.rl_begintime);
		rlEndTimeLayout = $(R.id.rl_endtime);
		rlCusTagLayout = $(R.id.rl_cus_tag);
		rlCusName = $(R.id.rl_cus_name);
		rlCusId = $(R.id.rl_cus_id);
		tvBeginTime = $(R.id.tv_begin_time);
		tvEndTime = $(R.id.tv_end_time);
		tvCusTag = $(R.id.tv_cus_tag_text);
		tvCusName = $(R.id.et_cus_name_text);
		tvCusId = $(R.id.et_cus_id_text);
		btnScreenClear = $(R.id.screen_clear);

		pvTime = new TimePickerView(this, TimePickerView.Type.ALL);
		pvTime.setCyclic(false);
		pvTime.setCancelable(true);
		//时间选择后回调
		pvTime.setOnTimeSelectListener(new PVTimeSelectListener());
	}

	private void initListener(){
		ibtnBack.setOnClickListener(this);
		ibtnFilter.setOnClickListener(this);

		rlTimeLayout.setOnClickListener(new RlClickListener());
		rlBeginTimeLayout.setOnClickListener(new PVTimeOnClickListener());
		rlEndTimeLayout.setOnClickListener(new PVTimeOnClickListener());
		btnScreenClear.setOnClickListener(this);

		rlCusName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText et = new EditText(CustomersScreeningActivity.this);
				et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(CustomersScreeningActivity.this, 68)));
				et.setLines(1);
				et.setEllipsize(TextUtils.TruncateAt.END);
				et.setSingleLine(true);
				if (!TextUtils.isEmpty(tvCusName.getText().toString())){
					et.setText(tvCusName.getText().toString());
				}
				et.setSelection(et.getText().length());
				new AlertDialog.Builder(CustomersScreeningActivity.this).setTitle("请输入客户名称")
						.setView(et)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								tvCusName.setText(et.getText().toString());
							}
						})
						.setNegativeButton("取消", null).show();
			}
		});

		rlCusId.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText et = new EditText(CustomersScreeningActivity.this);
				et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(CustomersScreeningActivity.this, 68)));
				et.setLines(1);
				et.setEllipsize(TextUtils.TruncateAt.END);
				et.setSingleLine(true);
				if (!TextUtils.isEmpty(tvCusId.getText().toString())){
					et.setText(tvCusId.getText().toString());
				}
				et.setSelection(et.getText().length());
				new AlertDialog.Builder(CustomersScreeningActivity.this).setTitle("请输入客户ID")
						.setView(et)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								tvCusId.setText(et.getText().toString());
							}
						})
						.setNegativeButton("取消", null).show();
			}
		});

	}

	private void initData() {
		if (currentTimeInfo == null) {
			currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
		}
		timeMatch(currentTimeInfo);
		if (currentTimeInfo != null) {
			tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
			tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
		} else {
			tvBeginTime.setText("");
			tvEndTime.setText("");
		}
	}


	private boolean timeEqual(TimeInfo info1, TimeInfo info2){
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
				case R.id.rl_cus_tag:
					currentPickCategory = PickCategory.TAG;
					simplePickerView = new SimplePickerView(mContext, tagsStrings);
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
			case R.id.right:
				Intent intent = new Intent();
				if (currentTimeInfo != null) {
					intent.putExtra("beginDate", DateUtils.getStartDateTimeString(currentTimeInfo.getStartTime()));
					intent.putExtra("endDate", DateUtils.getEndDateTimeString(currentTimeInfo.getEndTime()));
				} else {
					intent.putExtra("beginDate", "");
					intent.putExtra("endDate", "");
				}
				intent.putExtra("cusName", tvCusName.getText().toString().trim());
				intent.putExtra("cusId", tvCusId.getText().toString().trim());
				if (tagsSelectedIndex > 0 && tvCusTag.getText().equals(tagsStrings.get(tagsSelectedIndex))) {
					intent.putExtra("tagsId", String.valueOf(tagsKeys.get(tagsSelectedIndex - 1)));
				} else {
					intent.putExtra("tagsId", "");
				}
				setResult(RESULT_OK, intent);
				finish();
				break;
			case R.id.screen_clear:
				currentTimeInfo = null;
				initData();
				tvCusTag.setText(tagsStrings.get(0));
				tagsSelectedIndex = 0;
				tvCusName.setText("");
				tvCusId.setText("");
				break;
		}

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
					currentTimeInfo = null;
//                    rlBeginTimeLayout.setEnabled(true);
//                    rlEndTimeLayout.setEnabled(true);
					break;
			}

			if(currentTimeInfo != null){
				tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
				tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
			}else{
				tvBeginTime.setText("");
				tvEndTime.setText("");
			}
		} else if (currentPickCategory == PickCategory.TAG) {
			tvCusTag.setText(tagsStrings.get(position));
			tagsSelectedIndex = position;
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

	public enum PickCategory{
		TIME,
		TAG
	}

	public enum PickTime{
		BEGINTIME,
		ENDTIME
	}

	private void getUserTags() {
		HDClient.getInstance().visitorManager().getUserTags(new HDDataCallBack<List<UsersTagEntity.ItemsBean>>() {
			@Override
			public void onSuccess(List<UsersTagEntity.ItemsBean> value) {
				tagsStrings.clear();
				tagsKeys.clear();
				tagsStrings.add("全部标签");
				if (value.size() > 0) {
					for (UsersTagEntity.ItemsBean bean: value) {
						tagsStrings.add(bean.getTagName());
						tagsKeys.add(bean.getUserTagId());
					}
				}
				rlCusTagLayout.setOnClickListener(new RlClickListener());
			}

			@Override
			public void onError(int error, String errorMsg) {
				rlCusTagLayout.setOnClickListener(null);
			}

			@Override
			public void onAuthenticationException() {
				rlCusTagLayout.setOnClickListener(null);
			}
		});
	}

}
