package com.easemob.helpdesk.utils;

import android.text.TextUtils;

import com.hyphenate.kefusdk.bean.CategoryTreeEntity;
import com.hyphenate.kefusdk.utils.HDLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by liyuzhao on 16/4/28.
 */
public class CategoryTreeUtils {
    private static final String TAG = CategoryTreeUtils.class.getSimpleName();

    private List<CategoryTreeEntity> allEntities = Collections.synchronizedList(new ArrayList<CategoryTreeEntity>());
    private JSONArray jsonArray;

    private static CategoryTreeUtils instance = new CategoryTreeUtils();


    public static CategoryTreeUtils getInstance(){
        return instance;
    }

    public  synchronized List<CategoryTreeEntity> getAllEntities(String value){
        if(allEntities.size() > 0){
            allEntities.clear();
        }
        try{
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                getTreeEntity(jsonObject, 0, null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return allEntities;
    }

    private void getTreeEntity(JSONObject jsonObject, long parentColor, String rootName){
        CategoryTreeEntity entty = new CategoryTreeEntity();
        try{
            entty.id = jsonObject.getLong("id");
            entty.parentId = jsonObject.getLong("parentId");
            entty.tenantId = jsonObject.getLong("tenantId");
            entty.name = jsonObject.getString("name");
            entty.description = jsonObject.getString("description");
            if(entty.parentId != 0){
                entty.color = parentColor;
            }else{
                entty.color = jsonObject.getLong("color");
            }
            if(entty.parentId != 0){
                entty.rootName = rootName;
            }
            entty.lastUpdateDateTime = jsonObject.getString("lastUpdateDateTime");
            entty.createDateTime = jsonObject.getString("createDateTime");
            entty.deleted = jsonObject.getBoolean("deleted");
            if(jsonObject.has("children")&&!jsonObject.isNull("children")){
                String childRootName = null;
                if(entty.parentId == 0){
                    childRootName = entty.name;
                }else if(!TextUtils.isEmpty(rootName)){
                    childRootName = rootName;
                }
                JSONArray childArray = jsonObject.getJSONArray("children");
                if(childArray.length() > 0){
                    entty.hasChildren = true;
                }
                allEntities.add(entty);
                for (int i = 0; i < childArray.length(); i++) {
                    getTreeEntity(childArray.getJSONObject(i), entty.color, childRootName);
                }
            }else{
                allEntities.add(entty);
            }
        }catch (Exception e){
            e.printStackTrace();
            HDLog.e(TAG, "getTreeEntity error:" + e.getMessage());
        }
    }

}
