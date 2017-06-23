package com.easemob.helpdesk.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.chat.ChatActivity;
import com.easemob.helpdesk.widget.chatrow.BaseViewHolder;
import com.easemob.helpdesk.widget.chatrow.FileViewHolder;
import com.easemob.helpdesk.widget.chatrow.ImageViewHolder;
import com.easemob.helpdesk.widget.chatrow.OrderOrTrackViewHolder;
import com.easemob.helpdesk.widget.chatrow.RecallViewHolder;
import com.easemob.helpdesk.widget.chatrow.RobotMenuViewHolder;
import com.easemob.helpdesk.widget.chatrow.TxtViewHolder;
import com.easemob.helpdesk.widget.chatrow.VoiceViewHolder;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.manager.SessionManager;
import com.hyphenate.kefusdk.utils.MessageUtils;

import java.util.List;


/**
 * Created by lyuzhao on 2015/12/15.
 */
public class ChatAdapter extends RecyclerView.Adapter<BaseViewHolder> {

	private final String TAG = getClass().getSimpleName();

	private static final int MESSAGE_TYPE_SENT_TXT = 0;
	private static final int MESSAGE_TYPE_RECV_TXT = 1;
	private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
	private static final int MESSAGE_TYPE_RECV_IMAGE = 3;
	private static final int MESSAGE_TYPE_RECV_ORDER = 4;
	private static final int MESSAGE_TYPE_RECV_TRACK = 5;
	private static final int MESSAGE_TYPE_SENT_VOICE = 6;
	private static final int MESSAGE_TYPE_RECV_VOICE = 7;
	private static final int MESSAGE_TYPE_SENT_FILE = 8;
	private static final int MESSAGE_TYPE_RECV_FILE = 9;
	private static final int MESSAGE_TYPE_SENT_ROBOTMENU = 10;
	private static final int MESSAGE_TYPE_RECV_ROBOTMENU = 11;
	private static final int MESSAGE_TYPE_RECALL_MESSAGE = 12;

	private LayoutInflater inflater;
	private Activity mActivity;
	public int mMinItemWidth;
	public int mMaxItemWidth;

	public View animView;
	public boolean isAppChannel;
	private SessionManager sessionManager;
	private List<HDMessage> messageList;
	private RecyclerView mRecyclerView;

	public ChatAdapter(Activity activity, SessionManager sessionManager, RecyclerView recyclerView) {
		this.mActivity = activity;
		this.sessionManager = sessionManager;
		this.mRecyclerView = recyclerView;
		messageList = sessionManager.getAllMessages();
		inflater = LayoutInflater.from(mActivity);
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		mMaxItemWidth = (int) (displayMetrics.widthPixels * 0.4f);
		mMinItemWidth = (int) (displayMetrics.widthPixels * 0.15f);
		if (activity instanceof ChatActivity) {
			isAppChannel = ((ChatActivity) activity).isAppChannel();
		}
	}


	/**
	 * 刷新页面
	 */
	public void refresh() {
		notifyDataSetChanged();
	}

	/**
	 * 刷新页面，选择最后一个
	 */
	public void refreshSelectLast() {
		if (messageList.size() > 0) {
			mRecyclerView.scrollToPosition(messageList.size() - 1);
		}
	}

	/**
	 * 刷新页面，选择Position
	 */
	public void refreshSeekTo(int position) {
		if (messageList.size() > 0) {
			mRecyclerView.scrollToPosition(position);
		}
	}


