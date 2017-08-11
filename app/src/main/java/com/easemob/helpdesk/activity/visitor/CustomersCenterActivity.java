package com.easemob.helpdesk.activity.visitor;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.CustomersListAdapter;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.CustomerEntity;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tiancruyff on 2017/7/24.
 */

public class CustomersCenterActivity extends BaseActivity{
	private static final String TAG = CustomersCenterActivity.class.getSimpleName();

	private static final int MSG_LOAD_MORE_DATA = 0x01;
	private static final int MSG_REFRESH_DATA = 0x02;
	private static final int MSG_AUTHENTICATION = 0x03;

	private static final int PER_PAGE_COUNT = 15;

	private static final int REQUEST_CODE_SCREENING = 16;

	private int total_count = 0;
	private int mCurPageNo;
	private WeakHandler mWeakHandler;

	@BindView(R.id.recyclerView)
	protected EasyRecyclerView recyclerView;

	private CustomersListAdapter mAdapter;

	private List<CustomerEntity.EntitiesBean> customers = new ArrayList<>();

	private Dialog pd;
	private String visitorName = "";
	private String userTagIds = "";
	private String userName = "";
	private String beginDate = "";
	private String endDate = "";

	private static class WeakHandler extends Handler {
		WeakReference<CustomersCenterActivity> weakReference;
		public WeakHandler(CustomersCenterActivity activity){
			this.weakReference = new WeakReference<CustomersCenterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			CustomersCenterActivity activity = weakReference.get();
			if (null != activity){
				switch (msg.what){
					case MSG_LOAD_MORE_DATA:
						activity.updateView((List<CustomerEntity.EntitiesBean>) msg.obj);
						break;
					case MSG_REFRESH_DATA:
						activity.refreshView((List<CustomerEntity.EntitiesBean>) msg.obj);
						break;
					case MSG_AUTHENTICATION:
						activity.recyclerView.setRefreshing(false);
						HDApplication.getInstance().logout();
						break;
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_customers_center);
		ButterKnife.bind(this);
		mWeakHandler = new WeakHandler(this);

		final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(mLayoutManager);
		//设置分割线
		DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
		dividerLine.setSize(1);
		dividerLine.setColor(0xFFDDDDDD);
		recyclerView.addItemDecoration(dividerLine);
		recyclerView.setAdapterWithProgress(mAdapter = new CustomersListAdapter(this));
		mAdapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				loadMoreMethod();
			}
		});
		mAdapter.setNoMore(R.layout.view_nomore);
		mAdapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				mAdapter.resumeMore();
			}
		});

		mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int i) {
				CustomerEntity.EntitiesBean bean = mAdapter.getItem(i);
				HDUser currentUser = HDClient.getInstance().getCurrentUser();
				if(currentUser == null) {
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("visitorId", bean.getUserId());
				intent.putExtra("userId", bean.getUserId());
				intent.putExtra("tenantId", currentUser.getTenantId());
				intent.putExtra("showContact", true);
				intent.setClass(CustomersCenterActivity.this, CustomerDetailActivity.class);
				startActivity(intent);
			}
		});

		recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadTheFirstPageData();
			}
		});

		TimeInfo defaultTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
		beginDate = DateUtils.getStartDateTimeString(defaultTimeInfo.getStartTime());
		endDate = DateUtils.getEndDateTimeString(defaultTimeInfo.getEndTime());

		loadTheFirstPageData();
	}

	@OnClick(R.id.rl_back)
	protected void onBackClick(View view) {
		finish();
	}

	@OnClick(R.id.iv_filter)
	protected void onFilterClick(View view) {
		Intent intent = new Intent();
		intent.setClass(CustomersCenterActivity.this, CustomersScreeningActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SCREENING);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_SCREENING) {
				visitorName = data.getStringExtra("cusName");
				userTagIds = data.getStringExtra("tagsId");
				userName = data.getStringExtra("cusId");
				beginDate = data.getStringExtra("beginDate");
				endDate = data.getStringExtra("endDate");
				loadTheFirstPageData();
			}
		}
	}

	private void loadMoreMethod() {
		HDClient.getInstance().visitorManager().getAgentCustomersInfo(getRequestString(mCurPageNo + 1), new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (isFinishing()) {
					return;
				}
				Gson gson = new Gson();
				CustomerEntity customerEntity = gson.fromJson(value, CustomerEntity.class);

				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_LOAD_MORE_DATA;
				if (customerEntity != null) {
					total_count = customerEntity.getTotalElements();
					message.obj = customerEntity.getEntities();
					if (customerEntity.getEntities().size() > 0) {
						mCurPageNo++;
					}
				}
				mWeakHandler.sendMessage(message);
			}

			@Override
			public void onError(int error, String errorMsg) {
				if (isFinishing()) {
					return;
				}
				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_REFRESH_DATA;
				mWeakHandler.sendMessage(message);
			}

			@Override
			public void onAuthenticationException() {
				if (isFinishing()) {
					return;
				}
				HDLog.e(TAG, "getAgentCustomersInfo-onAuthenticationException");
				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_AUTHENTICATION;
				mWeakHandler.sendMessage(message);
			}
		});
	}

	private void loadTheFirstPageData() {
		HDClient.getInstance().visitorManager().getAgentCustomersInfo(getRequestString(0), new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (isFinishing()) {
					return;
				}
				Gson gson = new Gson();
				CustomerEntity customerEntity = gson.fromJson(value, CustomerEntity.class);

				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_REFRESH_DATA;
				if (customerEntity != null) {
					total_count = customerEntity.getTotalElements();
					message.obj = customerEntity.getEntities();
					if (customerEntity.getEntities().size() > 0) {
						mCurPageNo = 0;
					}
				}
				mWeakHandler.sendMessage(message);
			}

			@Override
			public void onError(int error, String errorMsg) {
				if (isFinishing()) {
					return;
				}
				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_LOAD_MORE_DATA;
				mWeakHandler.sendMessage(message);
			}

			@Override
			public void onAuthenticationException() {
				if (isFinishing()) {
					return;
				}
				HDLog.e(TAG, "getAgentCustomersInfo-onAuthenticationException");
				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_AUTHENTICATION;
				mWeakHandler.sendMessage(message);
			}
		});
	}

	private String getRequestString(int page) {
		StringBuilder sb = new StringBuilder();
		sb.append("page=").append(page).append("&size=").append(PER_PAGE_COUNT)
				.append("userTagIds=").append(userTagIds).append("&categoryId=-1&subCategoryId=-1&visitorName=").append(visitorName)
				.append("&visitorUserId=&summaryIds=&enquirySummary=&beginDate=").append(beginDate).append("&endDate=").append(endDate)
				.append("&originType=&username=").append(userName);
		return sb.toString();
	}


	private void refreshView(List<CustomerEntity.EntitiesBean> data){
		if (null != data){
			mAdapter.clear();
			mAdapter.addAll(data);
			customers.clear();
			customers.addAll(data);
			if (data.size() < PER_PAGE_COUNT){
				mAdapter.stopMore();
			}
			mAdapter.pauseMore();
		} else {
			recyclerView.setRefreshing(false);
		}
	}


	private void updateView(List<CustomerEntity.EntitiesBean> data) {
		if (data != null) {
			if (data.size() == 0) {
				mAdapter.stopMore();
				return;
			}
			mAdapter.addAll(data);
			customers.addAll(data);
			if (data.size() < PER_PAGE_COUNT) {
				mAdapter.stopMore();
				return;
			}
			mAdapter.pauseMore();
		} else {
			recyclerView.setRefreshing(false);
		}
	}

	private void closeDialog(){
		if (pd != null && pd.isShowing()){
			pd.dismiss();
		}
	}

	@Override
	public void onDestroy() {
		mWeakHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
		closeDialog();
	}

}
