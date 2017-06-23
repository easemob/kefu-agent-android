package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.chat.ChatActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.fragment.main.CurrentSessionFragment;
import com.easemob.helpdesk.utils.DateUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDMessage;

import java.util.Date;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

	protected static final String TAG = "viewholder";
	public ImageView ivAvatar;
	public TextView timestamp;
	public ImageView ivStatus;
	public Activity activity;
	public Context context;
	public ChatAdapter adapter;
	private View itemView;

	public BaseViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(itemView);
		this.itemView = itemView;
		this.activity = activity;
		context = activity;
		adapter = chatAdapter;
		initView();
	}

	private void initView(){
		ivAvatar = (ImageView) itemView.findViewById(R.id.iv_userhead);
		timestamp = (TextView) itemView.findViewById(R.id.timestamp);
		ivStatus = (ImageView) itemView.findViewById(R.id.msg_status);
		onFindViewById();
	}

	public final void handleMessage(HDMessage message, final int position){
		handleViewMessage(message, position);
		if (message.direct() == HDMessage.Direct.SEND) {
			if (ivStatus != null) {
				//重发按钮点击事件
				ivStatus.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						//显示重发消息的自定义alertdialog
						Intent intent = new Intent(activity, AlertDialog.class);
						intent.putExtra("msg", "确认要重发吗？");
						intent.putExtra("title", "重发");
						intent.putExtra("cancel", true);
						intent.putExtra("position", position);
						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_RESEND);
					}
				});
			}
		}

		if (position == 0) {
			timestamp.setText(DateUtils.getTimestampString(new Date(message.getTimestamp())));
			timestamp.setVisibility(View.VISIBLE);
		} else {
			// 两条消息时间离得如果稍长，显示时间
			HDMessage prevMessage = adapter.getItem(position - 1);
			if (prevMessage != null && DateUtils.isCloseEnough(message.getTimestamp(), prevMessage.getTimestamp())) {
				timestamp.setVisibility(View.GONE);
			} else {
				timestamp.setText(DateUtils.getTimestampString(new Date(message.getTimestamp())));
				timestamp.setVisibility(View.VISIBLE);
			}
		}
	}

	public abstract void handleViewMessage(HDMessage message, final int position);

	/**
	 *
	 */
	protected abstract void onFindViewById();

	/**
	 * 通过xml查找相应的ID，通用方法
	 * @param id
	 * @param <T>
	 * @return
	 */
	protected <T extends View> T findViewById(@IdRes int id) {
		return (T) itemView.findViewById(id);
	}


	protected void setMessageSendCallback(final HDMessage message) {
		if (message.getMessageCallback() == null) {
			message.setMessageCallback(new HDDataCallBack() {
				@Override
				public void onSuccess(Object value) {
					if (activity.isFinishing()) {
						return;
					}
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							adapter.refresh();
							if (CurrentSessionFragment.refreshCallback != null) {
								CurrentSessionFragment.refreshCallback.onRefreshView();
							}
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

				@Override
				public void onProgress(int progress) {
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							adapter.refresh();
						}
					});
				}
			});
		}
	}

}
