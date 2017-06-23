package com.easemob.helpdesk.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {
	private static String PREFERENCE_NAME = "userinfo";
	private static PreferenceUtils instance = new PreferenceUtils();
	private static SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor editor;
	private Context mContext;
	
	private static final String SHARED_KEY_USERNAME = "shared_username";
	private static final String SHARED_KEY_INIT_DATA = "init_data";
	private static final String SHARED_KEY_TECHCHANNEL = "tech_channel";
	private static final String SHARED_KEY_AGENTALL = "agent_all";
	private static final String SHARED_KEY_BROADCAST_UNREADCOUNT = "broadcast_unreadcount";
	private static final String SHARED_KEY_NEW_MSG_NOTI = "new_msg_noti";
	private static final String SHARED_KEY_NOTI_ALERT_SOUND = "noti_alert_sound";
	private static final String SHARED_KEY_NOTI_ALERT_VIBRATE = "noti_alert_vibrate";

	private PreferenceUtils() {
	}

	public static PreferenceUtils getInstance() {
		return instance;
	}

	public void init(Context context) {
		if (context != null) {
			this.mContext = context;
		}
		mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}
	
	
	public String getUserName(){
		return mSharedPreferences.getString(SHARED_KEY_USERNAME, null);
	}

	public void setUserName(String userName){
		editor.putString(SHARED_KEY_USERNAME, userName);
		editor.commit();
	}


	public void setInitData(String initData){
		editor.putString(SHARED_KEY_INIT_DATA, initData);
		editor.commit();
	}

	public String getInitData(){
		return mSharedPreferences.getString(SHARED_KEY_INIT_DATA, null);
	}

	public void setTechChannel(String techChannel){
		editor.putString(SHARED_KEY_TECHCHANNEL, techChannel);
		editor.commit();
	}

	public String getTechChannel(){
		return mSharedPreferences.getString(SHARED_KEY_TECHCHANNEL, null);
	}

	public void setBroadcastUnReadCount(boolean enable){
		editor.putBoolean(SHARED_KEY_BROADCAST_UNREADCOUNT, enable);
		editor.commit();
	}

	public boolean getBroadcastUnReadCount(){
		return mSharedPreferences.getBoolean(SHARED_KEY_BROADCAST_UNREADCOUNT, false);
	}

	public void setNewMsgNotiStatus(boolean enable) {
		editor.putBoolean(SHARED_KEY_NEW_MSG_NOTI, enable);
		editor.commit();
	}

	public boolean getNewMsgNotiStatus() {
		return mSharedPreferences.getBoolean(SHARED_KEY_NEW_MSG_NOTI, true);
	}

	public void setNotiAlertSoundStatus(boolean enable) {
		editor.putBoolean(SHARED_KEY_NOTI_ALERT_SOUND, enable);
		editor.commit();
	}

	public boolean getNotiAlertSoundStatus() {
		return mSharedPreferences.getBoolean(SHARED_KEY_NOTI_ALERT_SOUND, true);
	}

	public void setNotiAlertVibrateStatus(boolean enable) {
		editor.putBoolean(SHARED_KEY_NOTI_ALERT_VIBRATE ,enable);
		editor.commit();
	}

	public boolean getNotiAlertVibrateStatus() {
		return mSharedPreferences.getBoolean(SHARED_KEY_NOTI_ALERT_VIBRATE, true);
	}
}