	@Override
	public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = null;
		BaseViewHolder holder = null;
		switch (viewType) {
			case MESSAGE_TYPE_RECALL_MESSAGE:
				convertView = inflater.inflate(R.layout.row_recall_message, parent, false);
				holder = new RecallViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_VOICE:
				convertView = inflater.inflate(R.layout.row_received_voice, parent, false);
				holder = new VoiceViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_SENT_VOICE:
				convertView = inflater.inflate(R.layout.row_sent_voice, parent, false);
				holder = new VoiceViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_ORDER:
				convertView = inflater.inflate(R.layout.row_received_order, parent, false);
				holder = new OrderOrTrackViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_TRACK:
				convertView = inflater.inflate(R.layout.row_received_order, parent, false);
				holder = new OrderOrTrackViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_SENT_IMAGE:
				convertView = inflater.inflate(R.layout.row_sent_picture, parent, false);
				holder = new ImageViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_IMAGE:
				convertView = inflater.inflate(R.layout.row_received_picture, parent, false);
				holder = new ImageViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_ROBOTMENU:
				convertView = inflater.inflate(R.layout.row_received_robotmenu_message, parent, false);
				holder = new RobotMenuViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_SENT_ROBOTMENU:
				convertView = inflater.inflate(R.layout.row_sent_robotmenu_message, parent, false);
				holder = new RobotMenuViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_TXT:
				convertView = inflater.inflate(R.layout.row_received_message, parent, false);
				holder = new TxtViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_SENT_TXT:
				convertView = inflater.inflate(R.layout.row_sent_message, parent, false);
				holder = new TxtViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_SENT_FILE:
				convertView = inflater.inflate(R.layout.row_sent_file, parent, false);
				holder = new FileViewHolder(mActivity, this, convertView);
				break;
			case MESSAGE_TYPE_RECV_FILE:
				convertView = inflater.inflate(R.layout.row_received_file, parent, false);
				holder = new FileViewHolder(mActivity, this, convertView);
				break;
		}
		//noinspection ConstantConditions
		convertView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		return holder;

	}


	@Override
	public void onBindViewHolder(BaseViewHolder holder, final int position) {
		if (position > messageList.size()) {
			return;
		}
		final HDMessage message = messageList.get(position);
		if (message.direct() == HDMessage.Direct.RECEIVE) {
			if (sessionManager.isAgentChat()) {
				holder.ivAvatar.setImageResource(R.drawable.default_agent_avatar);
			} else {
				holder.ivAvatar.setImageResource(R.drawable.default_customer_avatar);
			}

		} else {
			if (message.getFromUser().isSelf()) {
				String avatarUrl = HDClient.getInstance().getCurrentUser().getAvatar();
				if (TextUtils.isEmpty(avatarUrl)) {
					holder.ivAvatar.setImageResource(R.drawable.default_agent_avatar);
				} else {
					if (!avatarUrl.startsWith("http")) {
						avatarUrl = "http:" + avatarUrl;
					}
					Glide.with(mActivity).load(avatarUrl).asBitmap()
							.diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.default_agent_avatar).error(R.drawable.default_agent_avatar)
							.into(holder.ivAvatar);
				}

			} else {
				if (holder.ivAvatar != null)
					holder.ivAvatar.setImageResource(R.drawable.default_agent_avatar);
			}
		}
		holder.handleMessage(message, position);
	}

	public HDMessage getItem(int position) {
		if (sessionManager.getAllMessages() != null && position < sessionManager.getAllMessages().size()) {
			return sessionManager.getAllMessages().get(position);
		}
		return null;
	}


	@Override
	public int getItemCount() {
		return messageList.size();
	}

	@Override
	public int getItemViewType(int position) {
		HDMessage message = messageList.get(position);
		if (message == null) {
			return -1;
		}
		if (MessageUtils.isRecallMessage(message)) {
			return MESSAGE_TYPE_RECALL_MESSAGE;
		}

		boolean isSend = message.direct() == HDMessage.Direct.SEND;
		if (message.getType() == HDMessage.Type.VOICE) {
			if (isSend) {
				return MESSAGE_TYPE_SENT_VOICE;
			} else {
				return MESSAGE_TYPE_RECV_VOICE;
			}
		} else if (message.getType() == HDMessage.Type.FILE) {
			if (isSend) {
				return MESSAGE_TYPE_SENT_FILE;
			} else {
				return MESSAGE_TYPE_RECV_FILE;
			}
		} else if (message.getType() == HDMessage.Type.IMAGE) {
			if (isSend) {
				return MESSAGE_TYPE_SENT_IMAGE;
			} else {
				return MESSAGE_TYPE_RECV_IMAGE;
			}
		} else if (message.getType() == HDMessage.Type.TXT) {
			if (MessageUtils.isRobotMenuMessage(message)) {
				if (isSend) {
					return MESSAGE_TYPE_SENT_ROBOTMENU;
				} else {
					return MESSAGE_TYPE_RECV_ROBOTMENU;
				}
			} else if (MessageUtils.isOrderOrTrackMessage(message)) {
				if (isSend) {
					return MESSAGE_TYPE_SENT_TXT;
				} else {
					return MESSAGE_TYPE_RECV_ORDER;
				}
			} else {
				if (isSend) {
					return MESSAGE_TYPE_SENT_TXT;
				} else {
					return MESSAGE_TYPE_RECV_TXT;
				}
			}
		}
		return super.getItemViewType(position);
	}
}
