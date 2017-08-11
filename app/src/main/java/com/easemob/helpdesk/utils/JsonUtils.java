package com.easemob.helpdesk.utils;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.hyphenate.kefusdk.entity.OSSConfig;
import com.hyphenate.kefusdk.entity.UserTag;
import com.easemob.helpdesk.entity.WorkQualityAgent;
import com.easemob.helpdesk.entity.WorkloadAgent;
import com.hyphenate.kefusdk.bean.HDPhrase;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.hyphenate.kefusdk.utils.JsonUtils.getMessageFromJson;


public class JsonUtils {
	
	private static final String TAG = JsonUtils.class.getSimpleName();

	public static android.support.v4.util.Pair<Integer, List<WorkloadAgent>> getWorkloadAgentsFromJson(String jsonStr){
		List<WorkloadAgent> workloadAgents = new ArrayList<>();
		int total_elements = 0;
		try{
			JSONObject jsonObject = new JSONObject(jsonStr);
			JSONArray jsonEntities = jsonObject.getJSONArray("entities");
			total_elements = jsonObject.getInt("totalElements");
			for (int i = 0; i < jsonEntities.length(); i++){
				/**
				 {
				 "sum_am": 15,
				 "cnt_svc": 8,
				 "sum_vm": 41,
				 "sum_sm": 23,
				 "cnt_sdc": 14,
				 "cnt_tc": 13,
				 "realName": "Uncle",
				 "cnt_sc": 14,
				 "cnt_sac": 6,
				 "cnt_tic": 0,
				 "avg_mc": 6,
				 "max_wt": 66658,
				 "name": "Uncle",
				 "key": "42b6e936-df96-42e4-bd19-0342d356008d",
				 "max_mc": 20,
				 "cnt_oc": 14,
				 "avg_wt": 6980,
				 "cnt_toc": 0
				 }
				 */
				WorkloadAgent workloadAgent = new WorkloadAgent();
				JSONObject jsonAgent = jsonEntities.getJSONObject(i);
				workloadAgent.setSum_am(getLongFromJson(jsonAgent, "sum_am"));
				workloadAgent.setCnt_svc(getLongFromJson(jsonAgent, "cnt_svc"));
				workloadAgent.setSum_vm(getLongFromJson(jsonAgent, "sum_vm"));
				workloadAgent.setSum_sm(getLongFromJson(jsonAgent, "sum_sm"));
				workloadAgent.setCnt_sdc(getLongFromJson(jsonAgent, "cnt_sdc"));
				workloadAgent.setCnt_tc(getLongFromJson(jsonAgent, "cnt_tc"));
				workloadAgent.setRealName(getStringFromJson(jsonAgent, "realName"));
				workloadAgent.setCnt_sc(getLongFromJson(jsonAgent, "cnt_sc"));
				workloadAgent.setCnt_sac(getLongFromJson(jsonAgent, "cnt_sac"));
				workloadAgent.setCnt_tic(getLongFromJson(jsonAgent, "cnt_tic"));
				workloadAgent.setAvg_mc(getLongFromJson(jsonAgent, "avg_mc"));
				workloadAgent.setMax_wt(getLongFromJson(jsonAgent, "max_wt"));
				workloadAgent.setName(getStringFromJson(jsonAgent, "name"));
				workloadAgent.setKey(getStringFromJson(jsonAgent, "key"));
				workloadAgent.setMax_mc(getLongFromJson(jsonAgent, "max_mc"));
				workloadAgent.setCnt_oc(getLongFromJson(jsonAgent, "cnt_oc"));
				workloadAgent.setAvg_wt(getLongFromJson(jsonAgent, "avg_wt"));
				workloadAgent.setCnt_toc(getLongFromJson(jsonAgent, "cnt_toc"));
				workloadAgents.add(workloadAgent);
			}
		}catch (JSONException e){
			e.printStackTrace();
		}


		return new android.support.v4.util.Pair(total_elements, workloadAgents);
	}


