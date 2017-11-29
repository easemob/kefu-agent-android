package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.kefusdk.gsonmodel.customer.CustomerEntity;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by tiancruyff on 2017/7/24.
 */

public class CustomersListAdapter extends RecyclerArrayAdapter<CustomerEntity.EntitiesBean> {

	public CustomersListAdapter(Context context) {
		super(context);
	}
	@Override
	public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
		return new CustomersListHolder(viewGroup);
	}
}
