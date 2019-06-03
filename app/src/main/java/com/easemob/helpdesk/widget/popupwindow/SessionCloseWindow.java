package com.easemob.helpdesk.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;

/**
 * Created by liyuzhao on 16/3/1.
 */
public class SessionCloseWindow extends BasePopupWindow {
    private Context mContext;

    private ImageView evalIcon;

    public SessionCloseWindow(Context context) {
        this.mContext = context;
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(context).inflate(R.layout.popup_session_more, null);
        this.setContentView(contentView);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        setOutsideTouchable(true);
        this.update();
        this.setBackgroundDrawable(new ColorDrawable(00000000));
        evalIcon = (ImageView) contentView.findViewById(R.id.iv_eval);
    }

    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            try{
                this.showAsDropDown(parent, CommonUtils.convertDip2Px(mContext, -10),CommonUtils.convertDip2Px(mContext, -10));
            }catch (IllegalStateException e){
                this.dismiss();
            }
        } else {
            this.dismiss();
        }
    }

    public void setEvalIcon(boolean isOver) {
        if (isOver) {
            evalIcon.setImageResource(R.drawable.expand_icon_vote_over);
        } else {
            evalIcon.setImageResource(R.drawable.expand_icon_vote);
        }
    }
}
