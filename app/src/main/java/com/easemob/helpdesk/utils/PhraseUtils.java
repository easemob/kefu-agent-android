package com.easemob.helpdesk.utils;

import com.hyphenate.kefusdk.bean.HDPhrase;
import com.hyphenate.kefusdk.utils.HDLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by liyuzhao on 16/6/2.
 */
public class PhraseUtils {

    private static final String TAG = PhraseUtils.class.getSimpleName();

    private List<HDPhrase> allEntities = Collections.synchronizedList(new ArrayList<HDPhrase>());

    private JSONObject jsonObject;

    private static PhraseUtils instance = new PhraseUtils();

    public static PhraseUtils getInstance(){
        return instance;
    }

    public synchronized List<HDPhrase> getAllEntities(String value){
        if (allEntities.size() > 0){
            allEntities.clear();
        }
        try{
            JSONObject jsonObject = new JSONObject(value);
            JSONArray jsonArray = jsonObject.getJSONArray("entities");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonPhrase = jsonArray.getJSONObject(i);
                getTreeEntity(jsonPhrase);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return allEntities;
    }

    private String getStringFromJson(JSONObject jsonObj, String jsonName) throws JSONException {
        if(jsonObj.has(jsonName) && !jsonObj.isNull(jsonName)){
            return jsonObj.getString(jsonName);
        }
        return null;
    }



    private void getTreeEntity(JSONObject jsonObject){
        HDPhrase entity = new HDPhrase();
        try {
            entity.id = jsonObject.getLong("id");
            entity.tenantId = jsonObject.getLong("tenantId");
            entity.parentId = jsonObject.getLong("parentId");
            entity.agentUserId = getStringFromJson(jsonObject, "agentUserId");
            entity.leaf = jsonObject.getBoolean("leaf");
            entity.deleted = jsonObject.getBoolean("deleted");
            entity.phrase = getStringFromJson(jsonObject, "phrase");
            if (jsonObject.has("brief")){
                entity.brief = getStringFromJson(jsonObject, "brief");
            }
            entity.seq = jsonObject.getInt("seq");
            entity.createDateTime = getStringFromJson(jsonObject, "createDateTime");
            entity.lastUpdateTime = getStringFromJson(jsonObject, "lastUpdateDateTime");
            if (jsonObject.has("children") && !jsonObject.isNull("children")){
                JSONArray childArray = jsonObject.getJSONArray("children");
                if (childArray.length() > 0){
                    entity.hasChildren = true;
                }
                allEntities.add(entity);
                for (int i = 0; i < childArray.length(); i++){
                    getTreeEntity(childArray.getJSONObject(i));
                }
            }else {
                allEntities.add(entity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            HDLog.e(TAG, "getTreeEntity error:" + e.getMessage());
        }
    }

}
