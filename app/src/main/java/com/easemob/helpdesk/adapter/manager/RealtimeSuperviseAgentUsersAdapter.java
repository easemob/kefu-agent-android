package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.pickerview.AgentStatusPickerView;
import com.hyphenate.kefusdk.gsonmodel.manager.SuperviseAgentUsers;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by tiancruyff on 2017/12/5.
 */

public class RealtimeSuperviseAgentUsersAdapter extends RecyclerArrayAdapter<SuperviseAgentUsers.EntitiesBean> {
	private Context mContext;
	private AgentStatusPickerView agentStatusPickerView;


	public RealtimeSuperviseAgentUsersAdapter(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
		return new RealtimeSuperviseAgentUsersViewHolder(parent);
	}

	class RealtimeSuperviseAgentUsersViewHolder extends BaseViewHolder<SuperviseAgentUsers.EntitiesBean> {
		TextView nickName;
		TextView currentSessionCount;
		TextView endSessionCount;
		TextView avgSessionTime;
		TextView firstLoginTime;
		TextView tvStatus;
		ImageView ivStatus;
		RelativeLayout userStatusLayout;

		RealtimeSuperviseAgentUsersViewHolder(ViewGroup itemView) {
			super(itemView, R.layout.manage_supervise_agentusers);
			nickName = $(R.id.nickname);
			tvStatus = $(R.id.user_status);
			ivStatus = $(R.id.iv_userstatus);
			currentSessionCount = $(R.id.current_session_count);
			endSessionCount = $(R.id.end_session_count);
			avgSessionTime = $(R.id.avg_session_time);
			firstLoginTime = $(R.id.today_first_login_time);
			userStatusLayout = $(R.id.user_status_layout);
		}

		@Override
		public void setData(final SuperviseAgentUsers.EntitiesBean data) {
			super.setData(data);
			if (data == null) {
				return;
			}

			if (nickName != null) {
				nickName.setText(data.getNickname());
			}
			if (currentSessionCount != null) {
				currentSessionCount.setText(data.getCurrent_session_count() + "人/" + data.getMax_session_count() + "人");
			}
			if (endSessionCount != null) {
				endSessionCount.setText(String.valueOf(data.getSession_terminal_count() + "条"));
			}
			if (avgSessionTime != null) {
				if (data.getAvg_session_time() > 0) {
					avgSessionTime.setText(String.valueOf(data.getAvg_session_time()) + "秒");
				} else {
					avgSessionTime.setText("0");
				}
			}
			if (firstLoginTime != null) {
				if (data.getFirst_login_time_of_today() != null) {
					firstLoginTime.setText(data.getFirst_login_time_of_today().toString());
				} else {
					firstLoginTime.setText("---");
				}
			}
			if (!TextUtils.isEmpty(data.getState())) {
				String status = data.getState();
				if (tvStatus != null) {
					CommonUtils.setAgentStatusTextView(tvStatus, status);
				}

				if (ivStatus != null) {
					CommonUtils.setAgentStatusView(ivStatus, status);
				}

				if (!status.equals("Offline")) {
					userStatusLayout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mContext != null) {
								if (agentStatusPickerView == null) {
									agentStatusPickerView = new AgentStatusPickerView(mContext, data.getUser_id());
								} else {
									agentStatusPickerView.setAgentId(data.getUser_id());
								}
								agentStatusPickerView.setCancelable(true);
								agentStatusPickerView.show();
							}
						}
					});
				}
			}
		}
	}
}
