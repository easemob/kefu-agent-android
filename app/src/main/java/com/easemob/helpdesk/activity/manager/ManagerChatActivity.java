package com.easemob.helpdesk.activity.manager;

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
import com.easemob.helpdesk.HDApplication;
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
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.option.OptionEntity;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.entity.user.HDVisitorUser;
import com.hyphenate.kefusdk.manager.session.SessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.JsonUtils;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/21.
 */
public class ManagerChatActivity extends BaseActivity {
    private final String TAG = "ManagerChatActivity";

    private static final int REQUEST_CODE_CATEGORY_SHOW = 0x01;

    public ChatAdapter mAdapter;

    @BindView(R.id.history_listview)
    public RecyclerView mRecyclerView;

    private ProgressDialog pd = null;
    private HDVisitorUser toUser;
    private String sServiceId = null;

    @BindView(R.id.btn_call_back)
    public Button btnCallBack;

    @BindView(R.id.ll_channel)
    public LinearLayout llChannel;

    @BindView(R.id.iv_channel)
    public ImageView iv_channel;

    @BindView(R.id.tv_channel_content)
    public TextView tvChannelText;

    private String originType;
    private String techChannelName;
    private long chatGroupId;

    /**
     * 加载更多的View
     */
    @BindView(R.id.chat_swipe_layout)
    public SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.user_name)
    public TextView tvTitle;

//    @BindView(R.id.ll_title_click)
//    public View llTitleClick;
    @BindView(R.id.ib_menu_more)
    public ImageButton ibMenuMore;

    @BindView(R.id.btn_up)
    public ImageButton btnUp;
    @BindView(R.id.btn_down)
    public ImageButton btnDown;

    @BindView(R.id.iv_show_label) protected ImageView ivShowLabel;
    @BindView(R.id.tag_layout)
    public LinearLayout tagLayout;
    @BindView(R.id.tagview)
    public TagView tagGroup;
    @BindView(R.id.seesion_extra_info)
    protected TextView sessionExtraInfo;