	public static Pair<Integer, List<WorkQualityAgent>> getWorkQualityAgentsFromJson(String jsonStr){
		List<WorkQualityAgent> workQualityAgents = new ArrayList<>();
		int total_elements = 0;
		try{
			JSONObject jsonObject = new JSONObject(jsonStr);
			JSONArray jsonEntities = jsonObject.getJSONArray("entities");
			total_elements = getIntFromJson(jsonObject, "totalElements");
			for (int i = 0; i < jsonEntities.length(); i++){
				WorkQualityAgent workQualityAgent = new WorkQualityAgent();
				JSONObject jsonAgent = jsonEntities.getJSONObject(i);
				workQualityAgent.max_ar = getIntFromJson(jsonAgent, "max_ar");
				workQualityAgent.cnt_ea = getIntFromJson(jsonAgent, "cnt_ea");
				workQualityAgent.max_fr = getIntFromJson(jsonAgent, "max_fr");
				workQualityAgent.avg_ar = jsonAgent.getDouble("avg_ar");
				workQualityAgent.avg_fr = jsonAgent.getDouble("avg_fr");
				workQualityAgent.avg_vm = jsonAgent.getDouble("avg_vm");
				workQualityAgent.cnt_ua = getIntFromJson(jsonAgent, "cnt_ua");
				workQualityAgent.name = getStringFromJson(jsonAgent, "name");
				workQualityAgent.pct_vm = getStringFromJson(jsonAgent, "pct_vm");
				workQualityAgent.key = getStringFromJson(jsonAgent, "key");
				if (jsonAgent.has("markList")){
					JSONObject jsonMark = jsonAgent.getJSONObject("markList");
					workQualityAgent.markList = jsonMark.toString();
				}
				workQualityAgents.add(workQualityAgent);
			}
		}catch (JSONException e){
			e.printStackTrace();
		}
		return new android.support.v4.util.Pair(total_elements, workQualityAgents);
	}

	public static HDUser getEMUserFromJson(JSONObject jsonUser){
		
		HDUser user = new HDUser();
		try {
			user.setTenantId(getLongFromJson(jsonUser,"tenantId"));
			user.setUserId(jsonUser.getString("userId"));
			if(jsonUser.has("userType")){
				user.setUserType(getStringFromJson(jsonUser, "userType"));
			}else{
				user.setUserType("Agent");
			}
			user.setNicename(jsonUser.getString("nicename"));
			user.setUsername(getStringFromJson(jsonUser,"username"));
			user.password = getStringFromJson(jsonUser,"password");
			user.setRoles(getStringFromJson(jsonUser,"roles"));
			user.setStatus(getStringFromJson(jsonUser,"status"));
			user.setOnLineState(getStringFromJson(jsonUser,"onLineState"));
			if(jsonUser.has("maxServiceSessionCount")){
				user.maxServiceSessionCount = jsonUser.getInt("maxServiceSessionCount");
			}
			user.setAvatar(getStringFromJson(jsonUser,"avatar"));
			user.setTrueName(getStringFromJson(jsonUser,"trueName"));
			user.setMobilePhone(getStringFromJson(jsonUser,"mobilePhone"));
			user.agentNumber = getLongFromJson(jsonUser,"agentNumber");
			user.lastUpdateDateTime = getStringFromJson(jsonUser, "lastUpdateDateTime");
			user.welcomeMessage = getStringFromJson(jsonUser,"welcomeMessage");
			user.currentOnLineState = getStringFromJson(jsonUser,"currentOnLineState");
			user.instanceId = getStringFromJson(jsonUser,"instanceId");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}


	private static int getIntFromJson(JSONObject jsonObj,String jsonName) throws JSONException{
		if(jsonObj.has(jsonName)){
			return jsonObj.getInt(jsonName);
		}
		return 0;
	}

	private static String getStringFromJson(JSONObject jsonObj,String jsonName) throws JSONException{
		if(jsonObj.has(jsonName) && !jsonObj.isNull(jsonName)){
			return jsonObj.getString(jsonName);
		}
		return null;
	}
	private static Long getLongFromJson(JSONObject jsonObj,String jsonName) throws JSONException{
		if(jsonObj.has(jsonName)){
			return jsonObj.getLong(jsonName);
		}
		return 0L;
	}


}
