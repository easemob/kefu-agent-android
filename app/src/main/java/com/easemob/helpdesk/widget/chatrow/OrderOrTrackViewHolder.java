package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.bean.MsgTypeOrderEntity;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.HDTextMessageBody;
import com.hyphenate.kefusdk.utils.JsonUtils;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class OrderOrTrackViewHolder extends BaseViewHolder {

	public ProgressBar pb;
	public ImageView ivStatus;
	public TextView tv;
	public TextView tvTitle;
	public TextView tvOrderTitle;
	public TextView tvDesc;
	public TextView tvPrice;
	public ImageView imgView;
	public LinearLayout lyView;
	public LinearLayout tvList;

	public OrderOrTrackViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		pb = findViewById(R.id.pb_sending);
		ivStatus = findViewById(R.id.msg_status);
		tv = findViewById(R.id.tv_chatcontent);
		tvTitle = findViewById(R.id.tvTitle);
		tvOrderTitle = findViewById(R.id.tvOrderTitle);
		tvDesc = findViewById(R.id.tvDesc);
		tvPrice = findViewById(R.id.tvPrice);
		imgView = findViewById(R.id.imgView);
		lyView = findViewById(R.id.ly_chatcontent);
		tvList = findViewById(R.id.ll_layout);
	}

	@Override
	public void handleViewMessage(HDMessage message, int position) {
		if (message.direct() == HDMessage.Direct.SEND) {
			// 设置内容
			SimpleCommonUtils.spannableEmoticonFilter(tv, CommonUtils.convertStringByMessageText(((HDTextMessageBody) message.getBody()).getMessage()));
		} else {
			MsgTypeOrderEntity entty = JsonUtils.getMsgOrderFromJson(message.getExtJson());
			if (entty != null) {
				if (tvTitle != null) {
					tvTitle.setText(entty.title);
				}
				if (entty.orderTitle != null) {
					tvOrderTitle.setText(entty.orderTitle);
				} else {
					tvOrderTitle.setVisibility(View.GONE);
				}
				tvDesc.setText(entty.desc);
				tvPrice.setText(entty.price);
				final String url = entty.itemRemoteUrl;
				lyView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (TextUtils.isEmpty(url)) {
							return;
						}
						Uri uri = Uri.parse(url);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						try {
							activity.startActivity(intent);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(activity, "无法打开", Toast.LENGTH_SHORT).show();
						}
					}
				});
				Glide.with(activity).load(entty.imgRemoteUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgView);
			}
		}

		if (message.direct() == HDMessage.Direct.SEND) {
			switch (message.getStatus()) {
				case SUCCESS:
					pb.setVisibility(View.GONE);
					ivStatus.setVisibility(View.GONE);
					break;
				case FAIL:
					pb.setVisibility(View.GONE);
					ivStatus.setVisibility(View.VISIBLE);
					break;
				case INPROGRESS:
					pb.setVisibility(View.VISIBLE);
					ivStatus.setVisibility(View.GONE);
					break;
			}
		}

	}


}
