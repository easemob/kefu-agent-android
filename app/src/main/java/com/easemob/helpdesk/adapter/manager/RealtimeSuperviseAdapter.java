package com.easemob.helpdesk.adapter.manager;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.ManagerRealtimeSuperviseAgentUsersActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.gsonmodel.manager.SuperviseAgentQueues;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by tiancruyff on 2017/12/5.
 */

public class RealtimeSuperviseAdapter extends RecyclerArrayAdapter<SuperviseAgentQueues.EntitiesBean> {
	private Context mContext;

	public RealtimeSuperviseAdapter(Context context) {
		super(context);
		this.mContext = context;
	}

	@Override
	public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
		return new ManageRealtimeSuperviseAgentQueueHolder(parent);
	}

	class ManageRealtimeSuperviseAgentQueueHolder extends BaseViewHolder<SuperviseAgentQueues.EntitiesBean> {
		TextView queueName;
		TextView waitingCount;
		TextView receptCount;
		View checkDetail;

		View onlineGraph;
		TextView onlineNum;
		View busyGraph;
		TextView busyNum;
		View leaveGraph;
		TextView leaveNum;
		View hidingGraph;
		TextView hidingNum;
		View offlineGraph;
		TextView offlineNum;

		public ManageRealtimeSuperviseAgentQueueHolder(ViewGroup itemView) {
			super(itemView, R.layout.manage_supervise_agentqueues);
			queueName = $(R.id.queue_name);
			waitingCount = $(R.id.supervise_waiting_count);
			receptCount = $(R.id.supervise_current_recept_count);
			checkDetail = $(R.id.check_detail);

			onlineGraph = $(R.id.online_graph);
			onlineNum = $(R.id.online_num);
			busyGraph = $(R.id.busy_graph);
			busyNum = $(R.id.busy_num);
			leaveGraph = $(R.id.leave_graph);
			leaveNum = $(R.id.leave_num);
			hidingGraph = $(R.id.hiding_graph);
			hidingNum = $(R.id.hiding_num);
			offlineGraph = $(R.id.offline_graph);
		    offlineNum = $(R.id.offline_num);
		}

		@Override
		public void setData(final SuperviseAgentQueues.EntitiesBean data) {
			super.setData(data);
			if (data == null) {
				return;
			}

			if (queueName != null) {
				queueName.setText(data.getQueue_name());
			}

			if (waitingCount != null) {
				waitingCount.setText(data.getSession_wait_count() + "人");
			}

			if (receptCount != null) {
				receptCount.setText(data.getCurrent_session_count() + "人/" + data.getMax_session_count() + "人");
			}


			if (checkDetail != null) {
				checkDetail.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, ManagerRealtimeSuperviseAgentUsersActivity.class);
						intent.putExtra("queueId", data.getQueue_id());
						intent.putExtra("queueName", data.getQueue_name());
						mContext.startActivity(intent);
					}
				});
			}

			if (onlineGraph != null) {
				onlineGraph.setLayoutParams(new LinearLayout.LayoutParams(0, CommonUtils.convertDip2Px(mContext, 8), data.getIdle_count()));
			}

			if (onlineNum != null) {
				onlineNum.setText(String.valueOf(data.getIdle_count()));
			}

			if (busyGraph != null) {
				busyGraph.setLayoutParams(new LinearLayout.LayoutParams(0, CommonUtils.convertDip2Px(mContext, 8), data.getBusy_count()));
			}

			if (busyNum != null) {
				busyNum.setText(String.valueOf(data.getBusy_count()));
			}

			if (leaveGraph != null) {
				leaveGraph.setLayoutParams(new LinearLayout.LayoutParams(0, CommonUtils.convertDip2Px(mContext, 8), data.getLeave_count()));
			}

			if (leaveNum != null) {
				leaveNum.setText(String.valueOf(data.getLeave_count()));
			}

			if (hidingGraph != null) {
				hidingGraph.setLayoutParams(new LinearLayout.LayoutParams(0, CommonUtils.convertDip2Px(mContext, 8), data.getHidden_count()));
			}

			if (hidingNum != null) {
				hidingNum.setText(String.valueOf(data.getHidden_count()));
			}

			if (offlineGraph != null) {
				offlineGraph.setLayoutParams(new LinearLayout.LayoutParams(0, CommonUtils.convertDip2Px(mContext, 8), data.getOffline_count()));
			}

			if (offlineNum != null) {
				offlineNum.setText(String.valueOf(data.getOffline_count()));
			}
		}
	}
}
