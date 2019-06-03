package com.easemob.helpdesk.activity.agent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.chat.SkillGroupsActivity;
import com.easemob.helpdesk.adapter.AgentsAdapter;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AgentUserListActivity extends BaseActivity implements OnItemClickListener,
		OnRefreshListener {

	private static final int REQUEST_CODE_ALERTDIALOG = 1;
	private static final int REUQEST_CODE_SKILLGROUP = 2;
	private ListView mListView;
	private List<AgentUser> agentUsers = new ArrayList<>();
	private AgentsAdapter adapter;
	private ImageButton clearSearch;
	private EditText query;
	private SwipeRefreshLayout mSwipeLayout = null;
	private Dialog pd = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_agentusers);
		initView();
		if (pd == null) {
			pd = DialogUtils.getLoadingDialog(AgentUserListActivity.this, R.string.info_loading);
		}
		pd.show();
		getAgentUserListFromRemote();
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.customer_listview);
		adapter = new AgentsAdapter(this, agentUsers);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(this);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly_agent);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark,
				R.color.holo_orange_light, R.color.holo_red_light);
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				boolean enable;
				if(mListView != null && mListView.getChildCount()>0){
					//check if the first item of the list is visible
					boolean firstItemVisible = mListView.getFirstVisiblePosition() == 0;
					//check if the top of the first item is visible
					boolean topOfFirstItemVisible = mListView.getChildAt(0).getTop() == 0;
					//enabling or disabling the refresh layout
					enable = firstItemVisible && topOfFirstItemVisible;
					mSwipeLayout.setEnabled(enable);
				}else{
					mSwipeLayout.setEnabled(true);
				}
			}
		});

		// search edittext
		query = (EditText) findViewById(R.id.query);
		query.setHint(R.string.hint_search);
		// clear btn by search edittext
		clearSearch = (ImageButton) findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.getChatFilter().filter(s);
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
				hideKeyboard();
			}
		});

	}

	public void back(View view){
		finish();
	}
	
	public void right(View view){
		Intent intent = new Intent();
		intent.setClass(this, SkillGroupsActivity.class);
		startActivityForResult(intent, REUQEST_CODE_SKILLGROUP);
	}
	
	private AgentUser currentAgentUser;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		AgentUser agentUser = (AgentUser) parent.getItemAtPosition(position);
		currentAgentUser = agentUser;
		startActivityForResult(
				new Intent(this, AlertDialog.class).putExtra("msg", R.string.confirm_transfer_session),
				REQUEST_CODE_ALERTDIALOG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_ALERTDIALOG) {
				String userId = currentAgentUser.user.getUserId();
				setResult(RESULT_OK, new Intent().putExtra("userId", userId));
				finish();
			}else if(requestCode == REUQEST_CODE_SKILLGROUP){
				setResult(RESULT_OK, data);
				finish();
			}

		}

	}

	@Override
	public void onRefresh() {
		// http get agentUser list
		getAgentUserListFromRemote();

	}

	private void getAgentUserListFromRemote() {
		HDClient.getInstance().agentManager().getAgentList(false, new HDDataCallBack<List<AgentUser>>() {
			@Override
			public void onSuccess(final List<AgentUser> value) {
				if(isFinishing()){
					return;
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeDialog();
						agentUsers.clear();
						agentUsers.addAll(value);
						synchronized (agentUsers){
							Collections.sort(agentUsers, new Comparator<AgentUser>() {
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

						adapter.refresh();
						mSwipeLayout.setRefreshing(false);
					}
				});
				
			}

			@Override
			public void onError(int error, String errorMsg) {
				if(isFinishing()){
					return;
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeDialog();
						Toast.makeText(AgentUserListActivity.this, R.string.error_getAgentUserListFail,
								Toast.LENGTH_SHORT).show();
						mSwipeLayout.setRefreshing(false);						
					}
				});
				
			}

		});
	}


	public void closeDialog() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
			pd = null;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDialog();
	}

}
