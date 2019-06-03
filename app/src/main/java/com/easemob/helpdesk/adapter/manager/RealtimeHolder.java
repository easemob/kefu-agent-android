package com.easemob.helpdesk.adapter.manager;

import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.widget.CircleImageView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;



/**
 * Created by tiancruyff on 2017/4/19.
 */

public class RealtimeHolder extends BaseViewHolder<RealTimeBaseAdapterItem> {
	private TextView index;
	private TextView name;
	private TextView value;
	private CircleImageView img;

	public RealtimeHolder(ViewGroup parent) {
		super(parent, R.layout.manager_realtime_adapter_item);

		index = $(R.id.realtime_index);
		name = $(R.id.realtime_name);
		value = $(R.id.realtime_value);
		img = $(R.id.realtime_img);
	}

	@Override
	public void setData(RealTimeBaseAdapterItem data) {
		super.setData(data);

		index.setText(String.format(" %d ", data.getIndex()));
		name.setText(data.getName());
		value.setText(data.getValue());
		if (data.getImg() != null) {
			img.setImageDrawable(data.getImg());
		}
	}
}
