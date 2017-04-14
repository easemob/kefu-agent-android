package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.hyphenate.kefusdk.entity.HDMessage;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class RecallViewHolder extends BaseViewHolder {

	public TextView tv;

	public RecallViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		tv = findViewById(R.id.tv_chatcontent);
	}

	@Override
	public void handleViewMessage(HDMessage message, int position) {
		String nicename = message.getFromUser().getNicename();
		String msgType = "未知";
		switch (message.getType()) {
			case TXT:
				msgType = "[文本]";
				break;
			case IMAGE:
				msgType = "[图片]";
				break;
			case LOCATION:
				msgType = "[位置]";
				break;
			case FILE:
				msgType = "[文件]";
				break;
			case VIDEO:
				msgType = "[视频]";
				break;
			case VOICE:
				msgType = "[语音]";
				break;
		}

		tv.setText(nicename + " " + "撤销了一条" + msgType + "消息");
	}

}
