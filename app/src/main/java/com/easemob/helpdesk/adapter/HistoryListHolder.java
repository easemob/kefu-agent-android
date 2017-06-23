package com.easemob.helpdesk.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.hyphenate.kefusdk.bean.HistorySessionEntity;
import com.hyphenate.kefusdk.bean.Summary;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by liyuzhao on 16/3/7.
 */
public class HistoryListHolder extends BaseViewHolder<HistorySessionEntity> {

    ImageView ivOriginType;
    ImageView avatar;
    TextView tvName;
    TextView tvTime;
    TextView tvAgent;
    TextView tvSummaryCount;
    TextView tvSummary;

    public HistoryListHolder(ViewGroup parent) {
        super(parent, R.layout.item_history_session);
        ivOriginType = $(R.id.originType);
        avatar = $(R.id.avatar);
        tvName = $(R.id.name);
        tvTime = $(R.id.time);
        tvAgent = $(R.id.tv_agent);
        tvSummaryCount = $(R.id.tv_summary_count);
        tvSummary = $(R.id.tv_summary);
    }

    @Override
    public void setData(HistorySessionEntity data) {
        super.setData(data);
        if(data == null){
            return;
        }
        if(data.visitorUser != null){
            tvName.setText(data.visitorUser.getNicename());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            tvTime.setText(DateUtils.getTimestampString(dateFormat.parse(data.startDateTime)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(!TextUtils.isEmpty(data.agentUserNiceName)){
            tvAgent.setText("客服-"+data.agentUserNiceName);
        }

       List<Summary> summaryList =  data.summarys;
        if(summaryList != null && summaryList.size() > 0){
            tvSummary.setVisibility(View.VISIBLE);
            tvSummaryCount.setVisibility(View.VISIBLE);

            Summary summary = summaryList.get(0);
            int roundRadius = CommonUtils.convertDip2Px(getContext(), 4);//圆角半径
//            int fillColor = Color.parseColor("#DFDFE0");//内部填充颜色
            GradientDrawable gradientDrawable = new GradientDrawable();//创建drawable
            gradientDrawable.setColor(getColorString(summary.color));
            gradientDrawable.setCornerRadius(roundRadius);
            String parentName = TextUtils.isEmpty(summary.rootName) ? "" : summary.rootName + " · ";
            tvSummary.setText(parentName + summary.name);
            tvSummary.setBackgroundDrawable(gradientDrawable);
            if(summaryList.size() > 1){
                tvSummaryCount.setText("("+summaryList.size()+")");
            }else {
                tvSummaryCount.setText("");
            }

        }else{
            tvSummary.setVisibility(View.GONE);
            tvSummaryCount.setVisibility(View.GONE);
        }
//
//
//        String summarysDetail = data.summarysDetail;
//        if (!TextUtils.isEmpty(summarysDetail)) {
//            tvMessage.setText("会话标签：" + data.summarysDetail);
//        }
//        int strokeWidth = 5;//边框宽度
//        int roundRadius = 15;//圆角半径
//        int strokeColor = Color.parseColor("#2E3135");//边框颜色
//        int fillColor = Color.parseColor("#DFDFE0");//内部填充颜色
//
//        GradientDrawable gradientDrawable = new GradientDrawable();//创建drawable
//        gradientDrawable.setColor(fillColor);
//        gradientDrawable.setStroke(strokeWidth, strokeColor);
//        setBackgroundDrawable(gradientDrawable);
    }


    private int getColorString(int color){
        String strColor;
        if(color == 0){
            strColor = "#000000";
        }else if(color == 255){
            strColor = "#ffffff";
        }else{
            strColor = "#"+Integer.toHexString(color);
            strColor = strColor.substring(0,7);
        }
        return Color.parseColor(strColor);
    }



}
