package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.ContextMenu;
import com.easemob.helpdesk.activity.chat.ShowVideoActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.mvp.BaseChatActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.messagebody.HDVideoMessageBody;
import com.hyphenate.util.DateUtils;
import com.hyphenate.util.TextFormater;

import java.io.File;
import java.util.Locale;


/**
 * Created by tiancruyff on 2017/5/24.
 */

public class VideoViewHolder extends BaseViewHolder {
	private ImageView imageView;
	private TextView sizeView;
	private TextView timeLengthView;
	private ImageView playView;
	private TextView percentageView;
	private ProgressBar pb;


	public VideoViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		imageView = ((ImageView) findViewById(R.id.chatting_content_iv));
		sizeView = (TextView) findViewById(R.id.chatting_size_iv);
		timeLengthView = (TextView) findViewById(R.id.chatting_length_iv);
		playView = (ImageView) findViewById(R.id.chatting_status_btn);
		percentageView = (TextView) findViewById(R.id.percentage);
		pb = (ProgressBar) findViewById(R.id.progressBar);

	}

	@Override
	public void handleViewMessage(final HDMessage message, final int position) {
		onSetUpView(message);

		final HDVideoMessageBody videoMessageBody = (HDVideoMessageBody) message.getBody();

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBubbleClick(message);
			}
		});

		imageView.setOnLongClickListener(new View.OnLongClickListener() {
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
				activity.startActivityForResult(intent, BaseChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});

		RequestOptions requestOptions = RequestOptions.errorOf(R.drawable.default_image);
		requestOptions.override(CommonUtils.convertDip2Px(activity, 120));

		if (message.isServerMsg()) {
			requestOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
			Glide.with(activity).load(videoMessageBody.getThumbRemoteUrl())
					.apply(requestOptions)
					.into(imageView);
		} else {
			//发送方向
			if (message.direct() == HDMessage.Direct.SEND) {
				// 发送的消息
				Glide.with(activity).load(videoMessageBody.getLocalPath()).apply(requestOptions)
						.into(imageView);
				setMessageSendCallback(message);
			}

		}

		switch (message.getStatus()) {
			case SUCCESS:
				pb.setVisibility(View.GONE);
				percentageView.setVisibility(View.GONE);
				if (ivStatus != null)
					ivStatus.setVisibility(View.GONE);
				break;
			case FAIL:
				pb.setVisibility(View.GONE);
				percentageView.setVisibility(View.GONE);
				if (ivStatus != null)
					ivStatus.setVisibility(View.VISIBLE);
				break;
			case INPROGRESS:
				if (ivStatus != null)
					ivStatus.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);
				percentageView.setVisibility(View.VISIBLE);
				percentageView.setText(String.format(Locale.getDefault(), "%d%%", message.getProgress()));
				break;
			case CREATE:
				pb.setVisibility(View.GONE);
				percentageView.setVisibility(View.GONE);
				if (ivStatus != null){
					ivStatus.setVisibility(View.VISIBLE);
				}
				break;
			default:
		}
	}

	protected void onSetUpView(final HDMessage message) {
		HDVideoMessageBody videoBody = (HDVideoMessageBody) message.getBody();

		if (videoBody.getDuation() <= 0 || videoBody.getDuation() == 100) {
		} else {
			String time = DateUtils.toTime(videoBody.getDuation());
			timeLengthView.setText(time);
		}

		if (message.direct() == HDMessage.Direct.RECEIVE) {
			if (videoBody.getFileLenth() > 0) {
				String size = TextFormater.getDataSize(videoBody.getFileLenth());
				sizeView.setText(size);
			}
		} else {
			if (videoBody.getLocalPath() != null && new File(videoBody.getLocalPath()).exists()) {
				String size = TextFormater.getDataSize(new File(videoBody.getLocalPath()).length());
				sizeView.setText(size);
			}
		}
	}


	protected void onBubbleClick(final HDMessage message) {
		HDVideoMessageBody videoBody = (HDVideoMessageBody) message.getBody();
		Intent intent = new Intent(context, ShowVideoActivity.class);
		intent.putExtra("videomsg", message);
		intent.putExtra("localpath", videoBody.getLocalPath());
		intent.putExtra("secret", videoBody.getFileSecret());
		intent.putExtra("remotepath", videoBody.getRemoteUrl());

		activity.startActivity(intent);
	}

}
