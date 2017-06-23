package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.chat.ChatActivity;
import com.easemob.helpdesk.widget.ContextMenu;
import com.easemob.helpdesk.activity.chat.ShowBigImageActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.BubbleImageView;
import com.hyphenate.kefusdk.entity.HDImageMessageBody;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.Locale;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class ImageViewHolder extends BaseViewHolder {
	public BubbleImageView bImageView;
	public TextView tvPercentage;
	public ProgressBar pb;
	public ImageView ivStatus;
	public TextView tvUserNick;

	public ImageViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		bImageView = findViewById(R.id.iv_sendPicture);
		tvPercentage = findViewById(R.id.percentage);
		pb = findViewById(R.id.progressBar);
		ivStatus = findViewById(R.id.msg_status);
		tvUserNick = findViewById(R.id.tv_userid);
	}

	@Override
	public void handleViewMessage(final HDMessage message, final int position) {
		final HDImageMessageBody imgBody = (HDImageMessageBody) message.getBody();
		if (imgBody == null) {
			HDLog.e("imageViewHolder", "imgBody is null");
			return;
		}

		bImageView.setOnLongClickListener(new View.OnLongClickListener() {
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
		bImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(activity, ShowBigImageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("message", message);
				activity.startActivity(intent);
			}
		});
		if (message.isServerMsg()) {
			Glide.with(activity).load(imgBody.getThumbnailUrl())
					.diskCacheStrategy(DiskCacheStrategy.SOURCE).error(R.drawable.default_image)
					.override(CommonUtils.convertDip2Px(activity, 120), CommonUtils.convertDip2Px(activity, 120))
					.into(bImageView);
		} else {
			//发送方向
			if (message.direct() == HDMessage.Direct.SEND) {
				// 发送的消息
				Glide.with(activity).load(imgBody.getLocalPath()).error(R.drawable.default_image)
						.override(CommonUtils.convertDip2Px(activity, 120), CommonUtils.convertDip2Px(activity, 120))
						.into(bImageView);
				setMessageSendCallback(message);
			}

		}

		switch (message.getStatus()) {
			case SUCCESS:
				pb.setVisibility(View.GONE);
				tvPercentage.setVisibility(View.GONE);
				if (ivStatus != null)
					ivStatus.setVisibility(View.GONE);
				break;
			case FAIL:
				pb.setVisibility(View.GONE);
				tvPercentage.setVisibility(View.GONE);
				if (ivStatus != null)
					ivStatus.setVisibility(View.VISIBLE);
				break;
			case INPROGRESS:
				if (ivStatus != null)
					ivStatus.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);
				tvPercentage.setVisibility(View.VISIBLE);
				tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", message.getProgress()));
				break;
			default:
		}
	}



}
