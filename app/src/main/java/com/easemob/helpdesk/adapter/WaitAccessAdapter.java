package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.SearchWaitAccessActivity;
import com.easemob.helpdesk.mvp.MainActivity;
import com.hyphenate.kefusdk.gsonmodel.main.WaitQueueResponse;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/3/7.
 */
public class WaitAccessAdapter extends RecyclerArrayAdapter<WaitQueueResponse.ItemsBean> {

    public WaitAccessAdapter(Context context){
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new WaitAccessHolder(viewGroup);
    }


    class WaitAccessHolder extends BaseViewHolder<WaitQueueResponse.ItemsBean>{
        private TextView tvName;
        private TextView tvTime;
        private TextView tvVip;
        private View jieRu;
        private View transfer;
        private View close;

        public WaitAccessHolder(ViewGroup parent) {
            super(parent, R.layout.row_waiting_access);
            tvName = $(R.id.tv_name);
            tvTime = $(R.id.tv_time);
            tvVip = $(R.id.tv_vip);
            jieRu = $(R.id.rl_jieru);
            transfer = $(R.id.rl_transfer);
            close = $(R.id.rl_close);

        }

        @Override
        public void setData(final WaitQueueResponse.ItemsBean data) {
            super.setData(data);
            if (data == null) {
                return;
            }
            tvName.setText(data.getUserName());
            tvTime.setText(data.getCreateDateTime());
            tvVip.setText(data.isVip() ? "是" : "否");

            HDUser emUser = HDClient.getInstance().getCurrentUser();
            if (emUser != null && emUser.getRoles() != null){
                String roles = emUser.getRoles();
                if (roles.contains("admin")){
                    transfer.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                }else{
                    transfer.setVisibility(View.INVISIBLE);
                    close.setVisibility(View.INVISIBLE);
                }

            }

            jieRu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getContext() instanceof MainActivity){
                        ((MainActivity)getContext()).waitIntent(getAdapterPosition(), 1);
                    }else if (getContext() instanceof SearchWaitAccessActivity){
                        ((SearchWaitAccessActivity)getContext()).clickAccess(getAdapterPosition(), 1);
                    }
                }
            });


            transfer.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if(getContext() instanceof MainActivity){
                        ((MainActivity)getContext()).waitIntent(getAdapterPosition(), 2);
                    }else if (getContext() instanceof SearchWaitAccessActivity){
                        ((SearchWaitAccessActivity)getContext()).clickAccess(getAdapterPosition(), 2);
                    }
                }
            });

            close.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if(getContext() instanceof MainActivity){
                        ((MainActivity)getContext()).waitIntent(getAdapterPosition(), 3);
                    }else if (getContext() instanceof SearchWaitAccessActivity){
                        ((SearchWaitAccessActivity)getContext()).clickAccess(getAdapterPosition(), 3);
                    }
                }
            });

        }
    }

}
