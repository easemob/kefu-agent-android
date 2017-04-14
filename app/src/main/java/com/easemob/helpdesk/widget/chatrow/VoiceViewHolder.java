package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.ChatActivity;
import com.easemob.helpdesk.activity.ContextMenu;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.recorder.MediaManager;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.HDVoiceMessageBody;
import com.hyphenate.kefusdk.utils.HDLog;

import java.io.File;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class VoiceViewHolder extends BaseViewHolder {

	public View lengthView;
	public TextView tvLength;
	public ProgressBar pb;
	public ImageView ivStatus;
	public TextView tvNick;
	public ImageView ivReadStatus;

	public VoiceViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		lengthView = findViewById(R.id.id_recorder_length);
		tvLength = findViewById(R.id.tv_length);
		pb = findViewById(R.id.pb_sending);
		ivStatus = findViewById(R.id.msg_status);
		tvNick = findViewById(R.id.tv_userid);
		ivReadStatus = findViewById(R.id.iv_unread_voice);
	}

	@Override
	public void handleViewMessage(final HDMessage message, final int position) {
		final HDVoiceMessageBody voiceBody = (HDVoiceMessageBody) message.getBody();
		if (voiceBody == null) {
			return;
		}
		int len = voiceBody.getVoiceLength();
		if (len > 0) {
			tvLength.setText(len + "\"");
			if (len == 100) {
				tvLength.setVisibility(View.INVISIBLE);
			} else {
				tvLength.setVisibility(View.VISIBLE);
			}
		} else {
			tvLength.setVisibility(View.INVISIBLE);
		}

		if (len != 100) {
			ViewGroup.LayoutParams layoutParams = lengthView.getLayoutParams();
			layoutParams.width = (int) (adapter.mMinItemWidth + Math.min(adapter.mMaxItemWidth / 60f * len, adapter.mMaxItemWidth));
		} else {
			ViewGroup.LayoutParams layoutParams = lengthView.getLayoutParams();
			layoutParams.width = (int) (adapter.mMinItemWidth + Math.min(adapter.mMaxItemWidth / 60f * len, adapter.mMaxItemWidth));
		}

		lengthView.setOnLongClickListener(new View.OnLongClickListener() {
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


		final boolean isSend = (message.direct() == HDMessage.Direct.SEND);
		if (!isSend) {
			HDLog.d(TAG, "it is receive msg");
			if (!TextUtils.isEmpty(voiceBody.getLocalPath())) {
				File file = new File(voiceBody.getLocalPath());
				if (!file.exists()) {
					if (message.getStatus() == HDMessage.Status.SUCCESS) {
						downloadVoiceFile(message);
					}
				}
			}
			if (message.isListened()) {
				ivReadStatus.setVisibility(View.GONE);
			} else {
				ivReadStatus.setVisibility(View.VISIBLE);
			}
		}

		lengthView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickVoiceItem(v, isSend, message, voiceBody);
			}
		});

		if (isSend) {
			setMessageSendCallback(message);
		}
		switch (message.getStatus()) {
			case SUCCESS:
				pb.setVisibility(View.GONE);
				if (ivStatus != null) {
					ivStatus.setVisibility(View.GONE);
				}
				break;
			case FAIL:
				if (pb != null) {
					pb.setVisibility(View.GONE);
				}
				if (ivStatus != null) {
					ivStatus.setVisibility(View.VISIBLE);
				}
				break;
			case INPROGRESS:
				if (pb != null) {
					pb.setVisibility(View.VISIBLE);
				}
				break;
		}

	}



	private void downloadVoiceFile(final HDMessage message) {
		if (message.getMessageCallback() != null) {
			message.setMessageCallback(new HDDataCallBack() {
				@Override
				public void onSuccess(Object value) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.refresh();
						}
					});
				}

				@Override
				public void onError(int error, String errorMsg) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.refresh();
						}
					});
				}
			});
		}
		HDClient.getInstance().chatManager().downloadAttachment(message);
	}

	private void clickVoiceItem(View v, final boolean isSend, final HDMessage message, HDVoiceMessageBody voiceBody) {
		if (message.getStatus() == HDMessage.Status.SUCCESS || message.getStatus() == HDMessage.Status.FAIL || isSend) {
			File file = new File(voiceBody.getLocalPath());
			if (file.exists() && file.isFile()) {
				playVoiceItem(v, isSend, message, voiceBody);
			} else {
				if (message.getStatus() == HDMessage.Status.SUCCESS || message.getStatus() == HDMessage.Status.FAIL) {
					if (message.getMessageCallback() == null) {
						message.setMessageCallback(new HDDataCallBack() {
							@Override
							public void onSuccess(Object value) {
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										adapter.refresh();
									}
								});
							}

							@Override
							public void onError(int error, String errorMsg) {
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(activity, "语音下载失败！", Toast.LENGTH_SHORT).show();
										adapter.refresh();
									}
								});
							}
						});
					}
					Toast.makeText(activity, "正在下载请稍后...", Toast.LENGTH_SHORT).show();
					HDClient.getInstance().chatManager().downloadAttachment(message);
					HDLog.e(TAG, "file not exist");
				}


			}
		} else if (message.getStatus() == HDMessage.Status.INPROGRESS) {
			Toast.makeText(activity, "下载中...", Toast.LENGTH_SHORT).show();
		}

	}


	private void playVoiceItem(View v, final boolean isSend, HDMessage message, HDVoiceMessageBody voiceBody) {
		//播放动画
		if (adapter.animView != null) {
			if (isSend) {
				adapter.animView.setBackgroundResource(R.drawable.icon_audio_blue_3);
			} else {
				adapter.animView.setBackgroundResource(R.drawable.icon_audio_white_3);
			}
			adapter.animView = null;
		}

		adapter.animView = v.findViewById(R.id.id_recorder_anim);
		if (isSend) {
			adapter.animView.setBackgroundResource(R.drawable.voice_to_icon);
		} else {
			adapter.animView.setBackgroundResource(R.drawable.voice_from_icon);
			if (!message.isListened()) {
				ivReadStatus.setVisibility(View.GONE);
				message.setListened(true);
				HDClient.getInstance().chatManager().setMessageListened(message);

			}
		}
		AnimationDrawable anim = (AnimationDrawable) adapter.animView.getBackground();
		anim.start();

		//播放音频
		MediaManager.playSound(activity, voiceBody.getLocalPath(), new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (isSend) {
					adapter.animView.setBackgroundResource(R.drawable.icon_audio_blue_3);
				} else {
					adapter.animView.setBackgroundResource(R.drawable.icon_audio_white_3);
				}
			}
		});

	}
}
