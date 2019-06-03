package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.DateUtils;
import com.hyphenate.kefusdk.entity.HistorySessionEntity;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistorySessionAdapter extends BaseAdapter{
	
	private List<HistorySessionEntity> mList;
	private Context mContext;
	private HistorySessionEntity[] sessions;
	
	private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;

	private WeakHandler handler;

	private static class WeakHandler extends Handler{
		WeakReference<HistorySessionAdapter> weakReference;
		public WeakHandler(HistorySessionAdapter adapter){
			this.weakReference = new WeakReference<HistorySessionAdapter>(adapter);
		}
		private void refreshList() {
			HistorySessionAdapter adapter = weakReference.get();
			if (null != adapter){
				adapter.sessions = adapter.mList.toArray(new HistorySessionEntity[adapter.mList.size()]);
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


	
	/**
	 * 刷新页面
	 */
	public void refresh(){
		if(handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)){
			return;
		}
		Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
		handler.sendMessage(msg);
	}
	
	public HistorySessionAdapter(Context mContext,List<HistorySessionEntity> sessionList){
		this.mContext = mContext;
		this.mList = sessionList;
		handler = new WeakHandler(this);
	}
	

	@Override
	public int getCount() {
		return sessions==null?0:sessions.length;
	}

	@Override
	public HistorySessionEntity getItem(int position) {
		if(sessions!=null&&position<sessions.length){
			return sessions[position];
		}
		return null;
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
			viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name);
			viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.message);
			viewHolder.tvTime = (TextView) convertView.findViewById(R.id.time);
			viewHolder.list_item_layout=(RelativeLayout) convertView.findViewById(R.id.list_item_layout);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		HistorySessionEntity  sEntity = getItem(position);
		viewHolder.nameTextView.setText(sEntity.visitorUser.getNicename());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		try {
			viewHolder.tvTime.setText(DateUtils.getTimestampString(dateFormat.parse(sEntity.startDateTime)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String summarysDetail = sEntity.summarysDetail;
		if(!TextUtils.isEmpty(summarysDetail)){
			viewHolder.tvMessage.setText("会话标签："+sEntity.summarysDetail);
		}
		return convertView;
	}
	
	class ViewHolder{
		TextView nameTextView;
		ImageView avatar;
		TextView tvTime;
		TextView tvMessage;
		RelativeLayout list_item_layout;
	}
	

}
