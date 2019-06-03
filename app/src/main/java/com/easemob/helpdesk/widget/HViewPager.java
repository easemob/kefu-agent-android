package com.easemob.helpdesk.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.hyphenate.kefusdk.utils.HDLog;

/**
 * Created by liyuzhao on 30/03/2018.
 */

public class HViewPager extends ViewPager {

	private static final String TAG = "HViewPager";

	public HViewPager(Context context) {
		super(context);
	}

	public HViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (Exception e) {
			HDLog.e(TAG, "onInterceptTouchEvent:" + android.util.Log.getStackTraceString(e));
		}
		return false;
	}

}
