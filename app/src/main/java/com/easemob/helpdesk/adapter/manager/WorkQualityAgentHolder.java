package com.easemob.helpdesk.adapter.manager;

import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.WorkQualityAgent;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

/**
 * Created by liyuzhao on 16/6/23.
 */
public class WorkQualityAgentHolder extends BaseViewHolder<WorkQualityAgent> {

    TextView tvNickname;
    TextView tvOnlineDur;


    public WorkQualityAgentHolder(ViewGroup parent) {
        super(parent, R.layout.manage_list_item_agent);

        tvNickname = $(R.id.tv_nickname);
        tvOnlineDur = $(R.id.tv_online_dur);

    }

    @Override
    public void setData(WorkQualityAgent data) {
        super.setData(data);
        tvNickname.setText(data.name);
        tvOnlineDur.setText("满意度:" + data.avg_vm + "(" +data.pct_vm + ")");

    }
}
