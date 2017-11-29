package com.easemob.helpdesk.mvp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.agent.AgentProfileActivity;
import com.easemob.helpdesk.activity.history.HistorySessionActivity;
import com.easemob.helpdesk.activity.main.AgentsFragment;
import com.easemob.helpdesk.activity.main.CurrentSessionFragment;
import com.easemob.helpdesk.activity.main.LeaveMessageGroupFragment;
import com.easemob.helpdesk.activity.main.NoticeFragment;
import com.easemob.helpdesk.activity.main.SessionFragment;
import com.easemob.helpdesk.activity.main.WaitAccessFragment;
import com.easemob.helpdesk.activity.manager.ManagerHomeActivity;
import com.easemob.helpdesk.activity.visitor.CustomersCenterActivity;
import com.easemob.helpdesk.adapter.FragmentViewPagerAdapter;
import com.easemob.helpdesk.adapter.SlidingMenuListAdapter;
import com.easemob.helpdesk.entity.SlidingMenuItemEntity;
import com.easemob.helpdesk.entity.TabEntity;
import com.easemob.helpdesk.mvp.presenter.MainPresenter;
import com.easemob.helpdesk.mvp.view.IMainView;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.HDNotifier;
import com.easemob.helpdesk.widget.pickerview.MaxAccessPickerView;
import com.easemob.helpdesk.widget.pickerview.StatusPickerView;
import com.easemob.slidingmenu.lib.SlidingMenu;
import com.flyco.roundview.RoundTextView;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.HDEventListener;
import com.hyphenate.kefusdk.HDNotifierEvent;
import com.hyphenate.kefusdk.entity.option.OptionEntity;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.gsonmodel.main.ExpireInfo;
import com.hyphenate.kefusdk.manager.session.CloseSessionManager;
import com.hyphenate.kefusdk.manager.session.OverTimeSessionManager;
import com.hyphenate.kefusdk.utils.HDLog;
import com.liyuzhao.badger.BadgeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 主界面
 *
 * @author liyuzhao
 */
public class MainActivity extends BaseActivity implements IMainView {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Agent change request code
     */
    private static final int REQUEST_CODE_AGENT_CHANGE = 0x01;
    private static final int REQUEST_CODE_MANAGE_HOME = 0x02;

    /**
     * SessionFragment (MainTab)
     */
    private SessionFragment mySessionFragment;

    /**
     * NoticeFragment (MainTab)
     */
    private NoticeFragment mNoticeFragment;

    /**
     * WaitAccessFragment (MainTab)
     */
    private WaitAccessFragment mWaitAccessFragment;

    /**
     * LeaveMessageFragment (MainTab)
     */
    //private LeaveMessageFragment mLeaveMessageFragment;
    private LeaveMessageGroupFragment mLeaveMessageFragment;

    /**
     * alertdialog (info message)
     */
    private Dialog mAlertDialog;

    /**
     * 转接确认dialog
     */
    private Dialog mTransferConfirmDialog;


    private TextView tvDialogMessage;
    /**
     * ViewPager
     */
    private ViewPager mViewPager;

    /**
     * Fragment Datas
     */
    private List<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles = {"会话", "待接入", "消息中心", "留言"};

    private int currentSelectedIndex = 0;

    /**
     * 选中的图标
     */
    private int[] mIconSelectIds = {
            R.drawable.tabbar_icon_ongoing_select, R.drawable.tabbar_icon_waiter_select, R.drawable.tabbar_icon_notice_select, R.drawable.tabbar_icon_ticket_select
    };

    /**
     * 选中的图标
     */
    private int[] mIconUnselectIds = {
            R.drawable.tabbar_icon_ongoing, R.drawable.tabbar_icon_waiter, R.drawable.tabbar_icon_notice, R.drawable.tabbar_icon_ticket
    };

    private int[] mIconIds = {R.id.tabbar_icon_ongoing, R.id.tabbar_icon_waiter, R.id.tabbar_icon_notice, R.id.tabbar_icon_ticket};

    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

    /**
     * Bottom Tab bar
     */
    private CommonTabLayout mTabLayout;

    /**
     * 左边菜单
     */
    private SlidingMenu menu;

