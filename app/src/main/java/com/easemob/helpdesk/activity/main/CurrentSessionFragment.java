package com.easemob.helpdesk.activity.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.EMValueCallBack;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.SessionAdapter;
import com.easemob.helpdesk.listener.OnItemClickListener;
import com.easemob.helpdesk.mvp.ChatActivity;
import com.easemob.helpdesk.mvp.LoginActivity;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.EMToast;
import com.easemob.helpdesk.utils.HDNotifier;
import com.easemob.helpdesk.utils.OnFreshCallbackListener;
import com.easemob.helpdesk.utils.OnRefreshViewListener;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.HDConnectionListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.manager.session.CloseSessionManager;
import com.hyphenate.kefusdk.manager.session.CurrentSessionManager;
import com.hyphenate.kefusdk.manager.session.OverTimeSessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.util.NetUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CurrentSessionFragment extends Fragment implements OnFreshCallbackListener, OnRefreshViewListener, OnRefreshListener, CloseSessionManager.CloseSessionListener, OverTimeSessionManager.OverTimeSessionListener {

	private static final String TAG = CurrentSessionFragment.class.getSimpleName();

	private InputMethodManager inputMethodManager;
	@BindView(R.id.list)
	public RecyclerView mRecyclerView;
	private SessionAdapter adapter;
	@BindView(R.id.query)
	public EditText query;
	@BindView(R.id.search_clear)
	public ImageButton clearSearch;
	public static OnFreshCallbackListener callback = null;
	public static OnRefreshViewListener refreshCallback = null;
	@BindView(R.id.swipe_layout)
	public SwipeRefreshLayout mSwipeLayout;
	@BindView(R.id.emptyView)
	public View emptyView;
	private HDConnectionListener connectionListener;
	@BindView(R.id.rl_error_item)
	public RelativeLayout errorItem;
	public TextView errorText;
	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_current_session, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
				if (mRecyclerView != null && layoutManager.getChildCount() > 0) {
					boolean firstItemVisible = layoutManager.findFirstVisibleItemPosition() == 0;
					boolean topOfFirstItemVisible = layoutManager.findFirstCompletelyVisibleItemPosition() == 0;
					enable = firstItemVisible && topOfFirstItemVisible;
					mSwipeLayout.setEnabled(enable);
				} else {
					mSwipeLayout.setEnabled(true);
				}
			}
		});
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark, R.color.holo_orange_light, R.color.holo_red_light);
		adapter = new SessionAdapter(getActivity());
		mRecyclerView.setAdapter(adapter);
		adapter.registerAdapterDataObserver(mObserver);
		mObserverAttached = true;
		updateEmptyStatus(isEmpty());
		adapter.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onClick(View itemView, int position) {
				HDSession sEntty = adapter.getItem(position);
				if (sEntty == null) {
					return;
				}
				Intent intent = new Intent();
				intent.setClass(getActivity(), ChatActivity.class);
				intent.putExtra("hasUnReadMessage", sEntty.hasUnReadMessage());
				intent.putExtra("user", sEntty.getUser());
				intent.putExtra("originType", sEntty.getOriginType());
				intent.putExtra("techChannelName", sEntty.getTechChannelName());
				intent.putExtra("visitorid", sEntty.getServiceSessionId());
				intent.putExtra("chatGroupId", sEntty.getChatGroupId());
				startActivity(intent);
			}
		});
		// 搜索框
		String strSearch = "搜索";
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
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideSoftKeyboard();
			}
		});

		connectionListener = new HDConnectionListener() {
			@Override
			public void onConnected() {
				HDLog.e(TAG, "onConnected");
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (errorItem != null)
							errorItem.setVisibility(View.GONE);
					}
				});
			}

			@Override
			public void onAuthenticationFailed(int errorCode) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						HDLog.d(TAG, "onAuthenticationFailed");
						HDNotifier.getInstance().cancelNotification();
						// Jump to the login UI
						Intent intent = new Intent();
						intent.setClass(HDApplication.getInstance(), LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						HDApplication.getInstance().startActivity(intent);
						HDApplication.getInstance().finishAllActivity();
					}
				});

			}

			@Override
			public void onDisconnected() {
				HDLog.d(TAG, "onDisconnected");
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (errorItem != null)
							errorItem.setVisibility(View.VISIBLE);
						if (NetUtils.hasNetwork(getActivity())) {
							errorText.setText("连接不到服务器");
						} else {
							errorText.setText("当前无网络");
						}
					}
				});
			}
		};
		HDClient.getInstance().addConnectionListener(connectionListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		CurrentSessionManager.getInstance().getSessionsFromServer(new HDDataCallBack<List<HDSession>>() {
			@Override
			public void onSuccess(List<HDSession> value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (adapter != null) {
							adapter.refresh();
						}
						refreshSessionLabel();
					}
				});
			}

			@Override
			public void onError(final int error, final String errorMsg) {
				HDLog.e(TAG, "error:" + errorMsg);
			}
		});

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
						if (adapter != null) {
							adapter.refresh();
						}
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

	private void updateEmptyStatus(boolean empty) {
		if (mRecyclerView == null) {
			return;
		}
		if (empty) {
			mRecyclerView.setVisibility(View.GONE);
			if (emptyView != null) {
				emptyView.setVisibility(View.VISIBLE);
			}
		} else {
			mRecyclerView.setVisibility(View.VISIBLE);
			if (emptyView != null) {
				emptyView.setVisibility(View.GONE);
			}
		}
	}

	private boolean mObserverAttached = false;

	@Override
	public void onDetach() {
		super.onDetach();
		if (adapter != null && mObserverAttached) {
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
		if (connectionListener != null) {
			HDClient.getInstance().removeConnectionListener(connectionListener);
		}
		CloseSessionManager.getInstance().removeCloseSessionListener(this);
		OverTimeSessionManager.getInstance().removeOverTimeSessionListener(this);
	}

	private boolean isEmpty() {
		return adapter == null || adapter.getItemCount() == 0;
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
		((MainActivity) getActivity()).refreshSessionCount(CurrentSessionManager.getInstance().getSessions().size());
		((MainActivity) getActivity()).refreshSessionUnreadCount();
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
	public void close(final String sSessionId) {
		if (getActivity() == null) {
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
		if (connectionListener != null) {
			HDClient.getInstance().removeConnectionListener(connectionListener);
		}
		if (unbinder != null) {
			unbinder.unbind();
		}
		callback = null;
		refreshCallback = null;
		adapter = null;
	}

}
