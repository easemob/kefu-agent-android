package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.main.MainActivity;
import com.easemob.helpdesk.activity.main.SearchWaitAccessActivity;
import com.hyphenate.kefusdk.gsonmodel.main.WaitQueueResponse;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.mikepenz.iconics.view.IconicsTextView;

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
        private IconicsTextView tvJieRu;
        private IconicsTextView tvTransfer;
        private IconicsTextView tvClose;

        public WaitAccessHolder(ViewGroup parent) {
            super(parent, R.layout.row_waiting_access);
            tvName = $(R.id.tv_name);
            tvTime = $(R.id.tv_time);
            tvVip = $(R.id.tv_vip);
            tvJieRu = $(R.id.tv_jieru);
            tvTransfer = $(R.id.tv_transfer);
            tvClose = $(R.id.tv_close);

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
                    tvTransfer.setVisibility(View.VISIBLE);
                    tvClose.setVisibility(View.VISIBLE);
                }else{
                    tvTransfer.setVisibility(View.INVISIBLE);
                    tvClose.setVisibility(View.INVISIBLE);
                }

            }

            tvJieRu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getContext() instanceof MainActivity){
                        ((MainActivity)getContext()).waitIntent(getAdapterPosition(), 1);
                    }else if (getContext() instanceof SearchWaitAccessActivity){
                        ((SearchWaitAccessActivity)getContext()).clickAccess(getAdapterPosition(), 1);
                    }
                }
            });


            tvTransfer.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if(getContext() instanceof MainActivity){
                        ((MainActivity)getContext()).waitIntent(getAdapterPosition(), 2);
                    }else if (getContext() instanceof SearchWaitAccessActivity){
                        ((SearchWaitAccessActivity)getContext()).clickAccess(getAdapterPosition(), 2);
                    }
                }
            });

            tvClose.setOnClickListener(new View.OnClickListener(){

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
