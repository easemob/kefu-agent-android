package com.easemob.helpdesk.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;

/**
 * Created by liyuzhao on 16/3/1.
 */
public class SessionCloseWindow extends BasePopupWindow {
    private Context mContext;


    public SessionCloseWindow(Context context) {
        this.mContext = context;
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(context).inflate(R.layout.popup_session_more, null);
        this.setContentView(contentView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        setOutsideTouchable(true);
        this.update();
        ColorDrawable cDraw = new ColorDrawable(Color.argb(POPUPWINDOW_BG_ALPHA_HALF, 0, 0, 0));
        this.setBackgroundDrawable(cDraw);


    }

    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            try{
                this.showAsDropDown(parent, CommonUtils.convertDip2Px(mContext, -10), CommonUtils.convertDip2Px(mContext, -10));
            }catch (IllegalStateException e){
                this.dismiss();
            }
        } else {
            this.dismiss();
        }
    }


}
