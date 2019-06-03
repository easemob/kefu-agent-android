package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuzhao on 30/01/2018.
 */

public class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.MessageViewHolder> {

	private List<? extends Object> modelList = new ArrayList<>();
	private Context context;
	private int selectedIndex = -1;
	private OnItemClickListener onItemClickListener;

	public HorizontalRecyclerViewAdapter(Context context, List<? extends Object> horizontalList) {
		this.modelList = horizontalList;
		this.context = context;
	}

	@Override
	public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_horizontal_recyclerview, parent, false);
		return new MessageViewHolder(itemView);
	}


	@Override
	public void onBindViewHolder(MessageViewHolder holder, int p) {
		final int position = holder.getAdapterPosition();
		final Object model = modelList.get(position);
		MessageViewHolder messageViewHolder = holder;
		messageViewHolder.textView.setText(model.toString());
		if (selectedIndex != position) {
			messageViewHolder.textView.setTextColor(Color.BLACK);
		} else {
			messageViewHolder.textView.setTextColor(Color.parseColor("#049ae2"));
		}
		messageViewHolder.textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onItemClickListener != null) {
					onItemClickListener.onItemClick(v, position);
				}
			}
		});
	}


	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}

	public Object getItemAtPosition(int position){
		if (position < getItemCount()){
			return modelList.get(position);
		}
		return null;
	}

	@Override
	public int getItemCount() {
		return modelList.size();
	}


	class MessageViewHolder extends RecyclerView.ViewHolder {
		TextView textView;

		public MessageViewHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.textview);
		}
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.onItemClickListener = listener;
	}

	public interface OnItemClickListener {
		void onItemClick(View view, int position);
	}

}
