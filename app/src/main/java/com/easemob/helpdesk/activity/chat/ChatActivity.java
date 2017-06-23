package com.easemob.helpdesk.activity.chat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.category.CategoryShowActivity;
import com.easemob.helpdesk.activity.main.MainActivity;
import com.easemob.helpdesk.activity.transfer.TransferActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.emoticon.data.AppBean;
import com.easemob.helpdesk.emoticon.utils.Constants;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.emoticon.view.SimpleUserDefAppsGridView;
import com.easemob.helpdesk.fragment.main.CurrentSessionFragment;
import com.easemob.helpdesk.image.ImageHandleUtils;
import com.easemob.helpdesk.recorder.MediaManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.FaceConversionUtil;
import com.easemob.helpdesk.widget.chatview.ChatEmoticonsKeyBoard;
import com.easemob.helpdesk.widget.popupwindow.SessionCloseWindow;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.hyphenate.kefusdk.HDChatListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.HDCategorySummary;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.HDMessageUser;
import com.hyphenate.kefusdk.entity.HDTextMessageBody;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.manager.SessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.sj.emoji.EmojiBean;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import me.iwf.photopicker.PhotoPickerActivity;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.utils.EmoticonsKeyboardUtils;
import sj.keyboard.widget.EmoticonsAutoEditText;
import sj.keyboard.widget.FuncLayout;


/**
 * Created by liyuzhao on 05/04/2017.
 */

public class ChatActivity extends BaseActivity implements View.OnClickListener,FuncLayout.OnFuncKeyBoardListener  {
	private final String TAG = this.getClass().getSimpleName();

	public static final int REQUEST_CODE_SHORTCUT = 0;
	public static final int REQUEST_CODE_TRANSFER = 1;
	public static final int REQUEST_CODE_STOP_DIALOG = 2;
	public static final int RESULT_CODE_COPY_AND_PASTE = 11;
	public static final int RESULT_CODE_RECALL = 12;
	public static final int REQUEST_CODE_SEND_EVAL_INVIT = 20;


	//重发的RequestCode
	public static final int REQUEST_CODE_RESEND = 21;
	//打开相册
	public static final int REQUEST_CODE_CHOOSE_PICTURE = 26;
	//打开会话小结
	public static final int REQUEST_CODE_CATEGORY_SHOW = 27;
	//打开用户详情
	public static final int REQUEST_CODE_USER_DETAIL = 28;

	public static final int REQUEST_CODE_SELECT_FILE = 29;

	//copy message for contextmenu
	public static final int REQUEST_CODE_CONTEXT_MENU = 31;

	public static final int REQUEST_CODE_PERMISSIONS_CAMERA = 32;
	public static final int REQUEST_CODE_PERMISSIONS_RECORD = 33;

	/**
	 * 每页默认加载消息数
	 */
	private static final int pagesize = 20;

	/**
	 * 渠道显示的LinearLayout
	 */
	@BindView(R.id.ll_channel)
	protected LinearLayout llChannel;
	/**
	 * 渠道显示的图标
	 */
	@BindView(R.id.iv_channel)
	protected ImageView iv_channel;
	/**
	 * 来源信息显示TextView
	 */
	@BindView(R.id.tv_channel_content)
	protected TextView tvChannelText;
	/**
	 * 返回按钮
	 */
	@BindView(R.id.rl_back)
	protected RelativeLayout rlBack;
	/**
	 * 界面标题Title
	 */
	@BindView(R.id.user_name)
	protected TextView tvTitle;
	/**
	 * 标题和来源的父View
	 */
	@BindView(R.id.ll_title_click)
	protected RelativeLayout llTitleClick;

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
	 * 页面适配器
	 */
	public ChatAdapter mAdapter = null;

	/**
	 * 当前会话toUser（目标用户）
	 */
	private HDMessageUser toUser;
	/**
	 * 渠道来源
	 */
	private String originType;
	/**
	 * 渠道名称
	 */
	private String techChannelName;

	/**
	 * 加载中Dialog
	 */
	private Dialog pd = null;
	/**
	 * 会话ID
	 */
	private String sServiceId = null;
	/**
	 * 当前会话是否有未读消息
	 */
	private boolean hasUnReadMessage;

	/**
	 * 需要重发的消息位置
	 */
	public static int resendPos;

