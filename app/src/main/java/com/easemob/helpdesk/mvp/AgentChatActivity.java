package com.easemob.helpdesk.mvp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.main.AgentsFragment;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.emoticon.data.AppBean;
import com.easemob.helpdesk.emoticon.utils.Constants;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.emoticon.view.SimpleUserDefAppsGridView;
import com.easemob.helpdesk.widget.chatview.ChatEmoticonsKeyBoard;
import com.hyphenate.kefusdk.HDChatListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.manager.session.SessionManager;
import com.sj.emoji.EmojiBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.utils.EmoticonsKeyboardUtils;
import sj.keyboard.widget.EmoticonsAutoEditText;
import sj.keyboard.widget.FuncLayout;

/**
 * Created by liyuzhao on 02/05/2017.
 */

public class AgentChatActivity extends BaseChatActivity implements FuncLayout.OnFuncKeyBoardListener {

	/**
	 * 界面标题Title
	 */
	@BindView(R.id.user_name)
	protected TextView tvTitle;

	/**
	 * 列表View
	 */
	@BindView(R.id.list)
	protected RecyclerView mRecyclerView;

	/**
	 * 当前是否正在加载数据
	 */
	private boolean isLoadding = false;
	/**
	 * 是否有更多的数据
	 */
	private boolean haveMoreData = true;

	/**
	 * 加载中Dialog
	 */
	private Dialog pd = null;


	/**
	 * 加载更多的View
	 */
	@BindView(R.id.chat_swipe_layout)
	protected SwipeRefreshLayout swipeRefreshLayout;

	@BindView(R.id.ek_bar)
	public ChatEmoticonsKeyBoard ekBar;

	@Override
	public void setContentView() {
		setContentView(R.layout.activity_agent_chat);
	}

