package com.easemob.helpdesk.utils;

import android.support.v4.util.Pair;

import com.easemob.helpdesk.entity.WorkQualityAgent;
import com.easemob.helpdesk.entity.WorkloadAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class JsonUtils {
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
				workloadAgent.setSum_am(jsonAgent.optLong( "sum_am"));
				workloadAgent.setCnt_svc(jsonAgent.optLong( "cnt_svc"));
				workloadAgent.setSum_vm(jsonAgent.optLong("sum_vm"));
				workloadAgent.setSum_sm(jsonAgent.optLong( "sum_sm"));
				workloadAgent.setCnt_sdc(jsonAgent.optLong( "cnt_sdc"));
				workloadAgent.setCnt_tc(jsonAgent.optLong( "cnt_tc"));
				workloadAgent.setRealName(jsonAgent.optString("realName", ""));
				workloadAgent.setCnt_sc(jsonAgent.optLong( "cnt_sc"));
				workloadAgent.setCnt_sac(jsonAgent.optLong( "cnt_sac"));
				workloadAgent.setCnt_tic(jsonAgent.optLong( "cnt_tic"));
				workloadAgent.setAvg_mc(jsonAgent.optLong( "avg_mc"));
				workloadAgent.setMax_wt(jsonAgent.optLong( "max_wt"));
				workloadAgent.setName(jsonAgent.optString( "name", ""));
				workloadAgent.setKey(jsonAgent.optString( "key", ""));
				workloadAgent.setMax_mc(jsonAgent.optLong( "max_mc"));
				workloadAgent.setCnt_oc(jsonAgent.optLong( "cnt_oc"));
				workloadAgent.setAvg_wt(jsonAgent.optLong( "avg_wt"));
				workloadAgent.setCnt_toc(jsonAgent.optLong( "cnt_toc"));
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
			total_elements = jsonObject.optInt("totalElements");
			for (int i = 0; i < jsonEntities.length(); i++){
				WorkQualityAgent workQualityAgent = new WorkQualityAgent();
				JSONObject jsonAgent = jsonEntities.getJSONObject(i);
				workQualityAgent.max_ar = jsonAgent.optInt( "max_ar");
				workQualityAgent.cnt_ea = jsonAgent.optInt(  "cnt_ea");
				workQualityAgent.max_fr = jsonAgent.optInt("max_fr");
				workQualityAgent.avg_ar = jsonAgent.optDouble("avg_ar");
				workQualityAgent.avg_fr = jsonAgent.optDouble("avg_fr");
				workQualityAgent.avg_vm = jsonAgent.optDouble("avg_vm");
				workQualityAgent.cnt_ua = jsonAgent.optInt( "cnt_ua");
				workQualityAgent.name = jsonAgent.optString( "name", "");
				workQualityAgent.pct_vm = jsonAgent.optString(  "pct_vm", "");
				workQualityAgent.key = jsonAgent.optString( "key", "");
				JSONObject jsonMark = jsonAgent.optJSONObject("markList");
				if (jsonMark != null){
					workQualityAgent.markList = jsonMark.toString();
				}
				workQualityAgents.add(workQualityAgent);
			}
		}catch (JSONException e){
			e.printStackTrace();
		}
		return new android.support.v4.util.Pair(total_elements, workQualityAgents);
	}

//	public static HDUser getEMUserFromJson(JSONObject jsonUser){
//
//		HDUser user = new HDUser();
//		try {
//			user.setTenantId(getLongFromJson(jsonUser,"tenantId"));
//			user.setUserId(jsonUser.getString("userId"));
//			if(jsonUser.has("userType")){
//				user.setUserType(getStringFromJson(jsonUser, "userType"));
//			}else{
//				user.setUserType("Agent");
//			}
//			user.setNicename(jsonUser.getString("nicename"));
//			user.setUsername(getStringFromJson(jsonUser,"username"));
//			user.password = getStringFromJson(jsonUser,"password");
//			user.setRoles(getStringFromJson(jsonUser,"roles"));
//			user.setStatus(getStringFromJson(jsonUser,"status"));
//			user.setOnLineState(getStringFromJson(jsonUser,"onLineState"));
//			if(jsonUser.has("maxServiceSessionCount")){
//				user.maxServiceSessionCount = jsonUser.getInt("maxServiceSessionCount");
//			}
//			user.setAvatar(getStringFromJson(jsonUser,"avatar"));
//			user.setTrueName(getStringFromJson(jsonUser,"trueName"));
//			user.setMobilePhone(getStringFromJson(jsonUser,"mobilePhone"));
//			user.agentNumber = getLongFromJson(jsonUser,"agentNumber");
//			user.lastUpdateDateTime = getStringFromJson(jsonUser, "lastUpdateDateTime");
//			user.welcomeMessage = getStringFromJson(jsonUser,"welcomeMessage");
//			user.currentOnLineState = getStringFromJson(jsonUser,"currentOnLineState");
//			user.instanceId = getStringFromJson(jsonUser,"instanceId");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return user;
//	}


}
