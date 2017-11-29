package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.ContextMenu;
import com.easemob.helpdesk.activity.chat.ShowNormalFileActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.mvp.ChatActivity;
import com.easemob.helpdesk.utils.FileUtils;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.messagebody.HDNormalFileMessageBody;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.util.TextFormater;

import java.io.File;
import java.util.Locale;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class FileViewHolder extends BaseViewHolder {
	public ProgressBar pb;
	public TextView tvPercentage;
	public ImageView ivStatus;
	public TextView tvFileName;
	public TextView tvFileSize;
	public TextView tvFileState;
	public View bubble;

	public FileViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		pb = findViewById(R.id.pb_sending);
		tvPercentage = findViewById(R.id.percentage);
		ivStatus = findViewById(R.id.msg_status);
		tvFileName = findViewById(R.id.tv_file_name);
		tvFileSize = findViewById(R.id.tv_file_size);
		tvFileState = findViewById(R.id.tv_file_state);
		bubble = findViewById(R.id.bubble);
	}

	@Override
	public void handleViewMessage(final HDMessage message, final int position) {
		final HDNormalFileMessageBody fileMessageBody = (HDNormalFileMessageBody) message.getBody();
		final String filePath = fileMessageBody.getLocalPath();
		tvFileName.setText(fileMessageBody.getFileName());
		long fileSize = fileMessageBody.getFileSize();
		if (fileSize > 0) {
			tvFileSize.setText(TextFormater.getDataSize(fileSize));
		}

		bubble.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				Intent intent = new Intent(activity, ContextMenu.class);
				intent.putExtra("position", position);
				long currentTime = System.currentTimeMillis();
				long duration = currentTime - message.getTimestamp();
				boolean isTimeAllow = duration > 0 && duration < 1000 * 60 * 2;
				// App 渠道可以撤回 2分钟以内自己发送的消息
				if (adapter.isAppChannel && isTimeAllow && message.getFromUser().isSelf()) {
					intent.putExtra("type", ContextMenu.TYPE_CONTEXT_MENU_IMAGE_WITH_RECALL);
				} else {
					return false;
				}
				activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_CONTEXT_MENU);

				return true;
			}
		});

		bubble.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
					//下载
					activity.startActivity(new Intent(activity, ShowNormalFileActivity.class).putExtra("message", message));
				} else {
					//文件打开，直接打开
					FileUtils.openFile(new File(filePath), activity);
				}
			}
		});

		if (message.direct() == HDMessage.Direct.RECEIVE) {
			HDLog.d(TAG, "it is receive msg");
			if (TextUtils.isEmpty(filePath)) {
				tvFileState.setText("未下载");
			} else {
				File file = new File(filePath);
				if (file.exists()) {
					tvFileState.setText("已下载");
				} else {
					tvFileState.setText("未下载");
				}
			}
			return;
		} else {
			setMessageSendCallback(message);
		}

		// until here, deal with send file msg
		switch (message.getStatus()) {
			case SUCCESS:
				pb.setVisibility(View.INVISIBLE);
				tvPercentage.setVisibility(View.INVISIBLE);
				ivStatus.setVisibility(View.INVISIBLE);
				break;
			case FAIL:
				if (pb != null) {
					pb.setVisibility(View.INVISIBLE);
				}
				tvPercentage.setVisibility(View.INVISIBLE);
				ivStatus.setVisibility(View.VISIBLE);
				break;
			case INPROGRESS:
				if (pb != null) {
					pb.setVisibility(View.VISIBLE);
				}
				tvPercentage.setVisibility(View.VISIBLE);
				tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", message.getProgress()));
				break;
			case CREATE:
				if (pb != null) {
					pb.setVisibility(View.INVISIBLE);
				}
				tvPercentage.setVisibility(View.INVISIBLE);
				ivStatus.setVisibility(View.VISIBLE);
				break;
		}


	}


}
