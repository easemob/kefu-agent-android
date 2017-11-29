package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.kefusdk.entity.HistorySessionEntity;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/3/7.
 */
public class HistoryListAdapter extends RecyclerArrayAdapter<HistorySessionEntity> {

    public HistoryListAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new HistoryListHolder(viewGroup);
    }
}
