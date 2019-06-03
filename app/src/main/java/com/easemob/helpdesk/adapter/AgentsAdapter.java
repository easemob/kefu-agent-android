package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.transfer.TransferActivity;
import com.hyphenate.kefusdk.entity.agent.AgentUser;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.chat.HDClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AgentsAdapter extends BaseAdapter {
	private List<AgentUser> userList;
	private ChatHistoryFilter chatFilter;
	private final Context mContext;
	private AgentUser[] agentUsers;
	private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
	/*
     * 弱引用刷新UI
     */
	private WeakHandler handler;


	private static class WeakHandler extends android.os.Handler {
		WeakReference<AgentsAdapter> weakReference;

		public WeakHandler(AgentsAdapter adapter) {
			this.weakReference = new WeakReference<AgentsAdapter>(adapter);
		}

		private void refreshList() {
			AgentsAdapter adapter = weakReference.get();
			if (null != adapter) {
				adapter.agentUsers = adapter.userList.toArray(new AgentUser[adapter.userList.size()]);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case HANDLER_MESSAGE_REFRESH_LIST:
					refreshList();
					break;
				default:
					break;
			}
		}
	}

	public void refresh(){
		if(handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)){
			return;
		}
		Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
		handler.sendMessage(msg);
	}


	
	public ChatHistoryFilter getChatFilter(){
		if(chatFilter == null){
			chatFilter = new ChatHistoryFilter(userList);
		}
		return chatFilter;
	}
	
	public AgentsAdapter(Context context, List<AgentUser> userList) {
		super();
		this.mContext = context;
		this.userList = userList;
		handler = new WeakHandler(this);
	}

	@Override
	public int getCount() {
		return agentUsers == null ? 0 : agentUsers.length;
	}

	@Override
	public AgentUser getItem(int position) {
		if(agentUsers == null){
			return null;
		}
		if(position >= agentUsers.length){
			return null;
		}
		return agentUsers[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.row_chat_history, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.avatar = (com.easemob.helpdesk.widget.CircleImageView) convertView.findViewById(R.id.avatar);
			viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name);
			viewHolder.unReadMsgNum = (TextView) convertView.findViewById(R.id.unread_msg_number);
			viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.message);
			viewHolder.tvTime = (TextView) convertView.findViewById(R.id.time);
			viewHolder.ivStatusOnLine = (ImageView) convertView.findViewById(R.id.status_online);
			viewHolder.list_item_layout=(RelativeLayout) convertView.findViewById(R.id.list_item_layout);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.avatar.setImageResource(R.drawable.default_agent_avatar);
		AgentUser entty = getItem(position);
		if(entty == null){
			return convertView;
		}
		viewHolder.nameTextView.setText(entty.user.getNicename());
		viewHolder.unReadMsgNum.setVisibility(View.INVISIBLE);
		viewHolder.ivStatusOnLine.setVisibility(View.VISIBLE);
		CommonUtils.setAgentStatusView(viewHolder.ivStatusOnLine, entty.user.getOnLineState());
		CommonUtils.setAgentStatusTextView(viewHolder.tvMessage, entty.user.getOnLineState());
		try{
			if (!TextUtils.isEmpty(entty.user.getAvatar())){
				String avatarUrl = entty.user.getAvatar();
				if (avatarUrl.contains("//")){
					if (!avatarUrl.startsWith("http")){
						avatarUrl = "http:" + avatarUrl;
					}
					if (viewHolder.avatar != null){
						Glide.with(mContext).load(avatarUrl)
								.apply(RequestOptions.placeholderOf(R.drawable.default_agent_avatar))
								.into(viewHolder.avatar);
					}
				}else if (avatarUrl.startsWith("/ossimages")){
					avatarUrl = HDClient.getInstance().getKefuServerAddress() + avatarUrl;
					if (viewHolder.avatar != null){
						Glide.with(mContext).load(avatarUrl)
								.apply(RequestOptions.placeholderOf(R.drawable.default_agent_avatar))
								.into(viewHolder.avatar);
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		if(mContext instanceof TransferActivity){
			viewHolder.unReadMsgNum.setVisibility(View.INVISIBLE);
		}else{
			if(entty.hasUnReadMessage){
				int count = entty.unReadMessageCount;
				if(count > 0){
					viewHolder.unReadMsgNum.setVisibility(View.VISIBLE);
					if(count > 99){
						viewHolder.unReadMsgNum.setText("99+");
					}else{
						viewHolder.unReadMsgNum.setText(entty.unReadMessageCount+"");
					}
				}
			}
		}
		viewHolder.nameTextView.setText(entty.user.getNicename());
		return convertView;
	}

	class ViewHolder{
		TextView nameTextView;
		com.easemob.helpdesk.widget.CircleImageView avatar;
		TextView unReadMsgNum;
		TextView tvTime;
		TextView tvMessage;
		ImageView ivStatusOnLine;
		RelativeLayout list_item_layout;
	}
	
public class ChatHistoryFilter extends Filter{
		
	List<AgentUser> mOriginalValues = null;
		
		public ChatHistoryFilter(List<AgentUser> mList){
			mOriginalValues = mList;
		}
		
		
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if(mOriginalValues == null){
				mOriginalValues = new ArrayList<>();
			}
			if(prefix==null||prefix.length()==0){
				results.values = mOriginalValues;
				results.count = mOriginalValues.size();
			}else{
				String prefixString = prefix.toString().toLowerCase(Locale.US);
				final int count = mOriginalValues.size();
				final ArrayList<AgentUser> newValues = new ArrayList<AgentUser>();
				for (int i = 0; i < count; i++) {
					final AgentUser value = mOriginalValues.get(i);
					String valueText;
					String strNote = value.user.getNicename();
					if(!TextUtils.isEmpty(strNote)){
						valueText = strNote.toLowerCase(Locale.US);
					}else{
						valueText = (value.user.getNicename() != null)?value.user.getNicename().toLowerCase(Locale.US):"";
					}
					
					if(valueText.startsWith(prefixString)){
						newValues.add(value);
					}else if(valueText.contains(prefixString)){
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
			userList = (List<AgentUser>) results.values;
			refresh();
		}
	}
	
}
