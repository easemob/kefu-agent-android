package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.gsonmodel.manager.AlarmsReponse;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tiancruyff on 2017/11/13.
 */

public class AlarmItemHolder extends BaseViewHolder<AlarmsReponse.EntitiesBean> {
    private TextView date;
    private TextView level;
    private TextView content;
    private TextView customer;
    private TextView agent;
    private Context context;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public AlarmItemHolder(ViewGroup itemView, Context context) {
        super(itemView, R.layout.row_list_item_alarms);
        date = $(R.id.alarm_date);
        level = $(R.id.alarm_level);
        content = $(R.id.alarm_content);
        customer = $(R.id.alarm_customer);
        agent = $(R.id.alarm_agent);
        this.context = context;
    }

    @Override public void setData(AlarmsReponse.EntitiesBean data) {
        super.setData(data);

        date.setText(dateFormat.format(new Date(data.getAlarmDateTime())));
        switch (data.getMonitorLevel()) {
            case "1":
                level.setText("一级报警");
                Drawable drawable1 = context.getResources().getDrawable(R.drawable.block1_icon);
                content.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);
                break;
            case "2":
                level.setText("二级报警");
                Drawable drawable2 = context.getResources().getDrawable(R.drawable.block2_icon);
                content.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null);
                break;
            case "3":
            default:
                level.setText("三级报警");
                Drawable drawable3 = context.getResources().getDrawable(R.drawable.block3_icon);
                content.setCompoundDrawablesWithIntrinsicBounds(drawable3, null, null, null);
                break;
        }

        content.setText(data.getRuleName());
        customer.setText(data.getVisitorName());
        agent.setText(data.getAgentName());
    }
}
