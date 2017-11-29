package com.easemob.helpdesk.adapter.manager;

import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.gsonmodel.visitors.VisitorListResponse;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.text.DecimalFormat;

/**
 * Created by liyuzhao on 16/6/24.
 */
public class VisitorLoadHolder extends BaseViewHolder<VisitorListResponse.EntitiesBean> {

    private TextView tvName;
    private TextView tvVisitorCount;
    private TextView tvPercent;
    DecimalFormat df = new DecimalFormat("###,###,##0");

    public VisitorLoadHolder(ViewGroup parent) {
        super(parent, R.layout.row_list_item_visitor_load);

        tvName = $(R.id.tv_name);
        tvVisitorCount = $(R.id.tv_visitor_count);
        tvPercent = $(R.id.tv_percent);

    }

    @Override
    public void setData(VisitorListResponse.EntitiesBean data) {
        super.setData(data);

        tvName.setText(data.getName());
        tvVisitorCount.setText(df.format(data.getCount()));
        tvPercent.setText(data.getPercent());
    }


}