	/**
	 * title栏的更多按钮
	 */
	@BindView(R.id.ib_menu_more)
	protected View ibMenuMore;

	/**
	 * 加载更多的View
	 */
	@BindView(R.id.chat_swipe_layout)
	protected SwipeRefreshLayout swipeRefreshLayout;

	/**
	 * 会话关闭确认窗口
	 */
	private SessionCloseWindow closeWindow;

	@BindView(R.id.ek_bar)
	public ChatEmoticonsKeyBoard ekBar;

	private Unbinder unbinder;

	private SessionManager sessionManager;
	private long chatGroupId;

	/**
	 * 标签显示收起按钮
	 */
	@BindView(R.id.btn_up)
	protected ImageButton btnUp;

	/**
	 * 标签显示展开按钮
	 */
	@BindView(R.id.btn_down)
	protected ImageButton btnDown;

	/**
	 * 标签显示整个布局
	 */
	@BindView(R.id.tag_layout)
	protected LinearLayout tagLayout;

	/**
	 * 标签组布局
	 */
	@BindView(R.id.tagview)
	protected TagView tagGroup;

	/**
	 * 标签备注信息View
	 */
	@BindView(R.id.tv_note)
	protected TextView tvNote;
	/**
	 * 备注信息
	 */
	private String commentString;

	/**
	 * 标签整理父布局
	 */
	@BindView(R.id.tag_ll)
	protected View tagLL;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//加载Emoji表情图标
		FaceConversionUtil.getInstace().getFileText(getApplication());
		setContentView(R.layout.activity_chat);
		unbinder =ButterKnife.bind(this);

		//获取Intent传过来的数据
		Intent intent = getIntent();
		sServiceId = intent.getStringExtra("visitorid");
		toUser = intent.getParcelableExtra("user");
		originType = intent.getStringExtra("originType");
		techChannelName = intent.getStringExtra("techChannelName");
		hasUnReadMessage = intent.getBooleanExtra("hasUnReadMessage", false);
		chatGroupId = intent.getLongExtra("chatGroupId", 0);

