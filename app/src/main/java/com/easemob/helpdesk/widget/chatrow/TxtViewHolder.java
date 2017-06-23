package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.chat.ChatActivity;
import com.easemob.helpdesk.widget.ContextMenu;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.HDTextMessageBody;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class TxtViewHolder extends BaseViewHolder {
	public ProgressBar pb;
	public TextView tv;
	public ChatAdapter adapter;

	public TxtViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		pb = findViewById(R.id.pb_sending);
		tv = findViewById(R.id.tv_chatcontent);
	}

	@Override
	public void handleViewMessage(final HDMessage message, final int position) {
		SimpleCommonUtils.spannableEmoticonFilter(tv, CommonUtils.convertStringByMessageText(((HDTextMessageBody) message.getBody()).getMessage()));

		tv.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				Intent intent = new Intent(context, ContextMenu.class);
				intent.putExtra("position", position);
				long currentTime = System.currentTimeMillis();
				long duration = currentTime - message.getTimestamp();
				boolean isTimeAllow = duration > 0 && duration < 1000 * 60 * 2;
				// App 渠道可以撤回 2分钟以内自己发送的消息
				if (adapter.isAppChannel && isTimeAllow && message.getFromUser().isSelf()) {
					intent.putExtra("type", ContextMenu.TYPE_CONTEXT_MENU_TXT_WITH_RECALL);
				} else {
					intent.putExtra("type", ContextMenu.TYPE_CONTEXT_MENU_TXT);
				}
				((Activity) context).startActivityForResult(intent, ChatActivity.REQUEST_CODE_CONTEXT_MENU);


				return true;
			}
		});

		if (message.direct() == HDMessage.Direct.SEND) {
			setMessageSendCallback(message);
			switch (message.getStatus()) {
				case SUCCESS: // 发送成功
					pb.setVisibility(View.GONE);
					ivStatus.setVisibility(View.GONE);
					break;
				case FAIL: // 发送失败
					pb.setVisibility(View.GONE);
					ivStatus.setVisibility(View.VISIBLE);
					break;
				case INPROGRESS: // 发送中
					pb.setVisibility(View.VISIBLE);
					ivStatus.setVisibility(View.GONE);
					break;
			}
		}
	}


}
