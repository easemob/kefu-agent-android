package com.easemob.helpdesk.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.gsonmodel.customer.CustomerEntity;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

/**
 * Created by tiancruyff on 2017/7/24.
 */

public class CustomersListHolder extends BaseViewHolder<CustomerEntity.EntitiesBean> {

	private TextView tvNick;
	private TextView tvCreateDate;

	public CustomersListHolder(ViewGroup parent) {
		super(parent, R.layout.item_customer);
		tvNick = $(R.id.tv_cus_nick);
		tvCreateDate = $(R.id.tv_cus_createdate);
	}

	@Override
	public void setData(CustomerEntity.EntitiesBean data) {
		if(data == null){
			return;
		}
		tvNick.setText(data.getNickname());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		tvCreateDate.setText(dateFormat.format(new Date(data.getCreated_at())));
	}
}
