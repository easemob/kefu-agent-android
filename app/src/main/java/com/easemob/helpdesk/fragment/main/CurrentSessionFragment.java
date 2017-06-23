package com.easemob.helpdesk.fragment.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.chat.ChatActivity;
import com.easemob.helpdesk.activity.main.LoginActivity;
import com.easemob.helpdesk.activity.main.MainActivity;
import com.easemob.helpdesk.adapter.CurrentSessionAdapter;
import com.easemob.helpdesk.listener.OnItemClickListener;
import com.easemob.helpdesk.utils.AvatarUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.EMToast;
import com.easemob.helpdesk.utils.OnFreshCallbackListener;
import com.easemob.helpdesk.utils.OnRefreshViewListener;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.kefusdk.HDConnectionListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.HDSession;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.manager.CloseSessionManager;
import com.hyphenate.kefusdk.manager.CurrentSessionManager;
import com.hyphenate.kefusdk.manager.OverTimeSessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.util.NetUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 05/04/2017.
 */

public class CurrentSessionFragment extends Fragment implements OnRefreshViewListener, SwipeRefreshLayout.OnRefreshListener, CloseSessionManager.CloseSessionListener, OverTimeSessionManager.OverTimeSessionListener, OnFreshCallbackListener {
	private static final String TAG = CurrentSessionFragment.class.getSimpleName();

	@BindView(R.id.list)
	public RecyclerView mRecyclerView;
	@BindView(R.id.query)
	public EditText query;
	@BindView(R.id.search_clear)
	public ImageButton clearSearch;
	@BindView(R.id.iv_setting)
	public ImageView settting;
	@BindView(R.id.tv_session_count)
	public TextView tvSessionCount;
	@BindView(R.id.swipe_layout)
	public SwipeRefreshLayout mSwipeLayout;
	@BindView(R.id.emptyView)
	public View emptyView;
	@BindView(R.id.rl_error_item)
	public RelativeLayout errorItem;
	@BindView(R.id.iv_avatar)
	protected ImageView ivAvatar;
	@BindView(R.id.iv_status)
	protected ImageView ivStatus;


	public static OnFreshCallbackListener callback= null;
	public static OnRefreshViewListener refreshCallback = null;

	public TextView errorText;

	private CurrentSessionAdapter adapter;
	private InputMethodManager inputMethodManager;
	private HDConnectionListener connectionListener;
	private int currentSessionCount= 0;
	private HDUser currentUser;

