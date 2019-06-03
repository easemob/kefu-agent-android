package com.easemob.helpdesk.activity.manager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.manager.RealtimeSuperviseAgentUsersAdapter;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.gsonmodel.manager.SuperviseAgentUsers;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by tiancruyff on 2017/12/5.
 */

public class ManagerRealtimeSuperviseAgentUsersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{
	private static final String TAG = "ManagerRealtimeSuperviseAgentUsersActivity";

	private static final int MSG_REFRESH = 0x01;
	private static final int MSG_REFRESH_DATA = 0x02;
	private static final int MSG_AUTHENTICATION = 0x03;

	@BindView(R.id.recyclerView)
	protected EasyRecyclerView recyclerView;
	@BindView(R.id.title)
	protected TextView title;
	@BindView(R.id.sort_text)
	protected TextView tvSortText;
	@BindView(R.id.sort_up_icon)
	protected ImageView ivSortIconUp;
	@BindView(R.id.sort_down_icon)
	protected ImageView ivSortIconDown;

	private static long refreshDelay = 60 * 1000; //60s

	private WeakHandler mWeakHandler;

	private RealtimeSuperviseAgentUsersAdapter adapter;

	private Unbinder unbinder;

	private Comparator sortDown = new Comparator<SuperviseAgentUsers.EntitiesBean>() {
		@Override
		public int compare(SuperviseAgentUsers.EntitiesBean o1, SuperviseAgentUsers.EntitiesBean o2) {
			if (o1 == null || o2 == null) {
				return 0;
			}
			int o1State = CommonUtils.getAgentStatus(o1.getState());
			int o2State = CommonUtils.getAgentStatus(o2.getState());
			if (o1State > o2State) {
				return -1;
			} else if (o1State < o2State) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	private Comparator sortUp = new Comparator<SuperviseAgentUsers.EntitiesBean>() {
		@Override
		public int compare(SuperviseAgentUsers.EntitiesBean o1, SuperviseAgentUsers.EntitiesBean o2) {
			if (o1 == null || o2 == null) {
				return 0;
			}
			int o1State = CommonUtils.getAgentStatus(o1.getState());
			int o2State = CommonUtils.getAgentStatus(o2.getState());
			if (o1State > o2State) {
				return 1;
			} else if (o1State < o2State) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.manage_activity_realtime_supervise_agent_users);
		unbinder = ButterKnife.bind(this);
		mWeakHandler = new WeakHandler(this);
		initView();
		loadTheFirstPageData();
	}

	private void loadTheFirstPageData() {
		Integer queueId = getIntent().getIntExtra("queueId", -1);

		if (queueId <= 0) {
			HDLog.e(TAG, "get queueId from intent value is abnormal, queueId = " + queueId);
			return;
		}

		HelpDeskManager.getInstance().getMonitorAgentUsersByQueueId(queueId, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (TextUtils.isEmpty(value)){
					return;
				}
				Gson gson = new Gson();
				SuperviseAgentUsers response = gson.fromJson(value, SuperviseAgentUsers.class);
				if (response == null){
					HDLog.e(TAG, "getMonitorAgentUsersByQueueId response is null");
					return;
				}
				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_REFRESH_DATA;
				message.obj = response.getEntities();
				mWeakHandler.sendMessage(message);
			}

			@Override
			public void onError(int error, String errorMsg) {

			}
		});
	}

	private void initView() {
		String queueName = getIntent().getStringExtra("queueName");
		if (!TextUtils.isEmpty(queueName)) {
			title.setText("现场管理-" + queueName);
		}

		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		//设置分割线
		DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
		dividerLine.setSize(1);
		dividerLine.setColor(0xFFDDDDDD);
		recyclerView.addItemDecoration(dividerLine);

		recyclerView.setAdapterWithProgress(adapter = new RealtimeSuperviseAgentUsersAdapter(this));

		adapter.setNoMore(R.layout.view_nomore);
		adapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				adapter.resumeMore();
			}
		});
		recyclerView.setRefreshListener(this);
	}

	private static class WeakHandler extends Handler {
		WeakReference<ManagerRealtimeSuperviseAgentUsersActivity> weakReference;
		public WeakHandler(ManagerRealtimeSuperviseAgentUsersActivity activity){
			this.weakReference = new WeakReference<ManagerRealtimeSuperviseAgentUsersActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			ManagerRealtimeSuperviseAgentUsersActivity activity = weakReference.get();
			if (null != activity){
				switch (msg.what){
					case MSG_REFRESH:
						activity.onRefresh();
						break;
					case MSG_REFRESH_DATA:
						activity.refreshView((List<SuperviseAgentUsers.EntitiesBean>)msg.obj);
						break;
					case MSG_AUTHENTICATION:
						activity.recyclerView.setRefreshing(false);
						HDApplication.getInstance().logout();
						break;
				}
			}
		}
	}

	private void refreshView(List<SuperviseAgentUsers.EntitiesBean> entitiesBeans) {
		if (null != entitiesBeans){
			adapter.clear();
			adapter.addAll(entitiesBeans);
			adapter.stopMore();
			adapter.pauseMore();
			if (ivSortIconUp != null) {
				if (ivSortIconUp.getVisibility() == View.VISIBLE) {
					adapter.sort(sortUp);
				} else {
					adapter.sort(sortDown);
				}
			}
		} else {
			if (recyclerView != null) {
				recyclerView.setRefreshing(false);
			}
		}

		if (mWeakHandler.hasMessages(MSG_REFRESH)) {
			mWeakHandler.removeMessages(MSG_REFRESH);
		}

		Message message = mWeakHandler.obtainMessage();
		message.what = MSG_REFRESH;
		mWeakHandler.sendMessageDelayed(message, refreshDelay);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (unbinder != null){
			unbinder.unbind();
		}
		if (mWeakHandler != null && mWeakHandler.hasMessages(MSG_REFRESH)) {
			mWeakHandler.removeMessages(MSG_REFRESH);
		}
	}

	@Override
	public void onRefresh() {
		loadTheFirstPageData();
	}

	@OnClick(R.id.rl_back)
	public void onClickByBack(View view) {
		finish();
	}

	@OnClick(R.id.sort_layout)
	public void onClickBySort(View view) {
		if (ivSortIconDown.getVisibility() == View.VISIBLE) {
			ivSortIconUp.setVisibility(View.VISIBLE);
			ivSortIconDown.setVisibility(View.GONE);
			adapter.sort(sortUp);
		} else {
			ivSortIconUp.setVisibility(View.GONE);
			ivSortIconDown.setVisibility(View.VISIBLE);
			adapter.sort(sortDown);
		}
	}

}
