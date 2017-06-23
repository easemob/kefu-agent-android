package com.easemob.helpdesk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;

import com.easemob.helpdesk.activity.main.LoginActivity;
import com.easemob.helpdesk.activity.main.MainActivity;
import com.easemob.helpdesk.typeface.CustomFont;
import com.easemob.helpdesk.utils.HDNotifier;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.hyphenate.kefusdk.HDEventListener;
import com.hyphenate.kefusdk.HDNotifierEvent;
import com.hyphenate.kefusdk.chat.HDClient;
import com.mikepenz.iconics.Iconics;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liyuzhao on 05/04/2017.
 */

public class HDApplication extends Application {
	private static HDApplication instance;
	public Bitmap avatarBitmap = null;
	private final List<Activity> activityList = Collections.synchronizedList(new LinkedList<Activity>());
	public boolean avatarIsUpdate;
	private boolean isBroadcastUnreadCount;
	public static long AgentLastUpdateTime = 0;
	/**
	 * HDEventListener
	 */
	private HDEventListener eventListener;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		HDClient.getInstance().init(this);
        HDClient.getInstance().setDebugMode(true);
		registerEventListener();
		PreferenceUtils.getInstance().init(this);
		registerActivityListener();
		//only required if you add a custom or generic font on your own
		Iconics.init(getApplicationContext());

		//register custom fonts like this (or also provide a font definition file)
		Iconics.registerFont(new CustomFont());

		isBroadcastUnreadCount = PreferenceUtils.getInstance().getBroadcastUnReadCount();
	}

	/**
	 * 当App无界面时，提示来了一条消息
	 */
	protected void registerEventListener() {
		if (eventListener == null){
			eventListener = new HDEventListener() {
				@Override
				public void onEvent(HDNotifierEvent event) {
					switch (event.getEvent()){
						case EventNewMessage:
						case EventNewSession:
							if (MainActivity.instance == null){
								HDNotifier.getInstance().notifiChatMsg(null);
							}
							break;
					}
				}
			};
			HDClient.getInstance().chatManager().addEventListener(eventListener, new HDNotifierEvent.Event[]{HDNotifierEvent.Event.EventNewMessage, HDNotifierEvent.Event.EventNewSession});
		}

	}

	/**
	 * Class Instance Object
	 *
	 * @return HDApplication
	 */
	public static HDApplication getInstance() {
		return instance;
	}

	/**
	 * Save Activity to existing lists
	 *
	 * @param activity
	 */
	public void pushActivity(Activity activity) {
		synchronized (activityList){
			if (!activityList.contains(activity)) {
				activityList.add(activity);
			}
		}
	}

	public void popActivity(Activity activity) {
		synchronized (activityList){
			if (activityList.contains(activity)) {
				activityList.remove(activity);
			}
		}
	}

	/**
	 * 获取当前最顶部的activity的实例
	 * @return
	 */
	public Activity getTopActivity(){
		Activity mBaseActivity;
		synchronized (activityList){
			final int size = activityList.size() - 1;
			if(size < 0){
				return null;
			}
			mBaseActivity = activityList.get(size);
		}
		return mBaseActivity;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	public synchronized void logout() {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				PreferenceUtils.getInstance().removeAgentAll();
			}
		});
	}

	/**
	 * 结束所有的Activity
	 */
	public void finishAllActivity() {
		synchronized (activityList){
			for (Activity activity : activityList) {
				if (activity != null) {
					if (!activity.isFinishing()) {
						activity.finish();
					}
				}
			}
		}
	}

	private void registerActivityListener(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
				@Override
				public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
					/**
					 * 监听 activity 创建事件 将该activity 加入list
					 */
					if(!(activity instanceof LoginActivity)){
						pushActivity(activity);
					}

				}

				@Override
				public void onActivityStarted(Activity activity) {

				}

				@Override
				public void onActivityResumed(Activity activity) {

				}

				@Override
				public void onActivityPaused(Activity activity) {

				}

				@Override
				public void onActivityStopped(Activity activity) {

				}

				@Override
				public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

				}

				@Override
				public void onActivityDestroyed(Activity activity) {
					/**
					 * 监听到Activity销毁事件 将该Activity 从list中移除
					 */
					popActivity(activity);
				}
			});
		}
	}

	public void setBroadcastUnreadCount(boolean enable){
		isBroadcastUnreadCount = enable;
		PreferenceUtils.getInstance().setBroadcastUnReadCount(enable);
	}

	public boolean isBroadcastUnreadCount(){
		return isBroadcastUnreadCount;
	}
}