	private Unbinder unbinder;



	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_currentsession, null);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		currentUser = HDClient.getInstance().getCurrentUser();
		callback = this;
		refreshCallback = this;
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);
		//创建一个线性布局管理器
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
		//设置布局管理器
		mRecyclerView.setLayoutManager(layoutManager);
		//设置分割线
		DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
		dividerLine.setSize(1);
		dividerLine.setColor(0xFFDDDDDD);
		mRecyclerView.addItemDecoration(dividerLine);
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				boolean enable;
				if(mRecyclerView!= null && layoutManager.getChildCount()>0){
					boolean firstItemVisible = layoutManager.findFirstVisibleItemPosition() == 0;
					boolean topOfFirstItemVisible = layoutManager.findFirstCompletelyVisibleItemPosition() == 0;
					enable = firstItemVisible && topOfFirstItemVisible;
					mSwipeLayout.setEnabled(enable);
				}else{
					mSwipeLayout.setEnabled(true);
				}
			}
		});
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark, R.color.holo_orange_light, R.color.holo_red_light);
		adapter = new CurrentSessionAdapter(getActivity());
		mRecyclerView.setAdapter(adapter);
		adapter.registerAdapterDataObserver(mObserver);
		mObserverAttached = true;
		updateEmptyStatus(isEmpty());
		adapter.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onClick(View itemView, int position) {
				HDSession sEntty = adapter.getItem(position);
				if(sEntty == null){
					return;
				}
				Intent intent = new Intent();
				intent.setClass(getActivity(), ChatActivity.class);
				intent.putExtra("hasUnReadMessage", sEntty.hasUnReadMessage());
				intent.putExtra("user",  sEntty.getUser());
				intent.putExtra("originType",sEntty.getOriginType());
				intent.putExtra("techChannelName",sEntty.getTechChannelName());
				intent.putExtra("visitorid", sEntty.getServiceSessionId());
				intent.putExtra("chatGroupId", sEntty.getChatGroupId());
				startActivity(intent);
			}
		});
		// 搜索框
		String strSearch ="搜索";
		query.setHint(strSearch);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
			                          int count) {
				adapter.getFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
			                              int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideSoftKeyboard();
			}

		});
		CurrentSessionManager.getInstance().getSessionsFromServer(new HDDataCallBack<List<HDSession>>() {
			@Override
			public void onSuccess(List<HDSession> value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.refresh();
						refreshSessionLabel();
					}
				});
			}

			@Override
			public void onError(final int error, final String errorMsg) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(getActivity(), "获取会话列表失败！" + error + ";" + errorMsg, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		connectionListener = new HDConnectionListener() {
			@Override
			public void onConnected() {
				HDLog.e(TAG, "onConnected");
				if (getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (errorItem != null)
							errorItem.setVisibility(View.GONE);
						HDLog.e(TAG, "onConnected--error--GONE");
					}
				});
			}

			@Override
			public void onAuthenticationFailed(int errorCode) {
				if (getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Jump to the login UI
						Intent intent = new Intent();
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setClass(getContext(), LoginActivity.class);
						startActivity(intent);
						getActivity().finish();
					}
				});

			}

			@Override
			public void onDisconnected() {
				HDLog.d(TAG, "onDisconnected");
				if (getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (errorItem != null)
							errorItem.setVisibility(View.VISIBLE);
						if(NetUtils.hasNetwork(getActivity())){
							errorText.setText("连接不到服务器");
						}else{
							errorText.setText("当前无网络");
						}
					}
				});
			}
		};
		HDClient.getInstance().addConnectionListener(connectionListener);

		settting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MainActivity)getActivity()).showMaxAccess(view);
			}
		});
		refreshAgentAvatar();
		loadFirstStatus();
	}

	void hideSoftKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getActivity().getCurrentFocus() != null)
				inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}


	@Override
	public void onFresh(final EMValueCallBack callback) {
		CurrentSessionManager.getInstance().getSessionsFromServer(new HDDataCallBack<List<HDSession>>() {
			@Override
			public void onSuccess(List<HDSession> value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.refresh();
						refreshSessionLabel();
						if (mSwipeLayout != null)
							mSwipeLayout.setRefreshing(false);
						if (callback != null) {
							callback.onSuccess(null);
						}
					}
				});
			}

			@Override
			public void onError(final int error, final String errorMsg) {
				if (getActivity() == null) {
					return;
				}
				HDLog.e(TAG, "error:" + errorMsg);
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mSwipeLayout != null)
							mSwipeLayout.setRefreshing(false);
						if (callback != null) {
							callback.onError(error, errorMsg);
						}
					}
				});
			}
		});
	}

	private void updateEmptyStatus(boolean empty){
		if(empty){
			mRecyclerView.setVisibility(View.GONE);
			if(emptyView != null){
				emptyView.setVisibility(View.VISIBLE);
			}
		}else{
			mRecyclerView.setVisibility(View.VISIBLE);
			if(emptyView != null){
				emptyView.setVisibility(View.GONE);
			}
		}
	}
	private boolean mObserverAttached = false;
	@Override
	public void onDetach() {
		super.onDetach();
		if(adapter != null && mObserverAttached){
			adapter.unregisterAdapterDataObserver(mObserver);
			mObserverAttached = false;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CloseSessionManager.getInstance().addCloseSessionListener(this);
		OverTimeSessionManager.getInstance().addOverTimeSessionListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (connectionListener != null){
			HDClient.getInstance().removeConnectionListener(connectionListener);
		}
		CloseSessionManager.getInstance().removeCloseSessionListener(this);
		OverTimeSessionManager.getInstance().removeOverTimeSessionListener(this);
	}

	private boolean isEmpty(){
		return adapter == null ? true : adapter.getItemCount() == 0;
	}
	private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			updateEmptyStatus(isEmpty());
		}

		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			super.onItemRangeChanged(positionStart, itemCount);
			updateEmptyStatus(isEmpty());
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			super.onItemRangeInserted(positionStart, itemCount);
			updateEmptyStatus(isEmpty());
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			super.onItemRangeRemoved(positionStart, itemCount);
			updateEmptyStatus(isEmpty());
		}
	};

	public int getUnreadCount() {
		return CurrentSessionManager.getInstance().getTotalUnReadCount();
	}

	private void refreshSessionLabel() {
		((MainActivity) getActivity()).refreshSessionUnreadCount();
		((MainActivity) getActivity()).refreshSessionCount(CurrentSessionManager.getInstance().getSessions().size());
	}

	@Override
	public void onRefreshView() {
		adapter.refresh();
		refreshSessionLabel();
	}

	@Override
	public void onRefresh() {
		onFresh(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(tvSessionCount == null || currentUser == null){
			return;
		}
		tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
	}

	@Override
	public void close(final String sSessionId) {
		if(getActivity() == null){
			return;
		}
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				HDSession session = CurrentSessionManager.getInstance().getSessionEntity(sSessionId);
				if (session != null) {
					CurrentSessionManager.getInstance().remove(sSessionId);
					query.getText().clear();
					onRefreshView();
				}
			}
		});
	}

	@Override
	public void closeSession(String sSessionId) {
		HDSession session = CurrentSessionManager.getInstance().getSessionEntity(sSessionId);
		if (session != null) {
			CurrentSessionManager.getInstance().remove(sSessionId);
			EMToast.makeStyleableToast(getActivity(), "会话超时已关闭！").show();
			onRefreshView();
		}
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (connectionListener != null){
			HDClient.getInstance().removeConnectionListener(connectionListener);
		}
		if (unbinder != null){
			unbinder.unbind();
		}
	}

	public void refreshSessionCount(int count){
		if(tvSessionCount == null || currentUser == null){
			return;
		}
		if (count >= 0){
			currentSessionCount = count;
		}
		tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
	}

	public void refreshAgentAvatar() {
		if(ivAvatar != null)
			AvatarUtils.refreshAgentAvatar(getActivity(), ivAvatar);
	}


	public void refreshOnline(String status) {
		CommonUtils.setAgentStatusView(ivStatus, status);
	}

	private void loadFirstStatus() {
		if (currentUser != null) {
			refreshOnline(currentUser.getOnLineState());
		}
	}
}
