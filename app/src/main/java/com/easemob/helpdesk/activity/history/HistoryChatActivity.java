package com.easemob.helpdesk.activity.history;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.CategoryShowActivity;
import com.easemob.helpdesk.activity.visitor.CustomerDetailActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.mvp.ChatActivity;
import com.easemob.helpdesk.recorder.MediaManager;
import com.easemob.helpdesk.widget.popupwindow.HistorySessionMore;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDCategorySummary;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.entity.option.OptionEntity;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.entity.user.HDVisitorUser;
import com.hyphenate.kefusdk.manager.session.SessionManager;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



/**
 * Created by lyuzhao on 2015/12/16.
 */
public class HistoryChatActivity extends BaseActivity implements View.OnClickListener {

	private final String TAG = getClass().getSimpleName();

	private static final int REQUEST_CODE_CATEGORY_SHOW = 0x01;

	@BindView(R.id.btn_call_back)
	protected Button btnCallback;

	@BindView(R.id.history_listview)
	protected RecyclerView mRecyclerView;

	@BindView(R.id.ll_channel)
	protected LinearLayout llChannel;

	@BindView(R.id.iv_channel)
	protected ImageView iv_channel;

	@BindView(R.id.tv_channel_content)
	protected TextView tvChannelText;

	@BindView(R.id.user_name)
	protected TextView tvTitle;

	@BindView(R.id.btn_up)
	protected ImageButton btnUp;

	@BindView(R.id.btn_down)
	protected ImageButton btnDown;

	@BindView(R.id.tag_layout)
	protected LinearLayout tagLayout;

	@BindView(R.id.tag_container)
	protected View tagContainer;

	@BindView(R.id.tagview)
	protected TagView tagGroup;

	@BindView(R.id.tv_note)
	protected TextView tvNote;

	@BindView(R.id.ll_title_click)
	protected LinearLayout llTitleClick;

	@BindView(R.id.ib_menu_more)
	protected ImageButton ibMenuMore;

	@BindView(R.id.seesion_extra_info)
	protected TextView sessionExtraInfo;

	@BindView(R.id.iv_show_label) protected ImageView ivShowLabel;

	/**
	 * 加载更多的View
	 */
	@BindView(R.id.chat_swipe_layout)
	public SwipeRefreshLayout swipeRefreshLayout;

	/**
	 * 是否有更多的数据
	 */
	private boolean haveMoreData = true;
	/**
	 * 当前是否正在加载数据
	 */
	private boolean isLoadding = false;

	private String originType;
	private String techChannelName;

	public ChatAdapter mAdapter;

	private ProgressDialog pd = null;
	private HDVisitorUser visitorUser;
	private String sessionId = null;
	private String commentString;

	private SessionManager sessionManager;

