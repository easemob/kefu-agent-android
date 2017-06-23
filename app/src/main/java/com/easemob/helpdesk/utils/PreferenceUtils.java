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


	public void setAgentAll(String agentAll) {
		editor.putString(SHARED_KEY_AGENTALL, agentAll);
		editor.commit();
	}

	public String getAgentAll() {
		return mSharedPreferences.getString(SHARED_KEY_AGENTALL, null);
	}

	public void removeAgentAll(){
		editor.remove(SHARED_KEY_AGENTALL).commit();
	}

	public void setBroadcastUnReadCount(boolean enable){
		editor.putBoolean(SHARED_KEY_BROADCAST_UNREADCOUNT, enable);
		editor.commit();
	}

	public boolean getBroadcastUnReadCount(){
		return mSharedPreferences.getBoolean(SHARED_KEY_BROADCAST_UNREADCOUNT, false);
	}



}
