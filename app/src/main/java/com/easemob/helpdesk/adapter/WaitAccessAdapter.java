package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.SearchWaitAccessActivity;
import com.easemob.helpdesk.listener.OnDataItemClickListener;
import com.easemob.helpdesk.mvp.MainActivity;
import com.hyphenate.kefusdk.gsonmodel.main.WaitQueueResponse;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.text.SimpleDateFormat;

/**
 * Created by liyuzhao on 16/3/7.
 */
public class WaitAccessAdapter extends RecyclerArrayAdapter<WaitQueueResponse.ItemsBean> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public WaitAccessAdapter(Context context) {
        super(context);
    }

    @Override public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new WaitAccessHolder(viewGroup);
    }

    private OnDataItemClickListener<WaitQueueResponse.ItemsBean> dataItemClickListener;

    public void setOnDataItemClickListener(OnDataItemClickListener listener) {
        this.dataItemClickListener = listener;
    }

    class WaitAccessHolder extends BaseViewHolder<WaitQueueResponse.ItemsBean> {
        private TextView tvName;
        private TextView tvTime;
        private TextView tvVip;
        private View jieRu;
        private View transfer;
        private View close;
        private ImageView ivOriginType;

        public WaitAccessHolder(ViewGroup parent) {
            super(parent, R.layout.row_waiting_access);
            tvName = $(R.id.tv_name);
            tvTime = $(R.id.tv_time);
            tvVip = $(R.id.tv_vip);
            jieRu = $(R.id.rl_jieru);
            transfer = $(R.id.rl_transfer);
            close = $(R.id.rl_close);
            ivOriginType = $(R.id.iv_originType);
        }

        @Override public void setData(final WaitQueueResponse.ItemsBean data) {
            super.setData(data);
            if (data == null) {
                return;
            }
            tvName.setText(data.getVisitorName());
            try {
                tvTime.setText(dateFormat.format(dateFormat.parse(data.getCreatedAt())));
            } catch (Exception e) {
                tvTime.setText(data.getCreatedAt());
            }
            tvVip.setText(data.isVip() ? "是" : "否");

            String originType = data.getOriginType();
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

            HDUser emUser = HDClient.getInstance().getCurrentUser();
            if (emUser != null && emUser.getRoles() != null) {
                String roles = emUser.getRoles();
                if (roles.contains("admin")) {
                    transfer.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                } else {
                    transfer.setVisibility(View.INVISIBLE);
                    close.setVisibility(View.INVISIBLE);
                }
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dataItemClickListener != null) {
                        dataItemClickListener.onClick(v, data);
                    }
                }
            });
            jieRu.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).waitIntent(getAdapterPosition(), 1);
                    } else if (getContext() instanceof SearchWaitAccessActivity) {
                        ((SearchWaitAccessActivity) getContext()).clickAccess(getAdapterPosition(), 1);
                    }
                }
            });

            transfer.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).waitIntent(getAdapterPosition(), 2);
                    } else if (getContext() instanceof SearchWaitAccessActivity) {
                        ((SearchWaitAccessActivity) getContext()).clickAccess(getAdapterPosition(), 2);
                    }
                }
            });

            close.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).waitIntent(getAdapterPosition(), 3);
                    } else if (getContext() instanceof SearchWaitAccessActivity) {
                        ((SearchWaitAccessActivity) getContext()).clickAccess(getAdapterPosition(), 3);
                    }
                }
            });
        }
    }
}
