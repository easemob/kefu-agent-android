package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.kefusdk.gsonmodel.manager.AlarmsReponse;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by tiancruyff on 2017/11/13.
 */

public class AlarmsAdapter extends RecyclerArrayAdapter<AlarmsReponse.EntitiesBean> {

	private Context context;
	public AlarmsAdapter(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
		return new AlarmItemHolder(parent,context);
	}
}
