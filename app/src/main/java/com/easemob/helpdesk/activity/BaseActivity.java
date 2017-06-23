package com.easemob.helpdesk.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.activity.main.LoginActivity;

public class BaseActivity extends AppCompatActivity {
	private InputMethodManager manager;

	protected Activity mActivity;

	/**
	 * Perform initialization of all fragments and loaders.
	 *
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		if(!(this instanceof LoginActivity)){
			HDApplication.getInstance().pushActivity(this);
		}
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	/**
	 * 隐藏软键盘
	 */
	protected void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(!(this instanceof LoginActivity)){
			HDApplication.getInstance().popActivity(this);
		}
	}

	/**
	 * 通过xml查找相应的ID，通用方法
	 * @param id
	 * @param <T>
	 * @return
	 */
	protected <T extends View> T $(@IdRes int id) {
		return (T) findViewById(id);
	}

}
