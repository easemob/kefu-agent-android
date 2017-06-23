package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.entity.AgentQueue;

import java.util.ArrayList;
import java.util.List;

public class AgentQueueAdapter extends BaseAdapter {

	private final Context ctx;
	private List<AgentQueue> agentQueues;
	private AgentQueueFilter agentFilter;

	public AgentQueueFilter getAgentFilter() {
		if (agentFilter == null) {
			agentFilter = new AgentQueueFilter(agentQueues);
		}
		return agentFilter;
	}

	public AgentQueueAdapter(Context context, List<AgentQueue> agentQueues) {
		this.ctx = context;
		this.agentQueues = agentQueues;
	}

	@Override
	public int getCount() {
		return agentQueues.size();
	}

	@Override
	public AgentQueue getItem(int position) {
		return agentQueues.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(R.layout.row_chat_history, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name);
			viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.message);
			viewHolder.tvTime = (TextView) convertView.findViewById(R.id.time);
			viewHolder.list_item_layout = (RelativeLayout) convertView.findViewById(R.id.list_item_layout);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.avatar.setImageResource(R.drawable.avatar_department);
		AgentQueue entty = getItem(position);
		viewHolder.nameTextView.setText(entty.queueName);
		viewHolder.tvMessage.setText("(" + entty.onlineAgentCount + "/" + entty.totalAgentCount + ")");
		if(entty.totalAgentCount <= 0){
			convertView.setBackgroundColor(ctx.getResources().getColor(R.color.item_gray));
		}
		return convertView;
	}

	static class ViewHolder {
		TextView nameTextView;
		ImageView avatar;
		TextView tvTime;
		TextView tvMessage;
		RelativeLayout list_item_layout;
	}

	public class AgentQueueFilter extends Filter {

		List<AgentQueue> mOriginalValues = null;

		public AgentQueueFilter(List<AgentQueue> list) {
			mOriginalValues = list;
		}

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if (mOriginalValues == null) {
				mOriginalValues = new ArrayList<>();
			}
			if (prefix == null || prefix.length() == 0) {
				results.values = mOriginalValues;
				results.count = mOriginalValues.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();
				final int count = mOriginalValues.size();
				final ArrayList<AgentQueue> newValues = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					final AgentQueue value = mOriginalValues.get(i);
					String valueText;
					String strNote = value.queueName;
					if (!TextUtils.isEmpty(strNote)) {
						valueText = strNote.toLowerCase();
					} else {
						valueText = (strNote != null) ? strNote : "";
					}
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else if (valueText.contains(prefixString)) {
						newValues.add(value);
					}
				}
				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			agentQueues = (List<AgentQueue>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}

		}

	}

}
