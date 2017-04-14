package com.easemob.helpdesk.widget.chatrow;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.util.DensityUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liyuzhao on 10/04/2017.
 */

public class RobotMenuViewHolder extends BaseViewHolder {

	public ProgressBar pb;
	public ImageView ivStatus;
	public TextView tv;
	public TextView tvTitle;
	public LinearLayout tvList;

	public RobotMenuViewHolder(Activity activity, ChatAdapter chatAdapter, View itemView) {
		super(activity, chatAdapter, itemView);
	}

	@Override
	protected void onFindViewById() {
		pb = findViewById(R.id.pb_sending);
		ivStatus = findViewById(R.id.msg_status);
		tv = findViewById(R.id.tv_chatcontent);
		tvTitle = findViewById(R.id.tvTitle);
		tvList = findViewById(R.id.ll_layout);
	}


	@Override
	public void handleViewMessage(HDMessage message, int position) {
		if (message.direct() == HDMessage.Direct.SEND) {
			try {
				pb.setVisibility(View.GONE);
				ivStatus.setVisibility(View.GONE);
			} catch (Exception ignored) {
			}
			try {
				JSONObject jsonMsgType = message.getExtJson().getJSONObject("msgtype");
				JSONObject jsonChoice = jsonMsgType.getJSONObject("choice");
				if (jsonChoice.has("title") && !jsonChoice.isNull("title")) {
					tvTitle.setText(jsonChoice.getString("title"));
				}
				if (jsonChoice.has("items")) {
					setRobotMenuListMessageLayout(tvList, jsonChoice.getJSONArray("items"));
				} else if (jsonChoice.has("list")) {
					setRobotMenuMessagesLayout(tvList, jsonChoice.getJSONArray("list"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}


		} else {
			try {
				tv.setText("机器人菜单消息");
			} catch (Exception ignored) {
			}
		}
	}



	private void setRobotMenuListMessageLayout(LinearLayout parentView, JSONArray jsonArr) {
		try {
			parentView.removeAllViews();
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject itemJson = jsonArr.getJSONObject(i);
				final String itemStr = itemJson.getString("name");
//                final String itemId = itemJson.getString("id");
				final TextView textView = new TextView(context);
				textView.setText(itemStr);
				textView.setTextSize(15);
				textView.setTextColor(Color.parseColor("#ED5485"));
				LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				llLp.bottomMargin = DensityUtil.dip2px(context, 3);
				llLp.topMargin = DensityUtil.dip2px(context, 3);
				parentView.addView(textView, llLp);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void setRobotMenuMessagesLayout(LinearLayout parentView, JSONArray jsonArr) {
		try {
			parentView.removeAllViews();
			for (int i = 0; i < jsonArr.length(); i++) {
				final String itemStr = jsonArr.getString(i);
				final TextView textView = new TextView(context);
				textView.setText(itemStr);
				textView.setTextSize(15);
				textView.setTextColor(Color.parseColor("#ED5485"));
				LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				llLp.bottomMargin = DensityUtil.dip2px(context, 3);
				llLp.topMargin = DensityUtil.dip2px(context, 3);
				parentView.addView(textView, llLp);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



}
