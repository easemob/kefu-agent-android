package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.kefusdk.gsonmodel.visitors.VisitorListResponse;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/6/24.
 */
public class VisitorLoadAdapter extends RecyclerArrayAdapter<VisitorListResponse.EntitiesBean> {

    public VisitorLoadAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new VisitorLoadHolder(viewGroup);
    }
}
