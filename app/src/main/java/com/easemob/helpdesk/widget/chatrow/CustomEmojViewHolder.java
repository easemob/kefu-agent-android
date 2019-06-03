package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.ContextMenu;
import com.easemob.helpdesk.activity.chat.ShowBigImage;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.mvp.BaseChatActivity;
import com.hyphenate.kefusdk.chat.EmojiconManager;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.messagebody.HDTextMessageBody;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.MessageUtils;
import com.hyphenate.util.EMLog;

import java.io.File;
import java.util.Locale;

/**
 * Created by tiancruyff on 2017/12/8.
 */

public class CustomEmojViewHolder extends BaseViewHolder {
	public ImageView bImageView;
	public TextView tvPercentage;
	public ProgressBar pb;
	public ImageView ivStatus;
	public TextView tvUserNick;

	public CustomEmojViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
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
		if (!(message.getBody() instanceof HDTextMessageBody)){
			HDLog.e("CustomEmojViewHolder", "body is not HDTextMessageBody");
			return;
		}

		String remoteUrl = MessageUtils.getCustomEmojMessageRemoteUrl(message);

		if (remoteUrl == null) {
			HDLog.e("CustomEmojViewHolder", "remoteUrl is null");
			return;
		}

		EmojiconManager.EmojiconEntity emojiconEntity = HDClient.getInstance().emojiManager().getEmojicon(remoteUrl);
		if (bImageView == null){
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
				activity.startActivityForResult(intent, BaseChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});
		bImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(activity, ShowBigImage.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("message", message);
				intent.putExtra("showCustomEmoj", true);
				activity.startActivity(intent);
			}
		});

		if (emojiconEntity == null) {
			HDLog.e("CustomEmojViewHolder", "emojiconEntity is null");
			Glide.with(activity).load(remoteUrl)
					.apply(RequestOptions.errorOf(R.drawable.default_image).diskCacheStrategy(DiskCacheStrategy.RESOURCE))
					.into(bImageView);
		} else {
			if (!TextUtils.isEmpty(emojiconEntity.origin.remoteUrl)) {
				File localOrigin = new File(emojiconEntity.origin.localUrl);
				if (localOrigin.exists()) {
					Glide.with(activity).load(emojiconEntity.origin.localUrl)
							.apply(RequestOptions.placeholderOf(R.drawable.hd_default_image))
							.into(bImageView);
				} else {
					Glide.with(activity).load(emojiconEntity.origin.remoteUrl)
							.apply(RequestOptions.placeholderOf(R.drawable.hd_default_image))
							.into(bImageView);
				}
			} else if (!TextUtils.isEmpty(emojiconEntity.thumbnail.remoteUrl)) {
				File localThumb = new File(emojiconEntity.thumbnail.localUrl);
				if (localThumb.exists()) {
					Glide.with(activity).load(emojiconEntity.thumbnail.localUrl)
							.apply(RequestOptions.placeholderOf(R.drawable.hd_default_image))
							.into(bImageView);
				} else {
					Glide.with(activity).load(emojiconEntity.thumbnail.remoteUrl)
							.apply(RequestOptions.placeholderOf(R.drawable.hd_default_image))
							.into(bImageView);
				}
			} else {
				EMLog.e(TAG, "emojiconEntity date wrong");
				Glide.with(activity).load(remoteUrl)
						.apply(RequestOptions.errorOf(R.drawable.default_image).diskCacheStrategy(DiskCacheStrategy.RESOURCE))
						.into(bImageView);
			}
		}
		if (message.direct() == HDMessage.Direct.SEND) {
			message.setStatus(HDMessage.Status.SUCCESS);
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
			case CREATE:
				pb.setVisibility(View.GONE);
				tvPercentage.setVisibility(View.GONE);
				if (ivStatus != null)
					ivStatus.setVisibility(View.VISIBLE);
				break;
			default:
		}

	}


}
