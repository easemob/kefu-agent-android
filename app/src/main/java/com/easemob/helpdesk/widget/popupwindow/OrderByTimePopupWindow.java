package com.easemob.helpdesk.widget.popupwindow;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.RadioButton;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;

import java.util.Calendar;

public class OrderByTimePopupWindow extends BasePopupWindow implements OnCheckedChangeListener, OnClickListener {
	
	private View contentView;
	private Context mContext;
	private RadioButton rb1, rb2, rb3, rb4, rb5, rb6;
//	private long beforeStartTime;
//	private long beforeEndTime;
	private long startTime;
	private long endTime;
	private DatePickerDialog startDateDialog;
	private DatePickerDialog stopDateDialog;
	private Button btnFromTime, btnToTime ,btnOk ,btnClose;
	private String checkedName;
	
	
	
	@SuppressLint("InflateParams")
	public OrderByTimePopupWindow(Context context){
		this.mContext = context;
		contentView = LayoutInflater.from(context).inflate(R.layout.popu_time_list, null);
		this.setContentView(contentView);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.MATCH_PARENT);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.update();
		ColorDrawable cDraw = new ColorDrawable(Color.argb(POPUPWINDOW_BG_ALPHA, 0, 0, 0));
		this.setBackgroundDrawable(cDraw);
		initView();
		initListener();
	}
	
	private void initView() {
		btnFromTime = (Button) contentView.findViewById(R.id.btn_time_from);
		btnToTime = (Button) contentView.findViewById(R.id.btn_time_to);
		btnOk = (Button) contentView.findViewById(R.id.btn_ok);
		btnClose = (Button) contentView.findViewById(R.id.btn_close);

		rb1 = (RadioButton) contentView.findViewById(R.id.rb1);
		rb2 = (RadioButton) contentView.findViewById(R.id.rb2);
		rb3 = (RadioButton) contentView.findViewById(R.id.rb3);
		rb4 = (RadioButton) contentView.findViewById(R.id.rb4);
		rb5 = (RadioButton) contentView.findViewById(R.id.rb5);
		rb6 = (RadioButton) contentView.findViewById(R.id.rb6);
	}

	private void initListener() {
		rb1.setOnCheckedChangeListener(this);
		rb2.setOnCheckedChangeListener(this);
		rb3.setOnCheckedChangeListener(this);
		rb4.setOnCheckedChangeListener(this);
		rb5.setOnCheckedChangeListener(this);
		rb6.setOnCheckedChangeListener(this);
		
		btnClose.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		btnFromTime.setOnClickListener(this);
		btnToTime.setOnClickListener(this);
	}
	
	
	public void showPopupWindow(View parent){
		if(!this.isShowing()){
			this.showAtLocation(parent, Gravity.CENTER, 0, 0);
		}else{
			this.dismiss();
		}
	}


	@SuppressWarnings("ConstantConditions")
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		TimeInfo timeInfo = null;
		if(isChecked){
			switch (buttonView.getId()) {
//			case R.id.rb0:
//				timeInfo = DateUtils.getTimeInfoByAll();
//				checkedName = "全部时段";
//				break;
			case R.id.rb1:
				timeInfo = DateUtils.getTodayStartAndEndTime();
				checkedName = "今天";
				break;
			case R.id.rb2:
				timeInfo = DateUtils.getYesterdayStartAndEndTime();
				checkedName = "昨天";
				break;
			case R.id.rb3:
				timeInfo = DateUtils.getTimeInfoByCurrentWeek();
				checkedName = "本周";
				break;
			case R.id.rb4:
				timeInfo = DateUtils.getTimeInfoByCurrentMonth();
				checkedName = "本月";
				break;
			case R.id.rb5:
				timeInfo = DateUtils.getTimeInfoByLastMonth();
				checkedName = "上月";
				break;
			case R.id.rb6:
				//noinspection ConstantConditions,ConstantConditions,ConstantConditions,ConstantConditions
				timeInfo = DateUtils.getTodayStartAndEndTime();
				checkedName = "指定时段";
				break;
			default:
				break;
			}
			if(timeInfo != null){
				startTime = timeInfo.getStartTime();
				endTime = timeInfo.getEndTime();
				updateCustomerBtn();
			}
		}
		
	}
	
//	private void timeSelectBtnEnable(boolean enable){
//		if(enable){
//			btnFromTime.setEnabled(true);
//			btnToTime.setEnabled(true);
//		}else{
//			btnFromTime.setEnabled(false);
//			btnToTime.setEnabled(false);
//		}
//	}
	
	
	private void updateCustomerBtn() {
		Calendar startCal = Calendar.getInstance();
		startCal.setTimeInMillis(startTime);
		btnFromTime.setText(startCal.get(Calendar.YEAR) + "-" + (startCal.get(Calendar.MONTH) + 1) + "-"
				+ startCal.get(Calendar.DAY_OF_MONTH));
		Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(endTime);
		btnToTime.setText(endCal.get(Calendar.YEAR) + "-" + (endCal.get(Calendar.MONTH) + 1) + "-"
				+ endCal.get(Calendar.DAY_OF_MONTH));

	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_close:
			dismiss();
			break;
		case R.id.btn_ok:
			dismiss();
//			if(mContext instanceof HistorySessionActivity){
////				((HistorySessionActivity)mContext).onFreshData(startTime,endTime);
//				((HistorySessionActivity)mContext).onFreshDataAndChangeText(checkedName,startTime,endTime);
//			}
//			startTime = data.getLongExtra("startTime", 0);
//			endTime = data.getLongExtra("endTime", 0);
//			onFreshData();
			break;
		case R.id.btn_time_from:
			onClickTimeFrom();
			break;
		case R.id.btn_time_to:
			onClickTimeTo();
			break;
		default:
			break;
		}
		
	}
	
	
	private void onClickTimeFrom(){
		if(startTime == 0 || endTime == 0){
			TimeInfo timeInfo = DateUtils.getTodayStartAndEndTime();
			startTime = timeInfo.getStartTime();
			endTime = timeInfo.getEndTime();
		}
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTime);
		startDateDialog = new DatePickerDialog(mContext, new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				startTime = cal.getTimeInMillis();
				btnFromTime.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
				rb6.setChecked(true);
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		startDateDialog.show();
	}
	
	
	private void onClickTimeTo(){
		if(startTime == 0 || endTime == 0){
			TimeInfo timeInfo = DateUtils.getTodayStartAndEndTime();
			startTime = timeInfo.getStartTime();
			endTime = timeInfo.getEndTime();
		}
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(endTime);
		stopDateDialog = new DatePickerDialog(mContext, new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				endTime = cal.getTimeInMillis();
				btnToTime.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
				rb6.setChecked(true);
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		
		stopDateDialog.show();
		
		
	}
	
	
	
	
	
	
	

}
