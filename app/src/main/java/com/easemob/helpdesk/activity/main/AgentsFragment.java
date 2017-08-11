package com.easemob.helpdesk.activity.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.helpdesk.EMValueCallBack;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.AgentsAdapter;
import com.easemob.helpdesk.mvp.AgentChatActivity;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.OnFreshCallbackListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.AgentUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AgentsFragment extends Fragment implements OnFreshCallbackListener, OnRefreshListener {

	private static final String TAG = AgentsFragment.class.getSimpleName();

	@BindView(R.id.query)
	public EditText query;
	private final List<AgentUser> agentList = Collections.synchronizedList(new ArrayList<AgentUser>());
	private ImageButton clearSearch;
	private InputMethodManager inputMethodManager;
	@BindView(R.id.customer_listview)
	public ListView listview;
	private AgentsAdapter agentsAdapter;
	private Boolean isExit = false;
	public static OnFreshCallbackListener callback;
	public int unReadMsgCount;
	@BindView(R.id.swipe_ly_agent)
	public SwipeRefreshLayout mSwipeLayout = null;
	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_customer, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		callback = this;
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark, R.color.holo_orange_light, R.color.holo_red_light);
		agentsAdapter = new AgentsAdapter(getActivity(), agentList);
		listview.setAdapter(agentsAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AgentUser agentEntty = (AgentUser) parent.getItemAtPosition(position);
				Intent intent = new Intent();
				intent.setClass(getActivity(), AgentChatActivity.class);
				intent.putExtra("user", agentEntty.user);
				intent.putExtra("hasUnReadMessage", agentEntty.hasUnReadMessage);
				startActivity(intent);
			}
		});
		listview.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				boolean enable;
				if(listview!=null&&listview.getChildCount()>0){
					//check if the first item of the list is visible
					boolean firstItemVisible = listview.getFirstVisiblePosition()==0;
					//check if the top of the first item is visible
					boolean topOfFirstItemVisible = listview.getChildAt(0).getTop()==0;
					//enabling or disabling the refresh layout
					enable = firstItemVisible && topOfFirstItemVisible;
					mSwipeLayout.setEnabled(enable);
				}else{
					mSwipeLayout.setEnabled(true);
				}
			}
		});
		// search edittext
		query.setHint(R.string.hint_search);
		// clear btn by search edittext
		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				agentsAdapter.getChatFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
		// http getAgentUser list
		getAgentUserListFromRemote();
	}
	
	

	void hideSoftKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getActivity().getCurrentFocus() != null)
				inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/*
	 * double textview return top bar
	 */
	private void exitBy2Click() {
		Timer tExit;
		if (isExit == false) {
			isExit = true;
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false;
				}
			}, 2000);
		} else {
			listview.setSelection(0);
		}
	}

	@Override
	public void onFresh(EMValueCallBack callback) {
		// http get agentUser list
		getAgentUserListFromRemote();

	}

	private void getAgentUserListFromRemote() {
		HDClient.getInstance().agentManager().getAgentList(new HDDataCallBack<List<AgentUser>>() {
			@Override
			public void onSuccess(final List<AgentUser> value) {
				if(getActivity()==null){
					return;
				}
				HDApplication.AgentLastUpdateTime = System.currentTimeMillis();
				synchronized (agentList){
					agentList.clear();
					agentList.addAll(value);
					Collections.sort(agentList, new Comparator<AgentUser>() {
						@Override
						public int compare(AgentUser lhs, AgentUser rhs) {
							try {
								String lState = lhs.user.getOnLineState();
								String rState = rhs.user.getOnLineState();
								int lIntState = CommonUtils.getAgentStatus(lState);
								int rIntState = CommonUtils.getAgentStatus(rState);
								return lIntState-rIntState;

							}catch (Exception e){
							}
							return 0;
						}
					});
				}
				if(getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						sortUserByLastChatTime(agentList);
						agentsAdapter.refresh();
						if(mSwipeLayout != null){
							mSwipeLayout.setRefreshing(false);
						}
						if(getActivity() != null && getActivity() instanceof MainActivity){
							((MainActivity) getActivity()).refreshSessionUnreadCount();
						}

					}
				});
				
			}

			@Override
			public void onError(int error, String errorMsg) {
				if(getActivity()==null){
					return;
				}
				HDLog.e(TAG, "error:"+error+";errorMsg:"+errorMsg);
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						try{
							Toast.makeText(getActivity(), R.string.error_getAgentUserListFail, Toast.LENGTH_SHORT).show();
							mSwipeLayout.setRefreshing(false);
						}catch (Exception e){
							e.printStackTrace();
						}
					}
				});
			}

		});
	}

	private void sortUserByLastChatTime(List<AgentUser> mList) {
		int count = 0;
		for (int i = 0; i < mList.size(); i++) {
			AgentUser agentUser = mList.get(i);
			count += agentUser.unReadMessageCount;
		}
		unReadMsgCount = count;
	}

	@Override
	public void onRefresh() {
		onFresh(null);
	}

	//session tab click refresh
	public void refreshByRemote(){
		onFresh(null);
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (unbinder != null){
			unbinder.unbind();
		}
		callback = null;
		agentsAdapter = null;
	}
}
