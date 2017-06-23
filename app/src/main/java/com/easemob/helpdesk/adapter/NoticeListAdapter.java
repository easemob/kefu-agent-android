package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.kefusdk.gsonmodel.main.NoticesResponse;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/3/14.
 */
public class NoticeListAdapter extends RecyclerArrayAdapter<NoticesResponse.EntitiesBean> {

    public NoticeListAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new NoticeListHolder(viewGroup);
    }
}
