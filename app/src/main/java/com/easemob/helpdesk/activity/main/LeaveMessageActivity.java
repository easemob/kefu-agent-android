package com.easemob.helpdesk.activity.main;

import android.content.Intent;
import android.os.Bundle;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;

public class LeaveMessageActivity extends BaseActivity implements SimplePickerView.SimplePickSelectItemListener{

	LeaveMessageFragment leaveMessageFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_leave_message);
		leaveMessageFragment = new LeaveMessageFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.main_layout, leaveMessageFragment).commit();
	}

	public void simplePickerSelect(int position){
		leaveMessageFragment.simplePickerSelect(position);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		leaveMessageFragment.onFragmentResult(requestCode, resultCode, data);
	}
}