    /**
     * 菜单列表 （左边菜单）
     */
    private ListView menuList;

    /**
     * 左边菜单适配器
     */
    private SlidingMenuListAdapter menuAdapter;

    /**
     * 用户昵称 （左边菜单）
     */
    private TextView tvMenuNick;

    /**
     * 用户状态 （左边菜单）
     */
    private TextView tvMenuStatus;

    /**
     * 用户头像 （左边菜单）
     */
    private ImageView ivAvatar;

    /**
     * 用户状态 （左边菜单）
     */
    private ImageView ivStatus;

    List<SlidingMenuItemEntity> items = new ArrayList<>();
    private StatusPickerView statusPickerView;
    private MaxAccessPickerView maxAccessPickerView;

    private int currentSessionCount, currentWaitCount, currentNoticeCount;

    private LinearLayout modelChangeLayout;

    private MainPresenter mainPresenter = new MainPresenter(this);

//    private HDMessageListener messageListener;
    private HDEventListener eventListener;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            currentSelectedIndex = savedInstanceState.getInt("selectedIndex", 0);
        }
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_main);
        HDApplication.getInstance().avatarBitmap = null;
        initView();
        registerMessageListener();
        HDClient.getInstance().chatManager().getTenantOptions(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSlidingMenu();
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
        mainPresenter.getTechChannel();
        mainPresenter.getInitData();
        configureSlidingMenu();

        if (getIntent().getBooleanExtra("displayExpireInfo", false)) {
            getExpireInfo();
        }

    }

    private void updateMaxAccessButtonVisible () {
        if (mySessionFragment != null) {
            mySessionFragment.setSettingButtonVisible(MaxAccessPickerView.isModifiable());
        }
    }

    /**
     * 获取租户状态
     */
    private void getExpireInfo() {
        HDClient.getInstance().getExpireInfo(new HDDataCallBack<ExpireInfo.EntityBean>() {
            @Override
            public void onSuccess(final ExpireInfo.EntityBean value) {
                final HDUser currentUser = HDClient.getInstance().getCurrentUser();

                if(currentUser == null) {
                    return;
                }

                if(value == null) {
                    return;
                }

                if (value.getStatus() == null) {
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        StringBuilder sb = new StringBuilder().append("您的租户ID: " + currentUser.getTenantId()).append("\n");
                        String status;
                        if(value.getStatus().equals("Disable")) {
                            status = "已过期";
                        } else if(value.getStatus().equals("Enable") || value.getStatus().equals("Eanble")){
                            status = "正常";
                        } else {
                            status = "状态异常";
                        }
                        sb.append("租户状态: ").append(status).append("\n")
                          .append("剩余天数: ").append(value.getRemainDays()).append("\n")
                          .append("到期时间: ").append(dateFormat.format(new Date(value.getAgreementExpireTime())));
                        new AlertDialog.Builder(MainActivity.this)
                                .setCancelable(false)
                                .setTitle("租户到期提醒")
                                .setMessage(sb.toString())
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        if (value.getStatus().equals("Disable")) {
                                            HDClient.getInstance().logout(new HDDataCallBack() {
                                                @Override
                                                public void onSuccess(Object value) {
                                                    if (isFinishing()){
                                                        return;
                                                    }
                                                    runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                            closeDialog();
                                                            HDApplication.getInstance().finishAllActivity();
                                                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                        }
                                                        });
                                                }

                                                @Override
                                                public void onError(int error, String errorMsg) {
                                                    runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                            closeDialog();
                                                            Toast.makeText(MainActivity.this, "退出失败，请检查网络！", Toast.LENGTH_SHORT).show();
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
                                                            closeDialog();
                                                            HDApplication.getInstance().logout();
                                                        }
                                                        });
                                                }
                                            });

                                        }
                                    }
                                    }).show();
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
     * 消息监听
     */
    private void registerMessageListener() {
        if (eventListener == null){
            eventListener = new HDEventListener() {
                @Override
                public void onEvent(final HDNotifierEvent event) {
                    final Object data = event.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                    switch (event.getEvent()){
                        case EventNewMessage:
                            if (data instanceof HDMessage){
                                HDMessage message = (HDMessage) data;
                                if (!message.isAgentChat()){
                                    if (CurrentSessionFragment.refreshCallback != null) {
                                        CurrentSessionFragment.refreshCallback.onRefreshView();
                                        HDNotifier.getInstance().notifiChatMsg(message);
                                    }
                                }else{
                                    if (AgentsFragment.callback != null) {
                                        HDNotifier.getInstance().notifiChatMsg(message);
                                        AgentsFragment.callback.onFresh(null);
                                    }
                                }
                                if (mySessionFragment != null){
                                    mySessionFragment.refreshTabBarUnread(message.isAgentChat());
                                }
                            }

                            break;

                        case EventNewSession:
                            if (CurrentSessionFragment.callback != null) {
                                HDNotifier.getInstance().notifiChatMsg(null);
                                CurrentSessionFragment.callback.onFresh(null);
                            }
                            break;
                        case EventAgentUserChange:
                            if (AgentsFragment.callback != null) {
                                AgentsFragment.callback.onFresh(null);
                            }
                            break;
                        case EventCurrentUserDelete:
                            tipCurrentUserDeleted();
                            break;
                        case EventVisitorWaitListChange:
                            if (WaitAccessFragment.callback != null) {
                                WaitAccessFragment.callback.onFresh(null);
                            }
                            break;
                        case EventEnquiryChange:
                            if (data instanceof List){
                                List<JSONObject> jsonList = (List<JSONObject>) data;
                                if (jsonList != null && !jsonList.isEmpty()) {
                                    for (JSONObject jsonObject : jsonList) {
                                        try {
                                            JSONObject jsonSecond = jsonObject.getJSONObject("body");
                                            String sServiceId = jsonSecond.getString("serviceSessionId");
                                            String agentUserId = jsonSecond.getString("agentUserId");
                                            String visitorUserId = jsonSecond.getString("visitorUserId");
                                            if (ChatActivity.evalEventListener != null) {
                                                ChatActivity.evalEventListener.onEvalEventListener(sServiceId, visitorUserId, agentUserId);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            HDLog.e(TAG, e.toString());
                                        }
                                    }
                                }

                            }
                            break;
                        case EventSessionClosed:
                            OverTimeSessionManager.getInstance().notifyListeners((String) data);
                            break;
                        case EventRoleChangeToAdmin:
                        case EventRoleChangeToAgent:
                            tipAgentRoleChange();
                            HDClient.getInstance().chatManager().getTenantOptions(new HDDataCallBack<String>() {
                                @Override
                                public void onSuccess(String value) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateSlidingMenu();
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
                            break;
                        case EventTransferScheduleTimeout:
                            tipSessionChangeTimeout();
                            break;
                        case EventTransferScheduleDeny:
                            tipSessionTransferDeny();
                            break;
                        case EventTransferScheduleAccept:
                            if (data instanceof List){
                                List<JSONObject> jsonList = (List<JSONObject>) data;
                                if (jsonList != null && !jsonList.isEmpty()) {
                                    for (JSONObject jsonObject : jsonList) {
                                        tipSessionTransferSuccess();
                                        try {
                                            String[] results = jsonObject.getString("body").split(":");
                                            CloseSessionManager.getInstance().notifyListeners(results[2]);
                                        } catch (Exception e) {
                                            HDLog.e(TAG, "transfer accept error:" + e.getMessage());
                                        }
                                    }
                                }
                            }
                            break;
                        case EventMaxSessionsChanged:
                            refreshMaxAccessCount();
                            break;
                        case EventActivityCreate:
                            tipNoticeCenterRefresh();
                            break;
                        case EventSessionClosedByAdmin:
                            if (data instanceof List){
                                List<JSONObject> jsonList = (List<JSONObject>) data;
                                if(jsonList != null && !jsonList.isEmpty()){
                                    for (JSONObject jsonObject : jsonList) {
                                        try {
                                            String strBody = jsonObject.getString("body");
                                            String sSessionId = strBody.substring(0, strBody.indexOf(":"));
                                            if (sSessionId != null) {
                                                CloseSessionManager.getInstance().notifyListeners(sSessionId);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            HDLog.e(TAG, "ServiceSessionClosedByAdmin error:" + e.getMessage());
                                        }
                                    }
                                }

                            }

                            break;
                        case EventSessionTransfered:
                            if (data instanceof List){
                                List<JSONObject> jsonList = (List<JSONObject>) data;
                                if (jsonList != null && !jsonList.isEmpty()){
                                    for (JSONObject jsonObject : jsonList) {
                                        try {
                                            String strBody = jsonObject.getString("body");
                                            String [] splitStr = strBody.split(":");
                                            String sSessionId = splitStr[1];
                                            if (sSessionId != null) {
                                                CloseSessionManager.getInstance().notifyListeners(sSessionId);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            HDLog.e(TAG, "ServiceSessionTransfered error:" + e.getMessage());
                                        }
                                    }
                                }
                            }
                            break;
                        case EventOptionChange:
                            if (data instanceof String) {
                                String changedOption = (String) data;
                                if (changedOption.equals("agentVisitorCenterVisible")) {
                                    HDClient.getInstance().chatManager().getTenantOptions(new HDDataCallBack<String>() {
                                        @Override
                                        public void onSuccess(String value) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateSlidingMenu();
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
                                } else if (changedOption.equals("allowAgentChangeMaxSessions")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateMaxAccessButtonVisible();
                                        }
                                    });
                                }
                            }
                            break;
                        case EventTransferSchedule:
                            tipTransferSchedule();
                            break;
                    }

                        }
                    });

                }
            };
        }
        HDClient.getInstance().chatManager().addEventListener(eventListener);
    }

    /**
     * 配置左边滑动菜单
     */
    private void configureSlidingMenu() {
        //configure the SlidingMenu
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        //设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
//        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.color.colorAccent);

        //设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        //设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);

        /**
         * SLIDING_WINDOW will include the Title/ActionBar in the content
         * section of the SlidingMenu, while SLIDING_CONTENT does not.
         */
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局
//        menu.setMenu(R.layout.layout_left_menu);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.layout_left_menu, null);

        menu.setMenu(view);
        menuList = (ListView) view.findViewById(R.id.listview);
        tvMenuNick = (TextView) view.findViewById(R.id.tv_nickname);
        tvMenuStatus = (TextView) view.findViewById(R.id.tv_status);
        ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        ivStatus = (ImageView) view.findViewById(R.id.iv_status);

        modelChangeLayout = (LinearLayout) view.findViewById(R.id.model_change_layout);

        modelChangeLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ManagerHomeActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MANAGE_HOME);
            }
        });

        OptionEntity centerVisible = HDClient.getInstance().agentManager().getOptionEntity("agentVisitorCenterVisible");
