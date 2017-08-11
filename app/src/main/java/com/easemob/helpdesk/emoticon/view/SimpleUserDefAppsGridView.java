package com.easemob.helpdesk.emoticon.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.GridView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.emoticon.adapter.ChattingAppsAdapter;
import com.easemob.helpdesk.emoticon.data.AppBean;
import com.easemob.helpdesk.mvp.BaseChatActivity;

import java.util.ArrayList;

public class SimpleUserDefAppsGridView extends SimpleAppsGridView {

    public SimpleUserDefAppsGridView(Context context) {
        super(context);
    }

    public SimpleUserDefAppsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    protected void init(){
        GridView gv_apps = (GridView) view.findViewById(R.id.gv_apps);
        gv_apps.setSelector(new ColorDrawable(Color.TRANSPARENT));
//        gv_apps.setNumColumns(2);
        ArrayList<AppBean> mAppBeanList = new ArrayList<>();
        if (getContext() instanceof BaseChatActivity){
            mAppBeanList.addAll(((BaseChatActivity)getContext()).getExtendAppBeans());
        }
        ChattingAppsAdapter adapter = new ChattingAppsAdapter(getContext(), mAppBeanList);
        gv_apps.setAdapter(adapter);
    }
}
