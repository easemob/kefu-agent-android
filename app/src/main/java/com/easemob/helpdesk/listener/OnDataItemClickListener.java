package com.easemob.helpdesk.listener;

import android.view.View;

public interface OnDataItemClickListener<T> {
    void onClick(View itemView, T data);
}
