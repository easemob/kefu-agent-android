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
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.gsonmodel.customer.CustomerEntity;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.option.CustomersCenterScreenEntity;
import com.hyphenate.kefusdk.entity.user.HDUser;
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


	private static final int REQUEST_CODE_SCREENING = 16;

	private int mCurPageNo;
	private WeakHandler mWeakHandler;

	@BindView(R.id.recyclerView)
	protected EasyRecyclerView recyclerView;

	private CustomersListAdapter mAdapter;

	private List<CustomerEntity.EntitiesBean> customers = new ArrayList<>();

	private Dialog pd;

	private CustomersCenterScreenEntity customersCenterScreenEntity = new CustomersCenterScreenEntity();

	private TimeInfo currentTimeInfo;

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

		currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();

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

				if (bean.getBind_visitors() == null || bean.getBind_visitors().size() <= 0) {
					return;
				}

				Intent intent = new Intent();
				intent.putExtra("userId", bean.getBind_visitors().get(0));
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
		customersCenterScreenEntity.timeRange.setStartTime(defaultTimeInfo.getStartTime());
		customersCenterScreenEntity.timeRange.setEndTime(defaultTimeInfo.getEndTime());

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
		intent.putExtra("timeinfo", currentTimeInfo);
		startActivityForResult(intent, REQUEST_CODE_SCREENING);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_SCREENING) {
				customersCenterScreenEntity.visitorName = data.getStringExtra("cusName");
				customersCenterScreenEntity.userTagIds = data.getStringExtra("tagsId");
				customersCenterScreenEntity.userName = data.getStringExtra("cusId");
				currentTimeInfo.setStartTime(data.getLongExtra("beginDate", -1));
				currentTimeInfo.setEndTime(data.getLongExtra("endDate", -1));
				if (!(currentTimeInfo.getStartTime() == -1 || currentTimeInfo.getEndTime() == -1)) {
					customersCenterScreenEntity.timeRange.setStartTime(currentTimeInfo.getStartTime());
					customersCenterScreenEntity.timeRange.setEndTime(currentTimeInfo.getEndTime());
				}
				loadTheFirstPageData();
			}
		}
	}

	private void loadMoreMethod() {
		HDClient.getInstance().visitorManager().getAgentCustomersInfo(mCurPageNo + 1, customersCenterScreenEntity, new HDDataCallBack<List<CustomerEntity.EntitiesBean>>() {
			@Override
			public void onSuccess(List<CustomerEntity.EntitiesBean> value) {
				if (isFinishing()) {
					return;
				}
				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_LOAD_MORE_DATA;
				message.obj = value;
				if (value.size() > 0) {
					mCurPageNo++;
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
		HDClient.getInstance().visitorManager().getAgentCustomersInfo(0, customersCenterScreenEntity, new HDDataCallBack<List<CustomerEntity.EntitiesBean>>() {
			@Override
			public void onSuccess(List<CustomerEntity.EntitiesBean> value) {
				if (isFinishing()) {
					return;
				}

				Message message = mWeakHandler.obtainMessage();
				message.what = MSG_REFRESH_DATA;
				message.obj = value;
				if (value.size() > 0) {
					mCurPageNo = 0;
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

	private void refreshView(List<CustomerEntity.EntitiesBean> data){
		if (null != data){
			mAdapter.clear();
			mAdapter.addAll(data);
			customers.clear();
			customers.addAll(data);
			if (data.size() < customersCenterScreenEntity.PER_PAGE_COUNT){
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
			if (data.size() < customersCenterScreenEntity.PER_PAGE_COUNT) {
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
		if (mWeakHandler != null) {
			mWeakHandler.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
		closeDialog();
	}

}
