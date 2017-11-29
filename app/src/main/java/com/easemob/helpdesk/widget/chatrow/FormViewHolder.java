package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.hyphenate.kefusdk.entity.HDMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiancruyff on 2017/11/1.
 */

public class FormViewHolder extends BaseViewHolder {
	public ProgressBar pb;
	private LinearLayout bubbleView;
	private TextView titleView;
	private TextView contentView;

	public FormViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		pb = findViewById(R.id.pb_sending);
		bubbleView = findViewById(R.id.bubble);
		titleView = findViewById(R.id.tv_form_title);
		contentView = findViewById(R.id.tv_form_content);
	}

	@Override
	public void handleViewMessage(HDMessage message, int position) {
		JSONObject jsonExt = message.getExtJson();
		if (jsonExt == null || !jsonExt.has("msgtype")) {
			return;
		}
		try {
			String type = jsonExt.getString("type");
			if (!type.equals("html/form")){
				return;
			}
			JSONObject html = jsonExt.getJSONObject("msgtype").getJSONObject("html");
			final String targetUrl = html.getString("url");
			String topic = html.getString("topic");
			String desc = html.getString("desc");
			if (!TextUtils.isEmpty(topic)) {
				if (titleView != null) {
					titleView.setText(topic);
				}
			}

			if (!TextUtils.isEmpty(desc)) {
				if (contentView != null) {
					contentView.setText(desc);
				}
			}

			if (!TextUtils.isEmpty(targetUrl)) {
				bubbleView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						Uri content_url = Uri.parse(targetUrl);
						intent.setData(content_url);
						context.startActivity(intent);
					}
				});
			}

			if (ivStatus != null) {
				ivStatus.setVisibility(View.GONE);
			}

			if (pb != null) {
				pb.setVisibility(View.GONE);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