	private boolean isWait;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_history_chat);
		ButterKnife.bind(this);
		Intent intent = getIntent();
		sessionId = intent.getStringExtra("visitorid");
		visitorUser = intent.getParcelableExtra("user");
		originType = intent.getStringExtra("originType");
		techChannelName = intent.getStringExtra("techChannelName");
		isWait = intent.getBooleanExtra("isWait", false);
		long chatGroupId = intent.getLongExtra("chatGroupId", 0);
		sessionManager = new SessionManager(chatGroupId, sessionId, visitorUser, null);
		initDatas();
		initListener();
		LoadRemoteMsg();
		if (!isWait) {
			//获取tagsView
			getTagsFromRemote();
			getCommentsFromRemote();
			getSessionExtraInfo();
		} else {
			sessionExtraInfo.setVisibility(View.GONE);
			btnCallback.setText("-> 接入");
			ibMenuMore.setVisibility(View.GONE);
			tagContainer.setVisibility(View.GONE);
		}
	}

	private void initListener() {
		llTitleClick.setOnClickListener(this);
		btnUp.setOnClickListener(this);
		btnDown.setOnClickListener(this);
		btnCallback.setOnClickListener(this);
	}

	@OnClick(R.id.layout_back)
	public void onClickByBack(){
		finish();
	}

	private void getTagsFromRemote() {
		sessionManager.getCategorySummarys(new HDDataCallBack<List<HDCategorySummary>>() {
			@Override
			public void onSuccess(final List<HDCategorySummary> value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						final int tagLayoutVisibility = tagLayout.getVisibility();
						if (tagLayoutVisibility == View.GONE) {
							tagLayout.setVisibility(View.INVISIBLE);
							new Handler().post(new Runnable() {
								@Override
								public void run() {
									setTagViews(value);
									if (tagLayoutVisibility != tagLayout.getVisibility()) {
										tagLayout.setVisibility(tagLayoutVisibility);
									}
								}
							});
						} else {
							setTagViews(value);
						}
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {

			}
		});
	}

	private void setTagViews(List<HDCategorySummary> list) {

		Tag tag;
		if (list == null || list.size() == 0) {
			tagGroup.addTags(new java.util.ArrayList<Tag>());
			return;
		}
		ArrayList<Tag> tags = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			HDCategorySummary entty = list.get(i);
			String rootName = (TextUtils.isEmpty(entty.rootName)) ? "" : entty.rootName + ">";
			tag = new Tag(rootName + entty.name);
			tag.id = entty.id;
			tag.radius = 10f;
			int color = (int) entty.color;
			String strColor;
			if (color == 0) {
				strColor = "#000000";
			} else if (color == 255) {
				strColor = "#ffffff";
			} else {
				strColor = "#" + Integer.toHexString(color);
				strColor = strColor.substring(0, 7);
			}
			tag.layoutColor = Color.parseColor(strColor);
			tag.isDeletable = false;
			tags.add(tag);

		}
		tagGroup.addTags(tags);
	}


	private void getCommentsFromRemote() {
		sessionManager.getCommentsFromServer(new HDDataCallBack<String>() {
			@Override
			public void onSuccess(final String value) {
				if (isFinishing()) {
					return;
				}
				commentString = value;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (TextUtils.isEmpty(value)){
							tvNote.setText("");
						}else{
							tvNote.setText("备注：" + value);
						}
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {

			}
		});
	}


	private void initDatas() {
		if (pd == null) {
			pd = new ProgressDialog(this);
			pd.setMessage(getResources().getString(R.string.loading_getdata));
		}
		pd.show();
		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(layoutManager);
		if (visitorUser != null && !TextUtils.isEmpty(visitorUser.getNicename())) {
			tvTitle.setText(visitorUser.getNicename());
		}
		mAdapter = new ChatAdapter(this, sessionManager, mRecyclerView);
		mRecyclerView.setAdapter(mAdapter);

		llChannel.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(originType)) {
			switch (originType) {
				case "weibo":
					iv_channel.setImageResource(R.drawable.channel_weibo_icon);
					break;
				case "weixin":
					iv_channel.setImageResource(R.drawable.channel_wechat_icon);
					break;
				case "webim":
					iv_channel.setImageResource(R.drawable.channel_web_icon);
					break;
				case "app":
					iv_channel.setImageResource(R.drawable.channel_app_icon);
					break;
			}
			if (!TextUtils.isEmpty(techChannelName)) {
				tvChannelText.setText("会话来自:" + techChannelName);
			}
		}


		swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark,
				R.color.holo_orange_light, R.color.holo_red_light);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						int firstVisibilePosition = layoutManager.findFirstVisibleItemPosition();
						if (firstVisibilePosition == 0 && !isLoadding && haveMoreData) {
							sessionManager.asyncGetSessionMessages(new HDDataCallBack<List<HDMessage>>() {
								@Override
								public void onSuccess(final List<HDMessage> value) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											swipeRefreshLayout.setRefreshing(false);
											isLoadding = false;
											if (value.isEmpty()){
												haveMoreData = false;
												Toast.makeText(mActivity, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
												return;
											}else{
												mAdapter.refresh();
											}
										}
									});
								}

								@Override
								public void onError(int error, String errorMsg) {
									if (isFinishing()) {
										return;
									}
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											isLoadding = false;
											swipeRefreshLayout.setRefreshing(false);
										}
									});

								}
							});
						} else {
							Toast.makeText(mActivity, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
						}
						if (swipeRefreshLayout != null){
							swipeRefreshLayout.setRefreshing(false);
						}


					}
				}, 1000);
			}
		});


	}


	private void LoadRemoteMsg() {
		sessionManager.asyncGetSessionMessages(new HDDataCallBack<List<HDMessage>>() {
			@Override
			public void onSuccess(List<HDMessage> value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeDialog();
						mAdapter.refresh();
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeDialog();
						Toast.makeText(mActivity, "网络请求失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	@OnClick(R.id.iv_close) public void onClickByTagUp() {
		tagLayout.setVisibility(View.GONE);
		ivShowLabel.setVisibility(View.VISIBLE);
	}

	@OnClick(R.id.iv_show_label) public void onClickByTagDown() {
		tagLayout.setVisibility(View.VISIBLE);
		ivShowLabel.setVisibility(View.GONE);
	}

	public RecyclerView getListView() {
		return mRecyclerView;
	}

	private void closeDialog(){
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ll_title_click: {
				if (TextUtils.isEmpty(sessionId)) {
					return;
				}
				HDUser emUser = HDClient.getInstance().getCurrentUser();
				if (emUser == null){
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("visitorId", sessionId);
				intent.putExtra("userId", visitorUser.getUserId());
				intent.putExtra("tenantId", emUser.getTenantId());
				intent.setClass(mActivity, CustomerDetailActivity.class);
				startActivity(intent);
			}
			break;
			case R.id.btn_call_back:
				if (!isWait) {
					doCallBackSession();
				} else {
					doAccessSession();
				}
				break;
			case R.id.btn_up:
				tagLayout.setVisibility(View.GONE);
				btnUp.setVisibility(View.INVISIBLE);
				btnDown.setVisibility(View.VISIBLE);
				break;
			case R.id.btn_down:
				tagLayout.setVisibility(View.VISIBLE);
				btnUp.setVisibility(View.VISIBLE);
				btnDown.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
		}
	}

	HistorySessionMore sessionMoreWindow;

	public void sessionMore(View view) {
		if (sessionMoreWindow == null) {
			sessionMoreWindow = new HistorySessionMore(this);
		}
		sessionMoreWindow.showPopupWindow(ibMenuMore);
	}

	private void doAccessSession() {
		if (pd == null) {
			pd = new ProgressDialog(this);
		}
		pd.setMessage("正在接入...");
		pd.show();

		sessionManager.accessWaitUser(sessionId, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeDialog();
						Toast.makeText(getApplicationContext(), "接入成功！", Toast.LENGTH_SHORT).show();
						finish();
					}
				});
			}

			@Override
			public void onError(final int error, final String errorMsg) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeDialog();
						Toast.makeText(getApplicationContext(), "接入失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});


	}
	private void doCallBackSession() {
		if (pd == null) {
			pd = new ProgressDialog(this);
		}
		pd.setMessage("正在回呼...");
		pd.show();
		sessionManager.asyncCreateSession(new HDDataCallBack<HDSession>() {
			@Override
			public void onSuccess(final HDSession sessionEntty) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeDialog();
						Toast.makeText(getApplicationContext(), "回呼成功！", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent();
						intent.setClass(HistoryChatActivity.this, ChatActivity.class);
						intent.putExtra("visitorid", sessionEntty.getServiceSessionId());
						intent.putExtra("originType", originType);
						intent.putExtra("user", sessionEntty.getUser());
						intent.putExtra("chatGroupId", sessionEntty.getChatGroupId());
						intent.putExtra("techChannelName", techChannelName);
						startActivity(intent);
						finish();
					}
				});
			}

			@Override
			public void onError(final int error,final String errorMsg) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeDialog();
						if (error == 400) {
							try {
								JSONObject obj = new JSONObject(errorMsg);
								String errorCode = obj.getString("errorCode");
								if (errorCode.equals("KEFU_025")) {
									Toast.makeText(getApplicationContext(), "回呼失败，会话的关联不存在!", Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(getApplicationContext(), "回呼失败，该访客有尚未结束的会话！", Toast.LENGTH_SHORT).show();
								}

							} catch (JSONException e) {
								e.printStackTrace();
								Toast.makeText(getApplicationContext(), "回呼失败，该访客有尚未结束的会话！", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(getApplicationContext(), "回呼失败！", Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
	}

	public void lable_setting(View view) {
		popupclose(null);
		startActivityForResult(new Intent(this, CategoryShowActivity.class).putExtra("sessionId", sessionId).putExtra("value", sessionManager.getCategoryTreeValue()), REQUEST_CODE_CATEGORY_SHOW);
//
	}

	public void popupclose(View view) {
		if (sessionMoreWindow != null && sessionMoreWindow.isShowing()) {
			sessionMoreWindow.dismiss();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_CATEGORY_SHOW) {
				String newValue = data.getStringExtra("value");
				String comment = data.getStringExtra("comment");
				if (!newValue.equals(sessionManager.getCategoryTreeValue())) {
					getTagsFromRemote();
				}
				if (commentString == null) {
					commentString = "";
				}
				if (comment == null) {
					comment = "";
				}
				if (!commentString.equals(comment)) {
					getCommentsFromRemote();
				}
			}
		}


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MediaManager.release();
		sessionManager.clear();
	}

	private void getSessionExtraInfo() {
		OptionEntity optionEntity = HDClient.getInstance().agentManager().getOptionEntity("sessionOpenNoticeEnable");

		if (optionEntity != null && optionEntity.getOptionValue().equals("true")) {
			sessionManager.getSessionExtraInfo(new HDDataCallBack<String>() {
				@Override
				public void onSuccess(final String value) {
					if (isFinishing()){
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (sessionExtraInfo != null)
								sessionExtraInfo.setText(value);
						}
					});
				}

				@Override
				public void onError(int error, String errorMsg) {

				}
			});
		}
	}
}