//    protected String categoryTreeValue;
//    protected List<HDCategorySummary> tagList;
    @BindView(R.id.tv_note)
    public TextView tvNote;
    protected String commentString;

    /**
     * 是否有更多的数据
     */
    private boolean haveMoreData = true;
    /**
     * 当前是否正在加载数据
     */
    private boolean isLoadding = false;

    private Unbinder unbinder;
    private SessionManager sessionManager;

    private boolean fromAlarms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_history_chat);
        unbinder = ButterKnife.bind(this);
        Intent intent = getIntent();
        sServiceId = intent.getStringExtra("sessionId");
        toUser = intent.getParcelableExtra("user");
        originType = intent.getStringExtra("originType");
        chatGroupId = intent.getLongExtra("chatGroupId", 0);
        techChannelName = intent.getStringExtra("techChannelName");
        boolean hasCallback = intent.getBooleanExtra("callback", false);
        btnCallBack.setVisibility(hasCallback ? View.VISIBLE : View.GONE);
        fromAlarms = intent.getBooleanExtra("fromAlarms", false);
        if (!fromAlarms) {
            initDatas();
            LoadRemoteMsg();
            if (!TextUtils.isEmpty(sServiceId)){
                //获取tagsView
                getTagsFromRemote();
                getCommentsFromRemote();
            }
        } else {
            if (pd == null){
                pd = new ProgressDialog(this);
                pd.setMessage(getResources().getString(R.string.loading_getdata));
            }
            if (!pd.isShowing()) {
                pd.show();
            }
            HelpDeskManager.getInstance().getSessionHistory(sServiceId, new HDDataCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    try {
                        JSONObject sessionMsg = new JSONObject(value);
                        JSONObject sessionEntity = sessionMsg.getJSONObject("entity");
                        if (sessionEntity != null) {
                            if (sessionEntity.has("session_id")) {
                                sServiceId = sessionEntity.getString("session_id");
                            }
                            toUser = new HDVisitorUser();
                            JSONObject visitorUser = sessionEntity.getJSONObject("visitor");
                            if (visitorUser != null) {
                                toUser.setUserId(visitorUser.getString("user_id"));
                                toUser.setNicename(visitorUser.getString("nick_name"));
                                toUser.setUsername(visitorUser.getString("username"));
                            }
                            if (sessionEntity.has("origin_type")) {
                                originType = sessionEntity.getJSONArray("origin_type").getString(0);
                            }
                            if (sessionEntity.has("chat_group_id")) {
                                chatGroupId = sessionEntity.getLong("chat_group_id");
                            }
                            if (sessionEntity.has("channel_name")) {
                                techChannelName = sessionEntity.getString("channel_name");
                            }
                            final boolean hasCallback = sessionEntity.getString("state").equals("Terminal");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (btnCallBack != null) {
                                        btnCallBack.setVisibility(hasCallback ? View.VISIBLE : View.GONE);
                                    }
                                    initDatas();
                                    LoadRemoteMsg();
                                    if (!TextUtils.isEmpty(sServiceId)){
                                        //获取tagsView
                                        getTagsFromRemote();
                                        getCommentsFromRemote();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(int error, String errorMsg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                            Toast.makeText(ManagerChatActivity.this, "获取会话信息异常", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onAuthenticationException() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                            Toast.makeText(ManagerChatActivity.this, "获取会话信息权限异常", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

    }

    private void initDatas(){
        if (pd == null){
            pd = new ProgressDialog(this);
            pd.setMessage(getResources().getString(R.string.loading_getdata));
        }
        if (!pd.isShowing()) {
            pd.show();
        }
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        if (toUser != null && !TextUtils.isEmpty(toUser.getNicename())){
            tvTitle.setText(toUser.getNicename().trim());
        }
        sessionManager = new SessionManager(chatGroupId, sServiceId, toUser, null);
        mAdapter = new ChatAdapter(this, sessionManager, mRecyclerView);
        getSessionExtraInfo();
        mRecyclerView.setAdapter(mAdapter);
        llChannel.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(originType)){
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
            if (!TextUtils.isEmpty(techChannelName)){
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
                                    if (isFinishing()){
                                        return;
                                    }
                                    isLoadding = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (swipeRefreshLayout != null){
                                                swipeRefreshLayout.setRefreshing(false);
                                            }
                                            if (value.isEmpty()){
                                                haveMoreData = false;
                                                Toast.makeText(ManagerChatActivity.this, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
                                            }else {
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
                            Toast.makeText(ManagerChatActivity.this, getString(R.string.txt_no_more_message), Toast.LENGTH_SHORT).show();
                        }
                        if (swipeRefreshLayout != null){
                            swipeRefreshLayout.setRefreshing(false);
                        }


                    }
                }, 1000);
            }
        });


    }

    private void LoadRemoteMsg(){
        sessionManager.asyncGetSessionMessages(new HDDataCallBack<List<HDMessage>>() {
            @Override
            public void onSuccess(List<HDMessage> value) {
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
                if (isFinishing()) {
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


    @OnClick(R.id.ll_title_click)
    public void onClickByllTitle(View view){
        if (TextUtils.isEmpty(sServiceId)){
            return;
        }
        HDUser hdUser = HDClient.getInstance().getCurrentUser();
        if (hdUser == null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("visitorId", sServiceId);
        intent.putExtra("userId", toUser.getUserId());
        intent.putExtra("tenantId", hdUser.getTenantId());
        intent.setClass(mActivity, CustomerDetailActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_call_back)
    public void onClickByCallback(View view){
        if (pd == null) {
            pd = new ProgressDialog(this);
        }
        pd.setMessage("正在回呼...");
        pd.show();

        HelpDeskManager.getInstance().getCreateSessionService(toUser.getUserId(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (isFinishing()) {
                    return;
                }
                HDLog.d(TAG, "getCreateSessionService->" + value);
                final HDSession sessionEntty = JsonUtils.getSessionEntityByCallback(value);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "回呼成功！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(ManagerChatActivity.this, ChatActivity.class);
                        intent.putExtra("visitorid", sessionEntty.getServiceSessionId());
                        intent.putExtra("techChannelName", sessionEntty.getTechChannelName());
                        intent.putExtra("user", sessionEntty.getUser());
                        intent.putExtra("chatGroupId", sessionEntty.getChatGroupId());
                        startActivity(intent);
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
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "回呼失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
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

    @OnClick(R.id.iv_back)
    public void onClickByBack(View view){
        finish();
    }


    @OnClick(R.id.iv_close) public void onClickByTagUp() {
        tagLayout.setVisibility(View.GONE);
        ivShowLabel.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.iv_show_label) public void onClickByTagDown() {
        tagLayout.setVisibility(View.VISIBLE);
        ivShowLabel.setVisibility(View.GONE);
    }

    HistorySessionMore sessionMoreWindow;

    public void sessionMore(View view) {
        if (sessionMoreWindow == null) {
            sessionMoreWindow = new HistorySessionMore(this);
        }
        sessionMoreWindow.showPopupWindow(ibMenuMore);
    }


    public void lable_setting(View view) {
        if (sessionMoreWindow != null) {
            sessionMoreWindow.dismiss();
        }
        startActivityForResult(new Intent(this, CategoryShowActivity.class).putExtra("sessionId", sServiceId).putExtra("summarys", sessionManager.getCategoryTreeValue()), REQUEST_CODE_CATEGORY_SHOW);
//
    }

    private void getTagsFromRemote(){
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

    private void setTagViews(List<HDCategorySummary> list){
        Tag tag;
        if (list == null || list.size() == 0){
            tagGroup.addTags(new java.util.ArrayList<Tag>());
            return;
        }
        ArrayList<Tag> tags = new ArrayList<>();
        for (int i = 0; i < list.size(); i++){
            HDCategorySummary entty = list.get(i);
            String rootName = (TextUtils.isEmpty(entty.rootName))? "" : entty.rootName + ">";
            tag = new Tag(rootName + entty.name);
            tag.id = entty.id;
            tag.radius = 10f;
            int color = (int)entty.color;
            String strColor;
            if (color == 0){
                strColor = "#000000";
            }else if (color == 255){
                strColor = "#ffffff";
            }else{
                strColor = "#" + Integer.toHexString(color);
                strColor = strColor.substring(0, 7);
            }
            tag.layoutColor = Color.parseColor(strColor);
            tag.isDeletable = false;
            tags.add(tag);
        }
        tagGroup.addTags(tags);
    }

    private void getCommentsFromRemote(){
        sessionManager.getCommentsFromServer(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commentString = value;
                        if (TextUtils.isEmpty(commentString)){
                            tvNote.setText("");
                        }else{
                            tvNote.setText("备注:" + commentString);
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_CODE_CATEGORY_SHOW){
                String newValue = data.getStringExtra("value");
                String comment = data.getStringExtra("comment");
                if (!newValue.equals(sessionManager.getCategoryTreeValue())){
                    getTagsFromRemote();
                }
                if (commentString == null){
                    commentString = "";
                }
                if (comment == null){
                    comment = "";
                }
                if (!commentString.equals(comment)){
                    getCommentsFromRemote();
                }
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null){
            unbinder.unbind();
        }
        closeDialog();
        MediaManager.release();
    }

    private void closeDialog(){
        if (pd != null && pd.isShowing()){
            pd.dismiss();
        }
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
