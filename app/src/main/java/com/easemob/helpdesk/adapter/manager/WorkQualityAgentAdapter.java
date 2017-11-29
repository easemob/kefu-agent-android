package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.view.ViewGroup;

import com.easemob.helpdesk.entity.WorkQualityAgent;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/6/23.
 */
public class WorkQualityAgentAdapter extends RecyclerArrayAdapter<WorkQualityAgent> {
    public WorkQualityAgentAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WorkQualityAgentHolder(viewGroup);
    }
}
