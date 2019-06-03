package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView.BufferType;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.ShortCutEntity;
import com.easemob.helpdesk.widget.CollapsibleTextView;

import java.util.List;

public class ShortCutAdapter extends BaseAdapter {
	private Context mContext;
	private List<ShortCutEntity> mList;

	public ShortCutAdapter(Context context, List<ShortCutEntity> mList) {
		this.mContext = context;
		this.mList = mList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public ShortCutEntity getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.test_layout, parent, false);
			holder.textView = (CollapsibleTextView) convertView.findViewById(R.id.mem_info_txt_id);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textView.setDesc(getItem(position).message, BufferType.NORMAL);
		return convertView;
	}

	public static class ViewHolder {
		CollapsibleTextView textView;
	}
}
