package com.easemob.helpdesk.adapter.manager;


import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
/**
 * Created by tiancruyff on 2017/4/19.
 */

public class RealtimeAdapter extends RecyclerArrayAdapter<RealTimeBaseAdapterItem> {

	public RealtimeAdapter(Context context) {
		super(context);
	}

	@Override
	public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
		return new RealtimeHolder(viewGroup);
	}
}