//        List<SlidingMenuItemEntity> items = new ArrayList<>();
        items.clear();
        SlidingMenuItemEntity homeItem = new SlidingMenuItemEntity();
        homeItem.count = 0;
        homeItem.icon = getResourceDrawable(R.drawable.menu_icon_session);
        homeItem.title = "主界面";
        homeItem.id = R.id.slidingmenu_homeItem;


        SlidingMenuItemEntity historyItem = new SlidingMenuItemEntity();
        historyItem.count = 0;
        historyItem.icon = getResourceDrawable(R.drawable.menu_icon_history);
        historyItem.title = "历史会话";
        historyItem.id = R.id.slidingmenu_historyItem;

        SlidingMenuItemEntity customersCenterItem = new SlidingMenuItemEntity();
        customersCenterItem.count = 0;
        customersCenterItem.icon = getResourceDrawable(R.drawable.menu_icon_customers);
        customersCenterItem.title = "客户中心";
        customersCenterItem.id = R.id.slidingmenu_customerCenterItem;


        SlidingMenuItemEntity settingItem = new SlidingMenuItemEntity();
        settingItem.count = 0;
        settingItem.icon = getResourceDrawable(R.drawable.menu_icon_setting);
        settingItem.title = "设置";
        settingItem.id = R.id.slidingmenu_settingItem;

        items.add(homeItem);
        items.add(historyItem);
        if (centerVisible != null && centerVisible.getOptionValue() != null && centerVisible.getOptionValue().equals("true")) {
            items.add(customersCenterItem);
        }
        items.add(settingItem);
        menuAdapter = new SlidingMenuListAdapter(this, items);
        menuList.setAdapter(menuAdapter);
        menuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        menu.toggle();
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (view.getId()) {
                    case R.id.slidingmenu_homeItem:
                        break;
                    case R.id.slidingmenu_historyItem:
                        startActivity(new Intent(MainActivity.this, HistorySessionActivity.class));
                        break;
                    case R.id.slidingmenu_customerCenterItem:
                        startActivity(new Intent(MainActivity.this, CustomersCenterActivity.class));
                        break;
                    case R.id.slidingmenu_settingItem:
                        startActivity(new Intent(MainActivity.this, AgentProfileActivity.class));
                        break;
                }
                menu.toggle();
            }
        });

        menu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            @Override
            public void onOpened() {
                refreshMenuNickAndStatus();
                refreshHomeCount();
                if (ivAvatar != null) {
                    AvatarManager.getInstance(MainActivity.this).refreshAgentAvatar(MainActivity.this, ivAvatar);
                }
            }
        });
        updateMaxAccessButtonVisible();
        refreshMenuNickAndStatus();
        refreshAllAvatar();
    }

    public void updateSlidingMenu() {
        items.clear();

        OptionEntity centerVisible = HDClient.getInstance().agentManager().getOptionEntity("agentVisitorCenterVisible");

        SlidingMenuItemEntity homeItem = new SlidingMenuItemEntity();
        homeItem.count = 0;
        homeItem.icon = getResourceDrawable(R.drawable.menu_icon_session);
        homeItem.title = "主界面";
        homeItem.id = R.id.slidingmenu_homeItem;


        SlidingMenuItemEntity historyItem = new SlidingMenuItemEntity();
        historyItem.count = 0;
        historyItem.icon = getResourceDrawable(R.drawable.menu_icon_history);
        historyItem.title = "历史会话";
        historyItem.id = R.id.slidingmenu_historyItem;

        SlidingMenuItemEntity customersCenterItem = new SlidingMenuItemEntity();
        customersCenterItem.count = 0;
        customersCenterItem.icon = getResourceDrawable(R.drawable.menu_icon_customers);
        customersCenterItem.title = "客户中心";
        customersCenterItem.id = R.id.slidingmenu_customerCenterItem;


        SlidingMenuItemEntity settingItem = new SlidingMenuItemEntity();
        settingItem.count = 0;
        settingItem.icon = getResourceDrawable(R.drawable.menu_icon_setting);
        settingItem.title = "设置";
        settingItem.id = R.id.slidingmenu_settingItem;


        items.add(homeItem);
        items.add(historyItem);
        if (centerVisible != null && centerVisible.getOptionValue() != null && centerVisible.getOptionValue().equals("true")) {
            items.add(customersCenterItem);
        }
        items.add(settingItem);
        menuAdapter.updateListItem(items);
        updateMaxAccessButtonVisible();

        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser != null && loginUser.getRoles() != null){
            if (loginUser.getRoles().contains("admin")){
                modelChangeLayout.setVisibility(View.VISIBLE);
            }else{
                modelChangeLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void refreshAllAvatar() {
        if (mySessionFragment != null) {
            mySessionFragment.refreshAgentAvatar();
        }
        if (mWaitAccessFragment != null) {
            mWaitAccessFragment.refreshAgentAvatar();
        }
        if (mNoticeFragment != null) {
            mNoticeFragment.refreshAgentAvatar();
        }
        if (mLeaveMessageFragment != null){
            mLeaveMessageFragment.refreshAgentAvatar();
        }

        if (ivAvatar != null) {
            AvatarManager.getInstance(this).refreshAgentAvatar(MainActivity.this, ivAvatar);
        }
    }

    @Override
    public void refreshHomeCount() {
        if (items == null || items.size() == 0) {
            return;
        }
        int totalCount = 0;
        if (mySessionFragment != null) {
            totalCount += mySessionFragment.getSessionUnreadCount();
        }
//        if (mWaitAccessFragment != null) {
//            totalCount += mWaitAccessFragment.getWaitTotalCount();
//        }
//        if (mNoticeFragment != null) {
//            totalCount += mNoticeFragment.getUnreadCount();
//        }
        items.get(0).count = totalCount;
        menuAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshMenuNickAndStatus() {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser != null) {
            tvMenuNick.setText(loginUser.getNicename());
            CommonUtils.setAgentStatusView(ivStatus, loginUser.getOnLineState());
            CommonUtils.setAgentStatusTextView(tvMenuStatus, loginUser.getOnLineState());
        }
    }

    @Override
    public void refreshMaxAccessCount() {
        if (mySessionFragment != null){
            mySessionFragment.refreshSessionCount();
        }
    }

    public void agentStatusUpdated(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
        CommonUtils.setAgentStatusTextView(tvMenuStatus, status);
        if (mySessionFragment != null) {
            mySessionFragment.refreshOnline(status);
        }
        if (mWaitAccessFragment != null) {
            mWaitAccessFragment.refreshOnline(status);
        }
        if (mNoticeFragment != null) {
            mNoticeFragment.refreshOnline(status);
        }
        if (mLeaveMessageFragment != null){
            mLeaveMessageFragment.refreshOnline(status);
        }
    }


    public void menutoggle(View view) {
        if (menu != null) {
            menu.toggle();
        }
    }


    public void showStatus(View view) {
        if (statusPickerView == null) {
            statusPickerView = new StatusPickerView(this);
        }
        statusPickerView.setCancelable(true);
        statusPickerView.show();
    }

    public void showMaxAccess(View view){
        if (maxAccessPickerView == null){
            maxAccessPickerView = new MaxAccessPickerView(this);
        }
        maxAccessPickerView.setCancelable(true);
        maxAccessPickerView.checkModifiable();
        maxAccessPickerView.show();
    }



    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_AGENT_CHANGE) {
                try {
                    HDClient.getInstance().logout(null);
                } catch (Exception ignored) {
                }
                HDApplication.getInstance().logout();
            }else if (requestCode == REQUEST_CODE_MANAGE_HOME){
                HDUser loginUser = HDClient.getInstance().getCurrentUser();
                if (loginUser != null){
                    agentStatusUpdated(loginUser.getOnLineState());
                }

            }
        }
    }


    public void waitIntent(int position, int index){
        if (mWaitAccessFragment != null){
            mWaitAccessFragment.clickAccess(position, index);
        }
    }


    public synchronized void refreshSessionCount(int count) {
        if (mySessionFragment != null) {
            mySessionFragment.refreshSessionCount(count);
        }
    }



    public void refreshSessionUnreadCount() {
        if (mySessionFragment != null) {
//            mySessionFragment.refreshUnReadMsgCount();
            int count = mySessionFragment.getSessionUnreadCount();
//            refreshHomeCount(count);
            if (menu.isMenuShowing()) {
                refreshHomeCount();
            }
            if (count > 0) {
                //刷新应用角标
//                ShortcutBadger.applyCount(this, count);
                try{
                    BadgeUtil.sendBadgeNotification(null, 0, this, count, count);
                }catch (Exception e){
                    e.printStackTrace();
                }
//                CommonUtils.handleBadge(this, count);
                mTabLayout.showMsg(0, count);
                mTabLayout.setMsgMargin(0, -10, -3);
                //ff661a  or Color.rgb(255, 106, 0)
                RoundTextView rtv_session = mTabLayout.getMsgView(0);
                if (rtv_session != null) {
//                    rtv_session.getDelegate().setBackgroundColor(Color.parseColor("#ff661a"));
                    rtv_session.getDelegate().setBackgroundColor(Color.rgb(255, 106, 0));
                }
            } else {

//                ShortcutBadger.removeCount(this);
//                CommonUtils.handleBadge(this, 0);
                mTabLayout.hideMsg(0);
                try{
                    BadgeUtil.resetBadgeCount(getApplicationContext());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void refreshWaitUnreadCount() {
        if (mWaitAccessFragment != null) {
            int waitCount = mWaitAccessFragment.getWaitTotalCount();
            if (menu.isMenuShowing()) {
                refreshHomeCount();
            }
            if (waitCount > 0) {
                mTabLayout.showMsg(1, waitCount);
                mTabLayout.setMsgMargin(1, -10, -3);
                RoundTextView rtv_agent = mTabLayout.getMsgView(1);
                if (rtv_agent != null) {
//                    rtv_agent.getDelegate().setBackgroundColor(Color.parseColor("#ff661a"));
                    rtv_agent.getDelegate().setBackgroundColor(Color.rgb(255, 106, 0));
                }
            } else {
                mTabLayout.hideMsg(1);
            }
        }
    }

    public void refreshNoticeUnreadCount() {
        if (mNoticeFragment != null) {
            int count = mNoticeFragment.getUnreadCount();
            if (menu.isMenuShowing()) {
                refreshHomeCount();
            }
            if (count > 0) {
                mTabLayout.showMsg(2, count);
                mTabLayout.setMsgMargin(2, -10, -3);
                RoundTextView rtv_notice = mTabLayout.getMsgView(2);
                if (rtv_notice != null) {
                    rtv_notice.getDelegate().setBackgroundColor(Color.rgb(255, 106, 0));
                }
            } else {
                mTabLayout.hideMsg(2);
            }
        }
    }

    public void refreshOpenedLeaveMessageCount() {
        if (mLeaveMessageFragment != null) {
            int count = mLeaveMessageFragment.getOpenTicketsCountResult();
            if (menu.isMenuShowing()) {
                refreshHomeCount();
            }
            if (count > 0) {
                mTabLayout.showMsg(3, count);
                mTabLayout.setMsgMargin(3, -10, -3);
                //ff661a  or Color.rgb(255, 106, 0)
                RoundTextView rtv_leaveMsg = mTabLayout.getMsgView(3);
                if (rtv_leaveMsg != null) {
                    rtv_leaveMsg.getDelegate().setBackgroundColor(Color.rgb(255, 106, 0));
                }
            } else {
                mTabLayout.hideMsg(3);
            }
        }
    }


    /**
     * Initalization Component
     */
    private void initView() {
        //======start=======
        mFragments.clear();
        if (mySessionFragment == null){
            mySessionFragment = new SessionFragment();
        }
        if (mWaitAccessFragment == null){
            mWaitAccessFragment = new WaitAccessFragment();
        }
        if (mNoticeFragment == null){
            mNoticeFragment = new NoticeFragment();
        }
        if(mLeaveMessageFragment == null){
            mLeaveMessageFragment = new LeaveMessageGroupFragment();
        }

        mFragments.add(mySessionFragment);
        mFragments.add(mWaitAccessFragment);
        mFragments.add(mNoticeFragment);
        mFragments.add(mLeaveMessageFragment);

        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i], mIconIds[i]));
        }
        mViewPager = $(R.id.viewpager);

        FragmentViewPagerAdapter viewPagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), mViewPager, mFragments);
        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mTabLayout = $(R.id.tablayout);
        setDataToLayout();
        //========end=========
    }

    /**
     * 为CommonTablayout填充数据
     */
    private void setDataToLayout() {
        mTabLayout.setTabData(mTabEntities);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setCurrentTab(currentSelectedIndex);

    }


    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
        closeTransferDialog();
        closeRoleChangedDialog();
        if (maxAccessPickerView != null){
           maxAccessPickerView.dismiss();
        }
        if (statusPickerView != null){
            statusPickerView.dismiss();
        }
        if (eventListener != null){
            HDClient.getInstance().chatManager().removeEventListener(eventListener);
        }
        mySessionFragment = null;
        mNoticeFragment = null;
        mWaitAccessFragment = null;
        mLeaveMessageFragment = null;
        mViewPager.removeAllViews();
        mViewPager = null;
        if (HDApplication.getInstance().avatarBitmap != null) {
            HDApplication.getInstance().avatarBitmap = null;
        }

    }

    /**
     * 隐藏dialog
     */
    private void closeRoleChangedDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    private void closeTransferDialog() {
        if (mTransferConfirmDialog != null && mTransferConfirmDialog.isShowing()) {
            mTransferConfirmDialog.dismiss();
        }
    }

    private void closeDialog(){
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mainPresenter.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        HDLog.v(TAG, "onNewIntent");
    }


    @Override
    public void tipCurrentUserDeleted() {
        if (this.isFinishing()) {
            return;
        }
        Toast.makeText(MainActivity.this, "客服账号被删除！", Toast.LENGTH_SHORT)
                .show();
        finish();
        HDApplication.getInstance().logout();
    }

    @Override
    public void tipAgentRoleChange() {
        closeRoleChangedDialog();
        Activity topActivity = HDApplication.getInstance().getTopActivity();
        if (topActivity == null) {
            topActivity = MainActivity.this;
        }
        mAlertDialog = new Dialog(topActivity, R.style.MyDialogStyle);

        @SuppressLint("InflateParams")  View view = LayoutInflater.from(topActivity).inflate(R.layout.dialog_alertview, null);
        tvDialogMessage = (TextView) view.findViewById(R.id.dialog_message);
        ((TextView)view.findViewById(R.id.cancel)).setText("返回");
        (view.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        ((TextView)view.findViewById(R.id.ok)).setText("重新登录");
        (view.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // 退出时必须有网
                 dialog = DialogUtils.getLoadingDialog(mActivity, R.string.info_logouting);
                 dialog.setCanceledOnTouchOutside(false);
                 dialog.setCancelable(false);
                 dialog.show();
                 HDClient.getInstance().logout(new HDDataCallBack() {
                     @Override
                     public void onSuccess(Object value) {
                         if (isFinishing()){
                             return;
                         }
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 closeDialog();
                                 HDApplication.getInstance().finishAllActivity();
                                 Intent intent = new Intent(mActivity, LoginActivity.class);
                                 startActivity(intent);
                             }
                         });
                     }

                     @Override
                     public void onError(int error, String errorMsg) {
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 closeDialog();
                                 Toast.makeText(getApplicationContext(), "退出失败，请检查网络！", Toast.LENGTH_SHORT).show();
                             }
                         });
                     }
                 });
             }
        });
        mAlertDialog.setContentView(view);
        tvDialogMessage.setText("您的权限发生了变更，某些功能重新登录生效");
        mAlertDialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedIndex", currentSelectedIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSelectedIndex = savedInstanceState.getInt("selectedIndex", 0);

    }

    @Override
    public void tipSessionChangeTimeout() {
        Toast.makeText(MainActivity.this, "会话转接超时！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tipSessionTransferDeny() {
        Toast.makeText(MainActivity.this, "会话转接被拒绝！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tipSessionTransferSuccess() {
        Toast.makeText(MainActivity.this, "会话转接成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tipNoticeCenterRefresh() {
        HDNotifier.getInstance().notifiChatMsg(null);
        if (mNoticeFragment != null){
            mNoticeFragment.refreshChildDatas();
        }
    }

    @Override
    public void tipTransferSchedule() {
        closeTransferDialog();
        if (HDClient.getInstance().chatManager().getTransferWaitListSize() <= 0) {
            return;
        }
        Activity topActivity = HDApplication.getInstance().getTopActivity();
        if (topActivity == null) {
            topActivity = MainActivity.this;
        }
        mTransferConfirmDialog = new Dialog(topActivity, R.style.MyDialogStyle);

        @SuppressLint("InflateParams")  View view = LayoutInflater.from(topActivity).inflate(R.layout.dialog_transfer_confirm_view, null);
        tvDialogMessage = (TextView) view.findViewById(R.id.dialog_message);
        (view.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTransferConfirmDialog.dismiss();
                HDClient.getInstance().chatManager().transferAllSession(false);
            }
        });
        (view.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTransferConfirmDialog.dismiss();
                HDClient.getInstance().chatManager().transferAllSession(true);
            }
        });
        mTransferConfirmDialog.setContentView(view);
        tvDialogMessage.setText("收到" + HDClient.getInstance().chatManager().getTransferWaitListSize() + "个转接会话，是否接收？") ;
        mTransferConfirmDialog.show();
    }
}
