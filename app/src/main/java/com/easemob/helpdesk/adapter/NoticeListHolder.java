package com.easemob.helpdesk.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.gsonmodel.main.NoticesResponse;
import com.easemob.helpdesk.widget.imageview.RoundImageView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liyuzhao on 16/3/14.
 */
public class NoticeListHolder extends BaseViewHolder<NoticesResponse.EntitiesBean> {

    private RoundImageView avatar;
    private TextView tvName;
    private TextView tvTime;
    private TextView tvMessage;
    private TextView tvIcon;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public NoticeListHolder(ViewGroup parent) {
        super(parent, R.layout.row_notice_list_item);
        avatar = $(R.id.avatar);
        tvName = $(R.id.name);
        tvTime = $(R.id.time);
        tvMessage = $(R.id.message);
        tvIcon = $(R.id.tv_if_circel);
    }

    @Override
    public void setData(NoticesResponse.EntitiesBean data) {
        avatar.setImageResource(R.drawable.default_avatar);
        if(data == null){
            return;
        }
        String actorName = "通知消息";
        try{
            actorName = data.getActor().getName();
        }catch (Exception e){
        }
        tvName.setText(actorName);
        tvTime.setText(dateFormat.format(new Date(data.getCreated_at())));
        try{
            tvMessage.setText(data.getObject().getContent().getSummary());
        }catch (Exception e){
            e.printStackTrace();
        }
        if(data.getStatus().equals("read")) {
            tvIcon.setVisibility(View.GONE);
        } else {
            tvIcon.setVisibility(View.VISIBLE);
        }

    }
}
