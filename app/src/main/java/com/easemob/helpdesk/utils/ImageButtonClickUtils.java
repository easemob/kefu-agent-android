package com.easemob.helpdesk.utils;

import android.view.MotionEvent;
import android.view.View;

public class ImageButtonClickUtils {

	private ImageButtonClickUtils() {

	}

	/**
	 * 设置按钮的正反选效果
	 * 
	 * @param view
	 * @param normalResId
	 * @param pressResId
	 */
	public static void setClickState(View view, final int normalResId, final int pressResId) {
		view.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundResource(pressResId);
					break;
				case MotionEvent.ACTION_MOVE:
					v.setBackgroundResource(pressResId);
					break;
				case MotionEvent.ACTION_UP:
					v.setBackgroundResource(normalResId);
					break;
				default:
					break;
				}

				// 为了不影响监听按钮的onClick回调，返回值应为false
				return false;
			}
		});

	}

}