	/**
	 * 获取Intent传过来的数据
	 */
	private void intentDataParse() {
		Intent intent = getIntent();
		toUser = intent.getParcelableExtra("user");
		hasUnReadMessage = intent.getBooleanExtra("hasUnReadMessage", false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intentDataParse();
		sessionManager = new SessionManager(0, null, toUser, new HDChatListener() {
			@Override
			public void onNewMessage() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mAdapter.refreshSelectLast();
					}
				});
			}
		});

		initView();
		initListener();
		if (hasUnReadMessage){
			//从服务器获取最新消息
			sessionManager.asyncGetAgentUnReadMessages(new HDDataCallBack<List<HDMessage>>() {
				@Override
				public void onSuccess(List<HDMessage> value) {
					if (isFinishing()){
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeDialog();
							mAdapter.refreshSelectLast();
							if (AgentsFragment.callback != null){
								AgentsFragment.callback.onFresh(null);
							}
						}
					});
				}

				@Override
				public void onError(int error, String errorMsg) {
					if (isFinishing()){
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeDialog();
						}
					});
				}
			});
		}

	}


	@OnClick(R.id.rl_back)
	public void onClickByBack(){
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	/**
	 * 注册View的监听
	 */
	private void initListener() {
		mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (ekBar != null){
					ekBar.reset();
				}
				return false;
			}
		});

		ekBar.setAudioFinishRecorderListener(new ChatEmoticonsKeyBoard.AudioFinishRecorderListener() {
			@Override
			public void onFinish(float seconds, String filePath) {
				//发送语音消息
//                android.util.Log.e(TAG, "seconds:" + seconds + ",filePath:" + filePath);
				sendVoiceMessage((int) seconds, filePath);
			}
		});
	}

	/**
	 * 初始化View
	 */
	private void initView(){
		// 如果确定每个item的内容不会改变Recyclerview的大小,设置这个选项可以提高性能
		mRecyclerView.setHasFixedSize(true);

		//创建一个线性布局管理器
		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		//设置布局管理器
		mRecyclerView.setLayoutManager(layoutManager);
		mAdapter = new ChatAdapter(this, sessionManager, mRecyclerView);
		mRecyclerView.setAdapter(mAdapter);
		if (toUser != null && !TextUtils.isEmpty(toUser.getNicename())) {
			tvTitle.setText(toUser.getNicename());
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
						if (firstVisibilePosition <= 0 && !isLoadding && haveMoreData) {
							sessionManager.asyncGetAgentRemoteMessages(new HDDataCallBack<List<HDMessage>>() {
								@Override
								public void onSuccess(final List<HDMessage> value) {
									if (isFinishing()) {
										return;
									}
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											swipeRefreshLayout.setRefreshing(false);
											isLoadding = false;
											if (!value.isEmpty()) {
												mAdapter.refresh();
											} else {
												haveMoreData = false;
												Toast.makeText(mActivity, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
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
							if (isFinishing()) {
								return;
							}
							swipeRefreshLayout.setRefreshing(false);
							Toast.makeText(mActivity, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
						}
					}
				}, 1000);
			}
		});

		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				switch (newState){
					case 0:
						break;
					case 1:
						break;
					case 2:
						ekBar.reset();
						break;
				}
			}
		});


		initEmoticonsKeyBoardBar();

	}


	private void initEmoticonsKeyBoardBar(){

		SimpleCommonUtils.initEmoticonsEditText(ekBar.getEtChat());
		ekBar.setAdapter(SimpleCommonUtils.getCommonAdapter(this, emoticonClickListener));
		ekBar.addOnFuncKeyBoardListener(this);
		ekBar.addFuncView(new SimpleUserDefAppsGridView(this), getExtendAppBeans().size());
		ekBar.getEtChat().setOnSizeChangedListener(new EmoticonsAutoEditText.OnSizeChangedListener() {
			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				scrollToBottom();
			}
		});
		ekBar.getBtnSend().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String inputContent = ekBar.getEtChat().getText().toString().trim();
				if (TextUtils.isEmpty(inputContent)){
					Toast.makeText(mActivity, "内容不能为空!", Toast.LENGTH_SHORT).show();
					return;
				}
				if (inputContent.length() > 1000){
					Toast.makeText(mActivity, "消息太长!", Toast.LENGTH_SHORT).show();
					return;
				}

				OnSendBtnClick(ekBar.getEtChat().getText().toString());
				ekBar.getEtChat().setText("");
			}
		});

	}

	EmoticonClickListener emoticonClickListener = new EmoticonClickListener() {
		@Override
		public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {
			if (isDelBtn) {
				SimpleCommonUtils.delClick(ekBar.getEtChat());
			} else {
				if(o == null){
					return;
				}
				if(actionType == Constants.EMOTICON_CLICK_BIGIMAGE){
					if(o instanceof EmoticonEntity){
						OnSendImage(((EmoticonEntity)o).getIconUri());
					}
				} else {
					String content = null;
					if(o instanceof EmojiBean){
						content = ((EmojiBean)o).emoji;
					} else if(o instanceof EmoticonEntity){
						content = ((EmoticonEntity)o).getContent();
					}

					if(TextUtils.isEmpty(content)){
						return;
					}
					int index = ekBar.getEtChat().getSelectionStart();
					Editable editable = ekBar.getEtChat().getText();
					editable.insert(index, content);
				}
			}
		}
	};


	private void OnSendBtnClick(String msg) {
		if (!TextUtils.isEmpty(msg)) {
			sendText(msg);
			scrollToBottom();
		}
	}

	private void OnSendImage(String image) {
		if (!TextUtils.isEmpty(image)) {
			OnSendBtnClick("[img]" + image);
		}
	}

	private void scrollToBottom() {
		mRecyclerView.requestLayout();
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
				mAdapter.refreshSelectLast();
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		ekBar.reset();
	}


	@Override
	public ArrayList<AppBean> getExtendAppBeans() {
		ArrayList<AppBean> mAppBeanList = new ArrayList<>();
		mAppBeanList.add(new AppBean(1, R.drawable.input_more_icon_camera, "图片"));
		mAppBeanList.add(new AppBean(2, R.drawable.hd_chat_video_normal, "视频"));
		mAppBeanList.add(new AppBean(3, R.drawable.input_more_icon_file, "文件"));
		return mAppBeanList;
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (EmoticonsKeyboardUtils.isFullScreen(this)){
			boolean isConsum = ekBar.dispatchKeyEventInFullScreen(event);
			return isConsum ? true : super.dispatchKeyEvent(event);
		}
		return super.dispatchKeyEvent(event);
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode == RESULT_OK) {
//			if (requestCode == REQUEST_CODE_RESEND) {
//				resendMessage();
//			}else if (requestCode == REQUEST_CODE_SELECT_FILE) {//发送选择的文件
//				if (data != null) {
//					Uri uri = data.getData();
//					if (uri != null) {
//						sendFile(uri);
//					}
//				}
//
//			}else{
//				mAdapter.refresh();
//			}
//		}
//
//	}


	@Override
	public void OnFuncPop(int height) {
		scrollToBottom();
	}

	@Override
	public void OnFuncClose() {

	}
}
