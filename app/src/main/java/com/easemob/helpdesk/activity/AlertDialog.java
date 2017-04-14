package com.easemob.helpdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.easemob.helpdesk.R;

/**
 * 自定义AlerDialog
 */
public class AlertDialog extends BaseActivity implements OnClickListener {
	/**
	 * Dialog 显示信息View
	 */
	private TextView tvMessage;
	/**
	 * Dialog 确定，取消按钮
	 */
	private Button btnOk,btnCancel;
	/**
	 * 临时记录配置变量
	 */
	private int position;
	/**
	 * 文本框显示内容
	 */
	private String textMessage;
	/**
	 * 确定按钮，显示文字
	 */
	private String okString = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_custom_view);
		Intent gIntent = getIntent();
		textMessage = gIntent.getStringExtra("msg");
		okString = gIntent.getStringExtra("okString");
		position = gIntent.getIntExtra("position", -1);
		initView();
		
	}

	/**
	 * 获取当前Dialog内的View
	 */
	private void initView() {
		tvMessage = (TextView) findViewById(R.id.dialog_message);
		btnOk = (Button) findViewById(R.id.ok);
		btnCancel = (Button) findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		if(!TextUtils.isEmpty(textMessage)){
			tvMessage.setText(textMessage);
		}
		if(!TextUtils.isEmpty(okString)){
			btnOk.setText(okString);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			setResult(RESULT_OK, getIntent().putExtra("position", position));
			if(position != -1){
				ChatActivity.resendPos = position;
			}
			finish();
			break;
		case R.id.cancel:
			finish();
			break;
		default:
			break;
		}
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		Intent gIntent = getIntent();
		textMessage = gIntent.getStringExtra("msg");
		okString = gIntent.getStringExtra("okString");
		position = gIntent.getIntExtra("position", -1);
		initView();
	}
}
