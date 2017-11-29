package com.easemob.helpdesk.adapter.manager;

import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.WorkloadAgent;
import com.easemob.helpdesk.widget.CircleImageView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

/**
 * Created by liyuzhao on 16/6/22.
 */
public class WorkloadAgentHolder extends BaseViewHolder<WorkloadAgent> {

    CircleImageView ivAvatar;
    TextView tvNickname;
    TextView tvOnlineDur;

    public WorkloadAgentHolder(ViewGroup parent) {
        super(parent, R.layout.manage_list_item_agent);
        ivAvatar = $(R.id.avatar);
        tvNickname = $(R.id.tv_nickname);
        tvOnlineDur = $(R.id.tv_online_dur);
    }

    @Override
    public void setData(WorkloadAgent data) {
        super.setData(data);
        if (data == null){
            return;
        }
        tvNickname.setText(data.getName());
        tvOnlineDur.setText("会话数:" + ((int)data.getCnt_sc()));
    }
}
