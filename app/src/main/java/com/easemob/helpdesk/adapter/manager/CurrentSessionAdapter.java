package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.ManagerHomeActivity;
import com.easemob.helpdesk.widget.CircleImageView;
import com.hyphenate.kefusdk.gsonmodel.manager.CurrentSessionResponse;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/6/21.
 */
public class CurrentSessionAdapter extends RecyclerArrayAdapter<CurrentSessionResponse.ItemsBean> {

    public CurrentSessionAdapter(Context context) {
        super(context);
    }

    @Override public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ManagerCurrentSessionHolder(viewGroup);
    }

    @Override public void OnBindViewHolder(BaseViewHolder holder, final int position) {
        super.OnBindViewHolder(holder, position);
        ((ManagerCurrentSessionHolder) holder).transfer.setVisibility(View.VISIBLE);
        ((ManagerCurrentSessionHolder) holder).transfer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ((ManagerHomeActivity) getContext()).currentSessionIntent(position, 0);
            }
        });
        ((ManagerCurrentSessionHolder) holder).close.setVisibility(View.VISIBLE);
        ((ManagerCurrentSessionHolder) holder).close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ((ManagerHomeActivity) getContext()).currentSessionIntent(position, 1);
            }
        });
    }

    class ManagerCurrentSessionHolder extends BaseViewHolder<CurrentSessionResponse.ItemsBean> {
        TextView tvVisitName;
        TextView tvAgentName;
        TextView tvStartTime;
        View transfer;
        View close;

        CircleImageView civAvatar;
        ImageView ivOriginType;

        public ManagerCurrentSessionHolder(ViewGroup parent) {
            super(parent, R.layout.manager_current_session);
            tvVisitName = $(R.id.tv_name);
            tvAgentName = $(R.id.tv_agent_name);
            tvStartTime = $(R.id.tv_start_time);
            transfer = $(R.id.rl_transfer);
            close = $(R.id.rl_close);
            civAvatar = $(R.id.civ_avatar);
            ivOriginType = $(R.id.iv_originType);
        }

        @Override public void setData(CurrentSessionResponse.ItemsBean data) {
            super.setData(data);
            if (data == null) {
                return;
            }
            if (data.getVisitorUser() != null) {
                tvVisitName.setText(data.getVisitorUser().getNicename());
            }
            if (data.getAgentUserNiceName() != null) {
                tvAgentName.setText(data.getAgentUserNiceName());
            }
            if (data.getStartDateTime() != null) {
                tvStartTime.setText(data.getStartDateTime());
            }

            String originType = data.getOriginType().get(0);
            if (originType != null) {
                switch (originType) {
                    case "weibo":
                        ivOriginType.setImageResource(R.drawable.channel_weibo_icon);
                        break;
                    case "weixin":
                        ivOriginType.setImageResource(R.drawable.channel_wechat_icon);
                        break;
                    case "webim":
                        ivOriginType.setImageResource(R.drawable.channel_web_icon);
                        break;
                    case "app":
                        ivOriginType.setImageResource(R.drawable.channel_app_icon);
                        break;
                }
            }
        }
    }
}
