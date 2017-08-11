package com.easemob.helpdesk.activity.chat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.easemob.helpdesk.adapter.AgentQueueAdapter;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.AgentQueue;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.List;

public class SkillGroupsActivity extends BaseActivity implements OnItemClickListener, OnRefreshListener {

	private static final String TAG = SkillGroupsActivity.class.getSimpleName();
	private static final int REQUEST_CODE_SKILLGROUP_ALERTDIALOG = 1;
	private List<AgentQueue> agentQueues = new ArrayList<AgentQueue>();
	private ListView mListView;
	private ImageButton clearSearch;
	private EditText query;
	private SwipeRefreshLayout mSwipeLayout = null;
	private Dialog pd = null;
	private AgentQueueAdapter adapter;
	private AgentQueue currentAgentQueue;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_skillgroups);
		initView();
		if(pd == null){
			pd = DialogUtils.getLoadingDialog(SkillGroupsActivity.this, "加载中...");
		}
		pd.show();
		getSkillGroupsFromRemote();
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.listview);
		adapter = new AgentQueueAdapter(this, agentQueues);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(this);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly_skillgroup);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark,
				R.color.holo_orange_light, R.color.holo_red_light);
		// search edittext
		query = (EditText) findViewById(R.id.query);
		query.setHint(R.string.hint_search);
		// clear btn by search edittext
		clearSearch = (ImageButton) findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.getAgentFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideKeyboard();
			}
		});
	}

	public void back(View view) {
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AgentQueue agentQueue = (AgentQueue) parent.getItemAtPosition(position);
		currentAgentQueue = agentQueue;
		if(agentQueue.totalAgentCount > 0){
			startActivityForResult(new Intent(this, AlertDialog.class).putExtra("msg", "确定转接该会话？"), REQUEST_CODE_SKILLGROUP_ALERTDIALOG);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == REQUEST_CODE_SKILLGROUP_ALERTDIALOG){
				long queueId = currentAgentQueue.queueId;
				setResult(RESULT_OK, new Intent().putExtra("queueId", queueId));
				finish();
			}
		}
	}
	
	

	@Override
	public void onRefresh() {
		// http get skillgroup list
		getSkillGroupsFromRemote();

	}

	private void getSkillGroupsFromRemote() {
		HDClient.getInstance().agentManager().getSkillGroupsFromRemote(new HDDataCallBack<List<AgentQueue>>() {

			@Override
			public void onSuccess(final List<AgentQueue> value) {
				HDLog.d(TAG, "value:" + value);
				if(isFinishing()){
					return;
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeDialog();
						agentQueues.clear();
						agentQueues.addAll(value);
						adapter.notifyDataSetChanged();
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
						Toast.makeText(getApplicationContext(), R.string.error_getAgentUserListFail, Toast.LENGTH_SHORT).show();
						mSwipeLayout.setRefreshing(false);
					}
				});
			}

		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDialog();
	}

	public void closeDialog() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
			pd = null;
		}
	}

}
