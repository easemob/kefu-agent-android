package com.easemob.helpdesk.listener;

import android.view.View;

/**
 * Created by lyuzhao on 2015/12/10.
 */
public interface OnItemClickListener {
    void onClick(View itemView, int position);
    //stick 0 取消置顶 1置顶
    void onLongClick(View itemView, int position,int stick);
}
