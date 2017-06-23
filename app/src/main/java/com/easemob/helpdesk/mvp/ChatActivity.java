package com.easemob.helpdesk.mvp;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.CategoryShowActivity;
import com.easemob.helpdesk.activity.chat.CustomWebViewActivity;
import com.easemob.helpdesk.activity.chat.PhraseActivity;
import com.easemob.helpdesk.activity.main.CurrentSessionFragment;
import com.easemob.helpdesk.activity.transfer.TransferActivity;
import com.easemob.helpdesk.activity.visitor.CustomerDetailActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.emoticon.data.AppBean;
import com.easemob.helpdesk.emoticon.utils.Constants;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.emoticon.view.SimpleUserDefAppsGridView;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.IEvalEventListener;
import com.easemob.helpdesk.widget.chatview.ChatEmoticonsKeyBoard;
import com.easemob.helpdesk.widget.popupwindow.SessionCloseWindow;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.hyphenate.kefusdk.HDChatListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.HDCategorySummary;
import com.hyphenate.kefusdk.bean.OptionEntity;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.manager.session.SessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.sj.emoji.EmojiBean;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
 * 聊天界面
 *
 * @author liyuzhao
 */
public class ChatActivity extends BaseChatActivity implements IEvalEventListener, FuncLayout.OnFuncKeyBoardListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final int REQUEST_CODE_SHORTCUT = 0x010;
    public static final int REQUEST_CODE_TRANSFER = 0x011;
    public static final int REQUEST_CODE_STOP_DIALOG = 0x012;
    public static final int REQUEST_CODE_SEND_EVAL_INVIT = 0x015;

    //打开相册
    //打开会话小结
    public static final int REQUEST_CODE_CATEGORY_SHOW = 0x017;
    //打开用户详情
    public static final int REQUEST_CODE_USER_DETAIL = 0x018;

    /**
     * 发送自定义消息
     * send custom message
     */
    public static final int REQUEST_CODE_SEND_EXT_MSG = 0x019;

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
     * 监听到满意度变化回调
     */
    public static IEvalEventListener evalEventListener;

    /**
     * 返回按钮显示的未读消息View
     */
    @BindView(R.id.tv_unread_msg)
    protected TextView tvUnReadMsg;

    /**
     * title栏的更多按钮
     */
    @BindView(R.id.ib_menu_more)
    protected View ibMenuMore;

    /**
     * 是否发送过满意度评价申请
     */
    private String isSendEvalState = null;


    /**
     * 加载更多的View
     */
    @BindView(R.id.chat_swipe_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    /**
     * 服务器配置信息
     */
    private OptionEntity extOptionEntity;

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

    @BindView(R.id.seesion_extra_info)
    protected TextView sessionExtraInfo;
    /**
     * 会话关闭确认窗口
     */
    private SessionCloseWindow closeWindow;

    @BindView(R.id.ek_bar)
    public ChatEmoticonsKeyBoard ekBar;

    private long chatGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        evalEventListener = this;
        intentDataParse();
        sessionManager = new SessionManager(chatGroupId, sessionId, toUser, new HDChatListener() {
            @Override
            public void onEnquiryChanged() {
                // 获取满意度评价状态
                sessionManager.getEvalStatus(toUser.getTenantId(), new HDDataCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        updateEvalStatus(value);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {

                    }
                });
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
                        finish();
                        HDLog.d(TAG, "end Session:serviceId:" + sessionId);
                    }
                });
            }

            @Override
            public void onClosedByAdmin() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        HDLog.d(TAG, "endbyadmin Session:serviceId:" + sessionId);
                    }
                });
            }
        });
        initView();
        tagLL.setVisibility(View.VISIBLE);
        //从服务器获取最新消息
        /**
         * 异步获取访客和客服间消息from Server
         */
        sessionManager.asyncLoadRemoteMsg(new HDDataCallBack<List<HDMessage>>() {
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

        // 获取满意度评价状态
        sessionManager.getEvalStatus(toUser.getTenantId(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                updateEvalStatus(value);
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });

        //获取session信息，查询会话是否已经设置,Tag标签
        sessionManager.getCategorySummarys(new HDDataCallBack<List<HDCategorySummary>>() {
            @Override
            public void onSuccess(final List<HDCategorySummary> value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTagViews(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });

        //获取自定义消息
        getExtOptionUrl();
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
        ekBar.setAudioFinishRecorderListener(new ChatEmoticonsKeyBoard.AudioFinishRecorderListener() {
            @Override
            public void onFinish(float seconds, String filePath) {
                //发送语音消息
//                android.util.Log.e(TAG, "seconds:" + seconds + ",filePath:" + filePath);
                sendVoiceMessage((int) seconds, filePath);
            }
        });
        if (hasUnReadMessage) {
            setMessageReadedMarkTag();
            updateCurrentSessionUnreadCount();
        }

        getSessionExtraInfo();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tagLayout!= null) {
                    tagLayout.setVisibility(View.GONE);
                }
            }
        }, 300);

        try {
            loadPhraseData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_chat);
    }

    /**
     * 获取Intent传过来的数据
     */
    private void intentDataParse() {
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("visitorid");
        toUser = intent.getParcelableExtra("user");
        originType = intent.getStringExtra("originType");
        techChannelName = intent.getStringExtra("techChannelName");
        hasUnReadMessage = intent.getBooleanExtra("hasUnReadMessage", false);
        chatGroupId = intent.getLongExtra("chatGroupId", 0);
    }

    /**
     * 加载常用语
     */
    private void loadPhraseData(){
        sessionManager.asyncGetPhraseValues(new HDDataCallBack<List<String>>() {
            @Override
            public void onSuccess(final List<String> value) {
                if (isFinishing()){
                    return;
                }
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



    /**
     * 获取自定义消息设置信息
     */
    private void getExtOptionUrl() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    extOptionEntity = HDClient.getInstance().agentManager().getOptionEntity("imgMsgSender");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (extOptionEntity != null && !TextUtils.isEmpty(extOptionEntity.getOptionValue())) {
                                if (extOptionEntity.getOptionValue().length() > 3) {
//                                    extMsgLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
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
                            sessionManager.asyncLoadMoreMsg(new HDDataCallBack<List<HDMessage>>() {
                                @Override
                                public void onSuccess(final List<HDMessage> value) {
                                    if (isFinishing()){
                                        return;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (value.isEmpty()){
                                                haveMoreData = false;
                                                Toast.makeText(ChatActivity.this, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
                                            }else{
                                                mAdapter.refresh();
                                            }
                                            if (swipeRefreshLayout != null){
                                                swipeRefreshLayout.setRefreshing(false);
                                            }
                                            isLoadding = false;
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
                                            isLoadding = false;
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }

                                @Override
                                public void onAuthenticationException() {

                                }
                            });
                        } else {
                            Toast.makeText(ChatActivity.this, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
                        }
                        if (swipeRefreshLayout != null){
                            swipeRefreshLayout.setRefreshing(false);
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

        notifyChangeUnreadMsgCount();
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
        ekBar.reset();
    }

    /**
     * 更新未读数
     */
    private void notifyChangeUnreadMsgCount() {
        int unreadCount = HDApplication.getInstance().getUnReadMsgCount();
        if (unreadCount > 0) {
            tvUnReadMsg.setText("(" + unreadCount + ")");
        } else {
            tvUnReadMsg.setText("");
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

    /**
     * 更新当前会话未读数
     */
    private void updateCurrentSessionUnreadCount() {
        if (CurrentSessionFragment.refreshCallback != null) {
            CurrentSessionFragment.refreshCallback.onRefreshView();
        }
        notifyChangeUnreadMsgCount();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.refresh();
        }
    }

    @Override
    public ArrayList<AppBean> getExtendAppBeans() {
        ArrayList<AppBean> mAppBeanList = new ArrayList<>();
        mAppBeanList.add(new AppBean(1, R.drawable.input_more_icon_camera, "图片"));
        mAppBeanList.add(new AppBean(2, R.drawable.hd_chat_video_normal, "视频"));
        mAppBeanList.add(new AppBean(3, R.drawable.input_more_icon_file, "文件"));
        if(!TextUtils.isEmpty(sessionId)){
            mAppBeanList.add(new AppBean(4, R.drawable.input_more_icon_phrase, "常用语"));
            mAppBeanList.add(new AppBean(5, R.drawable.input_more_icon_link, "自定义消息"));
        }
        return mAppBeanList;
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

    public void updateEvalStatus(String evalState) {
        isSendEvalState = evalState;
    }

    @Override
    public void OnFuncPop(int height) {
        scrollToBottom();
    }

    @Override
    public void OnFuncClose() {

    }

    /**
     * 标题名称点击事件
     */
    @OnClick(R.id.ll_title_click)
    public void onClickByDetail(){
        Intent intent = new Intent();
        intent.putExtra("visitorId", sessionId);
        intent.putExtra("userId", toUser.getUserId());
        intent.putExtra("tenantId", toUser.getTenantId());
        intent.setClass(ChatActivity.this, CustomerDetailActivity.class);
        startActivity(intent);

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (EmoticonsKeyboardUtils.isFullScreen(this)){
            boolean isConsum = ekBar.dispatchKeyEventInFullScreen(event);
            return isConsum ? true : super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
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
     * 满意度评价邀请点击事件
     *
     * @param view
     */
    public void eval_send(View view) {
        popupclose(null);
        if (isSendEvalState == null || isSendEvalState.equalsIgnoreCase("none")) {
            Intent evalIntent = new Intent();
            evalIntent.setClass(this, AlertDialog.class);
            evalIntent.putExtra("msg", getString(R.string.comfirm_send_eval_invit));
            evalIntent.putExtra("okString", getString(R.string.txt_send));
            startActivityForResult(evalIntent, REQUEST_CODE_SEND_EVAL_INVIT);
        } else if (isSendEvalState.equalsIgnoreCase("invited")) {
            Toast.makeText(getApplicationContext(), getString(R.string.info_sended_noresend), Toast.LENGTH_SHORT).show();
        } else if (isSendEvalState.equalsIgnoreCase("over")) {
            Toast.makeText(getApplicationContext(), getString(R.string.has_evaled), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 标签设置点击事件
     *
     * @param view
     */
    public void lable_setting(View view) {
        popupclose(null);
        startActivityForResult(new Intent(ChatActivity.this, CategoryShowActivity.class)
                .putExtra("sessionId", sessionId)
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
                .putExtra("sessionId", sessionId)
                .putExtra("summarys", sessionManager.getCategoryTreeValue())
                .putExtra("close", true), REQUEST_CODE_CATEGORY_SHOW);
    }

    @OnClick(R.id.rl_back)
    public void onClickByBack(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @OnClick(R.id.ib_menu_more)
    public void onClickByMore(){
        if (closeWindow == null) {
            closeWindow = new SessionCloseWindow(this);
        }
        closeWindow.showPopupWindow(ibMenuMore);
    }

    @OnClick(R.id.btn_up)
    public void onClickByTagUp(){
        tagLayout.setVisibility(View.GONE);
        btnUp.setVisibility(View.INVISIBLE);
        btnDown.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_down)
    public void onClickByTagDown(){
        tagLayout.setVisibility(View.VISIBLE);
        btnUp.setVisibility(View.VISIBLE);
        btnDown.setVisibility(View.INVISIBLE);
    }




    /**
     * 满意度评价发送
     */
    private void sendEvalClick() {
        pd = DialogUtils.getLoadingDialog(this, R.string.info_sending);
        pd.show();
        sessionManager.asyncSendEvalInvite(toUser.getTenantId(), new HDDataCallBack<String>() {

            @Override
            public void onSuccess(String value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        isSendEvalState = "invited";
                        closeDialog();
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_send_success), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "sendEvalClick:" + errorMsg);
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_send_fail), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HDApplication.getInstance().logout();
                    }
                });

            }
        });
    }


    public void toPhraseUI(){
        Intent intent = new Intent();
        intent.setClass(this, PhraseActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SHORTCUT);
    }


    public void toCustomWebView(){
        if (extOptionEntity == null) {
            return;
        }
        String url = extOptionEntity.getOptionValue();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (url.startsWith("//")) {
            url = "http:" + url;
        }

        if (!url.startsWith("http")){
            return;
        }

        Intent webViewIntent = new Intent();
        webViewIntent.setClass(this, CustomWebViewActivity.class);
        webViewIntent.putExtra("url", url);
        String title = CommonUtils.getTitleFromUrlParam(url);
        if (!TextUtils.isEmpty(title)) {
            webViewIntent.putExtra("title", title);
        }
        startActivityForResult(webViewIntent, REQUEST_CODE_SEND_EXT_MSG);
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
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
        evalEventListener = null;
    }


    @Override
    public void onEvalEventListener(String sessionServiceId, String visitorUserId, String agentUserId) {
        if (sessionServiceId != null && sessionServiceId.equals(sessionId)) {
            isSendEvalState = "over";
        }
    }


    public boolean isAppChannel(){
        if(!TextUtils.isEmpty(sessionId)){
            if (originType == null || originType.equalsIgnoreCase("app") || originType.equalsIgnoreCase("webim")){
                return true;
            }
        }
        return false;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SHORTCUT) {
                String stcontent = data.getStringExtra("content");
                ekBar.getEtChat().setText(stcontent);
                ekBar.getEtChat().setSelection(ekBar.getEtChat().getText().length());
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
                                        HDApplication.getInstance().logout();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    closeDialog();
                    Toast.makeText(this, getString(R.string.toast_transfer_error), Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == REQUEST_CODE_STOP_DIALOG) {
                closeSessionAndUI();
            } else if (requestCode == REQUEST_CODE_SEND_EVAL_INVIT) {
                sendEvalClick();
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
                if (sessionId == null) {
                    return;
                }
                if (CurrentSessionFragment.callback != null) {
                    CurrentSessionFragment.callback.onFresh(null);
                }

            }else if (requestCode == REQUEST_CODE_SEND_EXT_MSG) {
                //发送自定义消息
                String extString = data.getStringExtra("ext");
                try {
                    JSONObject extJson = new JSONObject(extString);
                    sendText(getString(R.string.txt_custom_message), extJson);
                } catch (JSONException e) {
                    Toast.makeText(ChatActivity.this, getString(R.string.txt_style_error), Toast.LENGTH_SHORT).show();
                }


            }else{
                mAdapter.refresh();
            }
        }

    }

    private void getSessionExtraInfo() {
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