		sessionManager = new SessionManager(chatGroupId, sServiceId, toUser, new HDChatListener() {
			@Override
			public void onEnquiryChanged() {

			}

			@Override
			public void onNewMessage() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mAdapter.refreshSelectLast();
					}
				});
			}

			@Override
			public void onClosed() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ChatActivity.this.finish();
						HDLog.d(TAG, "end Session:serviceId:" + sServiceId);
					}
				});
			}

			@Override
			public void onClosedByAdmin() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ChatActivity.this.finish();
						HDLog.d(TAG, "endbyadmin Session:serviceId:" + sServiceId);
					}
				});
			}
		});
		initView();
		initListener();
		if (!TextUtils.isEmpty(sServiceId)) {
			tagLL.setVisibility(View.VISIBLE);
			//从服务器获取最新消息
			/**
			 * 异步获取访客和客服间消息from Server
			 */
			sessionManager.asyncLoadRemoteMsg(new HDDataCallBack<List<HDMessage>>() {
				@Override
				public void onSuccess(List<HDMessage> messageEntities) {
					if (isFinishing()){
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeDialog();
							mAdapter.refreshSelectLast();
						}
					});
				}

				@Override
				public void onError(int i, String s) {
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

				@Override
				public void onAuthenticationException() {

				}
			});
			//获取session信息，查询会话是否已经设置,Tag标签
			sessionManager.getCategorySummarys(new HDDataCallBack<List<HDCategorySummary>>() {
				@Override
				public void onSuccess(final List<HDCategorySummary> hdCategorySummaries) {
					if (isFinishing()) {
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setTagViews(hdCategorySummaries);
						}
					});
				}

				@Override
				public void onError(int i, String s) {

				}
			});

			//获取Note信息
			sessionManager.getCommentsFromServer(new HDDataCallBack<String>() {
				@Override
				public void onSuccess(final String value) {
					if (isFinishing()) {
						return;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (TextUtils.isEmpty(value)) {
								commentString = "";
								tvNote.setText("");
							} else {
								commentString = value;
								tvNote.setText(String.format(getString(R.string.tv_text_content), commentString));
							}
						}
					});
				}

				@Override
				public void onError(int error, String errorMsg) {

				}
			});

			if (hasUnReadMessage) {
				setMessageReadedMarkTag();
			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					tagLayout.setVisibility(View.GONE);
				}
			}, 300);
		}
		if (!CommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, "当前无网络!", Toast.LENGTH_SHORT).show();
		}
		try {
			loadPhraseData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		checkVoicePermission();
	}



	private void checkVoicePermission(){
		int hasRecordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
		if (hasRecordPermission != PackageManager.PERMISSION_GRANTED){
			PermissionGen.with(this)
					.addRequestCode(REQUEST_CODE_PERMISSIONS_RECORD)
					.permissions(Manifest.permission.RECORD_AUDIO
					).request();
		}
	}

	@PermissionSuccess(requestCode = REQUEST_CODE_PERMISSIONS_RECORD)
	public void recordAuthSuccess(){


	}

	@PermissionFail(requestCode = REQUEST_CODE_PERMISSIONS_RECORD)
	public void recordAuthFail(){
		new android.app.AlertDialog.Builder(this).setMessage("app需要手机录音权限 \n请在权限管理->麦克风->设为允许!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
		}).create().show();

	}


	/**
	 * 加载常用语
	 */
	private void loadPhraseData(){
		sessionManager.asyncGetPhraseValues(new HDDataCallBack<List<String>>() {
			@Override
			public void onSuccess(final List<String> value) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ekBar.getEtChat().setDatas(value);
					}
				});

			}

			@Override
			public void onError(int error, String errorMsg) {

			}
		});

	}


	public String getOriginType(){
		return originType;
	}

	/**
	 * 注册View的监听
	 */
	private void initListener() {
		btnUp.setOnClickListener(this);
		btnDown.setOnClickListener(this);
		rlBack.setOnClickListener(this);
		ibMenuMore.setOnClickListener(this);
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
				sendVoiceMessage((int) seconds, filePath);
			}
		});
	}

	/**
	 * 初始化View
	 */
	private void initView() {
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
		HDUser loginUser = HDClient.getInstance().getCurrentUser();
		if (loginUser == null){
			finish();
			return;
		}

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
				tvChannelText.setText(String.format(getString(R.string.txt_chat_from), techChannelName));
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
							isLoadding = true;
							sessionManager.asyncLoadMoreMsg(new HDDataCallBack<List<HDMessage>>() {
								@Override
								public void onSuccess(final List<HDMessage> value) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (!value.isEmpty()) {
												mAdapter.refresh();
												if (value.size() != pagesize) {
													haveMoreData = false;
												}
											} else {
												haveMoreData = false;
											}
											swipeRefreshLayout.setRefreshing(false);
											isLoadding = false;

										}
									});
								}

								@Override
								public void onError(int error, String errorMsg) {
									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											isLoadding = false;
											swipeRefreshLayout.setRefreshing(false);
										}
									});
								}

								@Override
								public void onAuthenticationException() {
									isLoadding = false;
								}
							});
						} else {
							Toast.makeText(ChatActivity.this, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
						}
						swipeRefreshLayout.setRefreshing(false);

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

		if (TextUtils.isEmpty(sServiceId)) {
			llChannel.setVisibility(View.GONE);
			tvTitle.setOnClickListener(null);
		} else {
			llChannel.setVisibility(View.VISIBLE);
		}

		initEmoticonsKeyBoardBar();
	}

	private void initEmoticonsKeyBoardBar(){
		SimpleCommonUtils.initEmoticonsEditText(ekBar.getEtChat());
		ekBar.setAdapter(SimpleCommonUtils.getCommonAdapter(this, emoticonClickListener));
		ekBar.addOnFuncKeyBoardListener(this);
		ekBar.addFuncView(new SimpleUserDefAppsGridView(this));
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
					Toast.makeText(ChatActivity.this, "内容不能为空!", Toast.LENGTH_SHORT).show();
					return;
				}
				if (inputContent.length() > 1000){
					Toast.makeText(ChatActivity.this, "消息太长!", Toast.LENGTH_SHORT).show();
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
		MediaManager.pause();
		ekBar.reset();
	}

	/**
	 * 更新当前会话未读数
	 */
	private void updateCurrentSessionUnreadCount() {
		if (CurrentSessionFragment.refreshCallback != null) {
			CurrentSessionFragment.refreshCallback.onRefreshView();
		}
	}

	/**
	 * 为消息设置已读标志
	 */
	private void setMessageReadedMarkTag() {
		sessionManager.setMessageReadedMarkTag(new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						updateCurrentSessionUnreadCount();
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {

			}

			@Override
			public void onAuthenticationException() {

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		MediaManager.resume();
		if (mAdapter != null) {
			mAdapter.refresh();
		}
	}

	public ArrayList<AppBean> getAppBeanList(){
		ArrayList<AppBean> mAppBeanList = new ArrayList<>();
		mAppBeanList.add(new AppBean(1, R.drawable.input_more_icon_camera, "图片"));
		mAppBeanList.add(new AppBean(2, R.drawable.input_more_icon_file, "文件"));
		//这里添加更多的扩展功能
		return mAppBeanList;
	}

	@Override
	public void OnFuncPop(int height) {
		scrollToBottom();
	}

	@Override
	public void OnFuncClose() {

	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (EmoticonsKeyboardUtils.isFullScreen(this)){
			boolean isConsum = ekBar.dispatchKeyEventInFullScreen(event);
			return isConsum ? true : super.dispatchKeyEvent(event);
		}
		return super.dispatchKeyEvent(event);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ib_menu_more:
				if (closeWindow == null) {
					closeWindow = new SessionCloseWindow(this);
				}
				closeWindow.showPopupWindow(ibMenuMore);
				break;
			case R.id.rl_back:
				startActivity(new Intent(this, MainActivity.class));
				finish();
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


	/**
	 * 加载Dialog关闭
	 */
	private void closeDialog() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}


	public void selectPicFromLocal() {
		PermissionGen.with(this).permissions(android.Manifest.permission.CAMERA).addRequestCode(REQUEST_CODE_PERMISSIONS_CAMERA).request();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
		switch(requestCode){
			case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT:
			{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
//                    if (dialog != null){
//                        // Show dialog if the read permission has been granted.
//                        dialog.show();
//                    }
				}else{
					// Permission has not been granted. Notify the user.
					Toast.makeText(ChatActivity.this, "无权限", Toast.LENGTH_SHORT).show();
				}

			}
		}


		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@PermissionSuccess(requestCode = REQUEST_CODE_PERMISSIONS_CAMERA)
	public void selectPicAuthSuccess(){
		//打开相册新方法
		Intent intent = ImageHandleUtils.pickSingleImage(this, true);
		this.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
	}

	@PermissionFail(requestCode = REQUEST_CODE_PERMISSIONS_CAMERA)
	public void selectPicAuthFail(){
		new android.app.AlertDialog.Builder(this).setMessage("拍照需要相机权限 \n请在权限管理->相机->设为允许!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
		}).create().show();
	}





	public void sendText(String txtContent) {
		sendText(txtContent, null);
	}

	/**
	 * 发送带有扩展的自定义文本消息
	 *
	 * @param txtContent 文本内容
	 * @param extJson    扩展消息（JSON格式）
	 */
	public void sendText(String txtContent, JSONObject extJson) {
		HDMessage message = HDMessage.createSendTextMessage(txtContent);
		if (extJson != null) {
			message.setExtJson(extJson);
		}
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
	}

	/**
	 * 选择文件
	 */
	public void selectFileFromLocal() {
		selectFileFromLocalNew();
	}

	public void selectFileFromLocalNew(){
		DialogProperties properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.SINGLE_MODE;
		properties.selection_type = DialogConfigs.FILE_SELECT;
		properties.root = Environment.getExternalStorageDirectory();
		properties.error_dir = Environment.getExternalStorageDirectory();
		properties.offset = new File(DialogConfigs.DEFAULT_DIR);
		properties.extensions = null;

		FilePickerDialog dialog = new FilePickerDialog(this, properties);
		dialog.setTitle("选择要发送的文件");
		dialog.setDialogSelectionListener(new DialogSelectionListener() {
			@Override
			public void onSelectedFilePaths(String[] files) {
				// files is the array of the paths of files selected by the Application User.
				if (files != null && files.length > 0){
					for (String filePath : files){
						sendFileMessage(filePath);
					}
				}
			}
		});
		dialog.show();
	}

	/**
	 * 关闭会话和界面
	 */
	public void closeSessionAndUI() {
		sessionManager.asyncStopSession(new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (CurrentSessionFragment.refreshCallback != null) {
							CurrentSessionFragment.refreshCallback.onRefreshView();
						}
						ChatActivity.this.finish();
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
						Toast.makeText(ChatActivity.this, getString(R.string.toast_op_fail_p_checknet), Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onAuthenticationException() {
			}
		});
	}

	/**
	 * 发送文件消息
	 */
	private void sendFile(Uri uri) {
		String filePath = null;
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = {"_data"};
			Cursor cursor = null;
			try {
				cursor = getContentResolver().query(uri, projection, null, null, null);
				if (cursor != null){
					int column_index = cursor.getColumnIndexOrThrow("_data");
					if (cursor.moveToFirst()) {
						filePath = cursor.getString(column_index);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if (cursor != null){
					cursor.close();
				}
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			filePath = uri.getPath();
		}

		sendFileMessage(filePath);
	}

	private void sendFileMessage(String filePath){
		if (filePath == null){
			return;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			Toast.makeText(this, getString(R.string.toast_file_no_exist), Toast.LENGTH_SHORT).show();
			return;
		}
		if (file.length() > 10 * 1024 * 1024) {
			Toast.makeText(this, getString(R.string.toast_file_nomore_count), Toast.LENGTH_SHORT).show();
			return;
		}
		//创建一个文件消息
		HDMessage message = HDMessage.createSendFileMessage(filePath);
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();

	}



	/**
	 * 发送语音消息
	 */
	private void sendVoiceMessage(final int seconds, final String filePath) {
		HDMessage message = HDMessage.createSendVoiceMessage(filePath, seconds);
		if (message == null){
			return;
		}
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
	}


	/**
	 * 发送图片消息
	 */
	private void sendPicture(final String filePath) {
		if (isFinishing()){
			return;
		}
		HDMessage message = HDMessage.createSendImageMessage(filePath);
		if (message == null){
			return;
		}
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDialog();
		if(unbinder != null){
			unbinder.unbind();
		}
		MediaManager.release();
		sessionManager.clear();
	}

	public boolean isAppChannel(){
		if(!TextUtils.isEmpty(sServiceId)){
			if (originType == null || originType.equalsIgnoreCase("app") || originType.equalsIgnoreCase("webim")){
				return true;
			}
		}
		return false;
	}

	/**
	 * 重发消息
	 */
	private void resendMessage() {
		sessionManager.resendMessage(resendPos);
		mAdapter.refreshSelectLast();
	}

	/**
	 * 为TagGroup 添加数据源
	 *
	 * @param list
	 */
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

	/**
	 * 会话转接点击事件
	 *
	 * @param view
	 */
	public void chat_transfer(View view) {
		popupclose(null);
		startActivityForResult(new Intent(this, TransferActivity.class), REQUEST_CODE_TRANSFER);
	}

	/**
	 * 标签设置点击事件
	 *
	 * @param view
	 */
	public void lable_setting(View view) {
		popupclose(null);
		startActivityForResult(new Intent(ChatActivity.this, CategoryShowActivity.class)
				.putExtra("sessionId", sServiceId)
				.putExtra("summarys", sessionManager.getCategoryTreeValue()), REQUEST_CODE_CATEGORY_SHOW);
	}

	/**
	 * 结束会话提示窗显示事件
	 *
	 * @param view
	 */
	public void chat_end(View view) {
		popupclose(null);
		if (HDClient.getInstance().isStopSessionNeedSummary && !sessionManager.categoryIsSet()) {
			showCategoryTreeDialog();
		} else {
			startActivityForResult(new Intent(this, AlertDialog.class).putExtra("msg", getString(R.string.comfirm_end_session)),
					REQUEST_CODE_STOP_DIALOG);
		}
	}


	/**
	 * 跳转会话标签展示添加界面
	 */
	private void showCategoryTreeDialog() {
		startActivityForResult(new Intent(ChatActivity.this, CategoryShowActivity.class)
				.putExtra("sessionId", sServiceId)
				.putExtra("summarys", sessionManager.getCategoryTreeValue())
				.putExtra("close", true), REQUEST_CODE_CATEGORY_SHOW);
	}


	/**
	 * 当前页面的关闭弹出的关闭事件
	 *
	 * @param view
	 */
	public void popupclose(View view) {
		if (closeWindow != null && closeWindow.isShowing()) {
			closeWindow.dismiss();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			if (resultCode == RESULT_CODE_COPY_AND_PASTE) {
				HDMessage copyMsg = mAdapter.getItem(data.getIntExtra("position", -1));
				CommonUtils.copyText(ChatActivity.this, ((HDTextMessageBody) copyMsg.getBody()).getMessage());
			}else if (resultCode == RESULT_CODE_RECALL){
				closeDialog();
				pd = DialogUtils.getLoadingDialog(this, "撤回中...");
				pd.show();
				final int position = data.getIntExtra("position", -1);
				if (position == -1){
					return;
				}
				final HDMessage recallMsg = mAdapter.getItem(position);
				sessionManager.asyncRecallMessage(recallMsg, new HDDataCallBack<HDMessage>() {
					@Override
					public void onSuccess(HDMessage value) {
						if (isFinishing()) {
							return;
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								closeDialog();
								mAdapter.notifyItemChanged(position);
								Toast.makeText(ChatActivity.this, "消息撤回成功！", Toast.LENGTH_SHORT).show();
							}
						});
					}

					@Override
					public void onError(int error, final String errorMsg) {
						if (isFinishing()) {
							return;
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								closeDialog();
								Toast.makeText(ChatActivity.this, "消息撤回失败！" + errorMsg, Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
			}
		}


		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_CHOOSE_PICTURE) {
				if (data != null) {
					ArrayList<String> picPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
					if (picPathList == null || picPathList.size() == 0) {
						return;
					}
					String picPath = picPathList.get(0);
					sendPicture(picPath);
				}

			} else if (requestCode == REQUEST_CODE_STOP_DIALOG) {
				closeSessionAndUI();
			} else if (requestCode == REQUEST_CODE_RESEND ) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_CATEGORY_SHOW) {
				String newValue = data.getStringExtra("value");
				String comment = data.getStringExtra("comment");
				boolean isClose = data.getBooleanExtra("close", false);
				List<HDCategorySummary> currentCategorySummarys = sessionManager.setCategorySummaryValue(newValue);
				setTagViews(currentCategorySummarys);

				if(comment != null){
					commentString = comment;
					tvNote.setText(String.format(getString(R.string.tv_text_content), commentString));
				}

				if (isClose) {
					closeSessionAndUI();
				}

			} else if (requestCode == REQUEST_CODE_USER_DETAIL) {
				String rNiceName = data.getStringExtra("nicename");
				if (TextUtils.isEmpty(rNiceName)) {
					return;
				}
				tvTitle.setText(rNiceName);
				if (sServiceId == null) {
					return;
				}
				if (CurrentSessionFragment.callback != null) {
					CurrentSessionFragment.callback.onFresh(null);
				}

			} else if (requestCode == REQUEST_CODE_SELECT_FILE) {//发送选择的文件
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						sendFile(uri);
					}
				}

			} else if (requestCode == REQUEST_CODE_TRANSFER) {
				String userId = data.getStringExtra("userId");
				final long queueId = data.getLongExtra("queueId", 0);
				pd = DialogUtils.getLoadingDialog(this, R.string.info_loading);
				pd.show();
				if (!TextUtils.isEmpty(userId) || queueId > 0) {
					sessionManager.transfer(data, new HDDataCallBack<String>() {
						@Override
						public void onSuccess(String value) {
							if (isFinishing()) {
								return;
							}
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									closeDialog();
									if (queueId > 0) {
										Toast.makeText(ChatActivity.this, getString(R.string.info_transfering), Toast.LENGTH_SHORT).show();
									}
									if (CurrentSessionFragment.callback != null) {
										CurrentSessionFragment.callback.onFresh(null);
									}
									finish();
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
									Toast.makeText(ChatActivity.this, getString(R.string.toast_transfer_fail), Toast.LENGTH_SHORT).show();
								}
							});
						}

						@Override
						public void onAuthenticationException() {
							if (queueId > 0) {
								if (isFinishing()){
									return;
								}
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
									}
								});
							}
						}
					});
				} else {
					closeDialog();
					Toast.makeText(this, getString(R.string.toast_transfer_error), Toast.LENGTH_SHORT).show();
				}

			} else {
				mAdapter.refresh();
			}
		}

	}
}
