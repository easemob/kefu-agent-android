package com.easemob.helpdesk.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.ShortCutEntity;
import com.easemob.helpdesk.entity.ShortCutGroupEntity;
import com.easemob.helpdesk.widget.expandlistview.BaseExpandListAdapter;

public class ShortCutGroupAdapter extends BaseExpandListAdapter {

	private List<ShortCutGroupEntity> mData;
	private ParentViewHolder pHolder;
	private ChildViewHolder cHolder;

	public ShortCutGroupAdapter(Context context, List<ShortCutGroupEntity> data) {
		super(context);
		this.mData = data;
	}

	@Override
	public int getParentCount() {
		return mData != null ? mData.size() : 0;
	}

	@Override
	public int getChildCount(int position) {
		List<ShortCutEntity> sub = mData.get(position).shortCutEntitys;
		return sub != null ? sub.size() : 0;
	}

	@Override
	public boolean isCanExpand(int position) {
		return mData.get(position).shortCutEntitys != null && mData.get(position).shortCutEntitys.size() != 0;
	}

	@Override
	public View getParentView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			pHolder = new ParentViewHolder();
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_parent, parent, false);
			pHolder.title = (TextView) convertView.findViewById(R.id.title);
			pHolder.arrow = (ImageView) convertView.findViewById(R.id.arrow_image);
			convertView.setTag(pHolder);
		} else {
			pHolder = (ParentViewHolder) convertView.getTag();
		}

		pHolder.title.setText(mData.get(position).shortcutMessageGroupName);

		if (isCanExpand(position)) {
			pHolder.arrow.setVisibility(View.VISIBLE);
		} else {
			pHolder.arrow.setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override
	public View getChildView(int parentPosition, int childPosition, View convertView, ViewGroup parent) {
		if (convertView == null) {
			cHolder = new ChildViewHolder();
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_child, parent, false);
			cHolder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(cHolder);
		} else {
			cHolder = (ChildViewHolder) convertView.getTag();
		}

		cHolder.title.setText(mData.get(parentPosition).shortCutEntitys.get(childPosition).message);
		return convertView;
	}

	private static class ParentViewHolder {
		TextView title;
		ImageView arrow;
	}

	private static class ChildViewHolder {
		TextView title;
	}

}
