package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.view.ViewGroup;

import com.easemob.helpdesk.entity.WorkloadAgent;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/6/22.
 */
public class WorkloadAgentAdapter extends RecyclerArrayAdapter<WorkloadAgent> {

    public WorkloadAgentAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WorkloadAgentHolder(viewGroup);
    }
}
