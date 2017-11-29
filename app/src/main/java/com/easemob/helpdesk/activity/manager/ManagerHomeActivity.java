package com.easemob.helpdesk.activity.manager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.CheckableLayout;
import com.easemob.helpdesk.widget.pickerview.StatusPickerView;
import com.easemob.slidingmenu.lib.SlidingMenu;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuzhao on 16/6/6.
 */
public class ManagerHomeActivity extends BaseActivity {

    private static final String TAG = ManagerHomeActivity.class.getSimpleName();

    /**
     * 左边菜单
     */
    private SlidingMenu menu;

    /**
     * 用户昵称 （左边菜单）
     */
    private TextView tvMenuNick;

    /**
     * 用户昵称 左边菜单
     */
    private TextView tvMenuStatus;

    /**
     * 用户头像 (左边菜单)
     */
    private ImageView ivAvatar;

    /**
     * 用户状态 (左边菜单)
     */
    private ImageView ivStatus;

    private HomeFragment homeFragment;

//    private ManagerCurrentSessionFragment currentSessionFragment;

//    private ManagerHistoryFragment historyFragment;

    private ManagerWorkloadFragment workloadFragment;

    private ManagerWorkmanshipFragment workmanshipFragment;

    private ManagerVisitorsFragment visitorsFragment;

//    private RealTimeMonitorFragment realTimeMonitorFragment;

//    private ManagerAlarmsFragment managerAlarmsFragment;

    private StatusPickerView statusPickerView;

    private HDUser currentLoginUser;

    private CheckableLayout homeMenuLayout;
    private CheckableLayout currentSessionMenuLayout;
    private CheckableLayout historySessionMenuLayout;
    private CheckableLayout realTimeMonitorLayout;
    private CheckableLayout alarmMenuLayout;

    private CheckableLayout statisticsMenuLayout;
    private View tvMenuStatisUp;
    private View tvMenuStatisDown;

    private LinearLayout extraStatisticsLayout;
    private CheckableLayout workloadMenuLayout;
    private CheckableLayout workManShipMenuLayout;
    private CheckableLayout visitorsMenuLayout;

    private MenuItem currentSelectedMenu = MenuItem.Home;
    private FragmentManager fragmentManager;

    private LinearLayout modelChangeLayout;


    private List<CheckableLayout> leftMenuViews = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.manager_main_activity);
        currentLoginUser = HDClient.getInstance().getCurrentUser();
        configureSlidingMenu();
        homeFragment = new HomeFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_layout, homeFragment).commit();
    }

    protected void clickMenuItem(MenuItem clickMenuItem){
        if (clickMenuItem != currentSelectedMenu){
            if (fragmentManager == null){
                return;
            }
//            if (realTimeMonitorFragment != null) {
//                realTimeMonitorFragment.removeFragments();
//            }
            currentSelectedMenu = clickMenuItem;
            switch (clickMenuItem){
                case Home:
                    if (homeFragment == null){
                        homeFragment = new HomeFragment();
                    }
                    fragmentManager.beginTransaction().replace(R.id.main_layout, homeFragment).commit();
                    break;
//                case CurrentSession:
//                    if (currentSessionFragment == null){
//                        currentSessionFragment = new ManagerCurrentSessionFragment();
//                    }
//                    fragmentManager.beginTransaction().replace(R.id.main_layout, currentSessionFragment).commit();
//                    break;
//                case HistorySession:
//                    if (historyFragment == null){
//                        historyFragment = new ManagerHistoryFragment();
//                    }
//                    fragmentManager.beginTransaction().replace(R.id.main_layout, historyFragment).commit();
//                    break;
                case Workload:
                    if (workloadFragment == null){
                        workloadFragment = new ManagerWorkloadFragment();
                    }
                    fragmentManager.beginTransaction().replace(R.id.main_layout, workloadFragment).commit();
                    break;
                case Workmanship:
                    if (workmanshipFragment == null){
                        workmanshipFragment = new ManagerWorkmanshipFragment();
                    }
                    fragmentManager.beginTransaction().replace(R.id.main_layout, workmanshipFragment).commit();
                    break;
                case Visitors:
                    if (visitorsFragment == null){
                        visitorsFragment = new ManagerVisitorsFragment();
                    }
                    fragmentManager.beginTransaction().replace(R.id.main_layout, visitorsFragment).commit();
                    break;
//                case RealTimeMonitor:
//                    if (realTimeMonitorFragment == null){
//                        realTimeMonitorFragment = new RealTimeMonitorFragment();
//                    }
//                    fragmentManager.beginTransaction().replace(R.id.main_layout, realTimeMonitorFragment).commit();
//                    break;
//                case Alarms:
//                    if (managerAlarmsFragment == null) {
//                        managerAlarmsFragment = new ManagerAlarmsFragment();
//                    }
//                    fragmentManager.beginTransaction().replace(R.id.main_layout, managerAlarmsFragment).commit();
//                    break;
            }
        }
        if (menu != null){
            menu.toggle();
        }
    }


    enum MenuItem{
        Home,
        CurrentSession,
        HistorySession,
        Workload,
        Workmanship,
        Visitors,
        RealTimeMonitor,
        Alarms
    }


    private void configureSlidingMenu() {
        //configure the SlidingMenu
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        //设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.color.colorAccent);

        //设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        //设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);


        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.layout_left_manager_menu, null);
        menu.setMenu(view);
        tvMenuNick = (TextView) view.findViewById(R.id.tv_nickname);
        tvMenuStatus = (TextView) view.findViewById(R.id.tv_status);
        ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        ivStatus = (ImageView) view.findViewById(R.id.iv_status);

        homeMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_home_layout);
        currentSessionMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_current_session_layout);
        historySessionMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_history_session_layout);
        statisticsMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_statistics_layout);
        tvMenuStatisUp = view.findViewById(R.id.tv_arrow_up);
        tvMenuStatisDown = view.findViewById(R.id.tv_arrow_down);

        extraStatisticsLayout = (LinearLayout) view.findViewById(R.id.extra_statistic_layout);
        workloadMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_workload_layout);
        workManShipMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_workmanship_layout);
        visitorsMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_visitors_layout);
        realTimeMonitorLayout = (CheckableLayout) view.findViewById(R.id.menu_realtime_monitor_layout);
        alarmMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_alarms_layout);

        extraStatisticsLayout.setVisibility(View.GONE);


        modelChangeLayout = (LinearLayout) view.findViewById(R.id.model_change_layout);
        modelChangeLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent());
                ManagerHomeActivity.this.finish();
            }
        });


        homeMenuLayout.setChecked(true);
        homeMenuLayout.setBackgroundColor(getResources().getColor(R.color.manager_left_menu_item_color_pressed));
        homeMenuLayout.setOnClickListener(menuItemClickListener);
//        currentSessionMenuLayout.setOnClickListener(menuItemClickListener);
//        historySessionMenuLayout.setOnClickListener(menuItemClickListener);
        workloadMenuLayout.setOnClickListener(menuItemClickListener);
        workManShipMenuLayout.setOnClickListener(menuItemClickListener);
        visitorsMenuLayout.setOnClickListener(menuItemClickListener);
//        realTimeMonitorLayout.setOnClickListener(menuItemClickListener);
//        alarmMenuLayout.setOnClickListener(menuItemClickListener);

        statisticsMenuLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (tvMenuStatisUp.getVisibility() == View.VISIBLE){
                    tvMenuStatisUp.setVisibility(View.INVISIBLE);
                    tvMenuStatisDown.setVisibility(View.VISIBLE);
                    extraStatisticsLayout.setVisibility(View.GONE);
                }else{
                    tvMenuStatisDown.setVisibility(View.INVISIBLE);
                    tvMenuStatisUp.setVisibility(View.VISIBLE);
                    extraStatisticsLayout.setVisibility(View.VISIBLE);
                }

            }
        });

        leftMenuViews.clear();
        leftMenuViews.add(homeMenuLayout);
//        leftMenuViews.add(currentSessionMenuLayout);
//        leftMenuViews.add(historySessionMenuLayout);
        leftMenuViews.add(workloadMenuLayout);
        leftMenuViews.add(workManShipMenuLayout);
        leftMenuViews.add(visitorsMenuLayout);
//        leftMenuViews.add(realTimeMonitorLayout);
//        leftMenuViews.add(alarmMenuLayout);

        currentSessionMenuLayout.setVisibility(View.GONE);
        historySessionMenuLayout.setVisibility(View.GONE);
        realTimeMonitorLayout.setVisibility(View.GONE);
        alarmMenuLayout.setVisibility(View.GONE);

        refreshMenuNickAndStatus();
        refreshAllAvatar();
    }

    private void refreshAllAvatar() {
        if (homeFragment != null) {
            homeFragment.refreshAgentAvatar();
        }

//        if (currentSessionFragment != null) {
//            currentSessionFragment.refreshAgentAvatar();
//        }
//        if (historyFragment != null) {
//            historyFragment.refreshAgentAvatar();
//        }
        if (workloadFragment != null) {
            workloadFragment.refreshAgentAvatar();
        }
        if (workmanshipFragment != null) {
            workmanshipFragment.refreshAgentAvatar();
        }
        if (visitorsFragment != null) {
            visitorsFragment.refreshAgentAvatar();
        }
//        if (realTimeMonitorFragment != null) {
//            realTimeMonitorFragment.refreshAgentAvatar();
//        }
//
//        if (managerAlarmsFragment != null) {
//            managerAlarmsFragment.refreshAgentAvatar();
//        }
        if (ivAvatar != null) {
            AvatarManager.getInstance(this).refreshAgentAvatar(ManagerHomeActivity.this, ivAvatar);

        }
    }

    private void refreshMenuNickAndStatus() {
        if (currentLoginUser != null) {
            tvMenuNick.setText(currentLoginUser.getNicename());
            CommonUtils.setAgentStatusView(ivStatus, currentLoginUser.getOnLineState());
            CommonUtils.setAgentStatusTextView(tvMenuStatus, currentLoginUser.getOnLineState());
        }
    }

    public void agentStatusUpdated(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
        CommonUtils.setAgentStatusTextView(tvMenuStatus, status);
        if (homeFragment != null) {
            homeFragment.refreshOnline(status);
        }
//        if (currentSessionFragment != null){
//            currentSessionFragment.refreshOnline(status);
//        }
//        if (historyFragment != null){
//            historyFragment.refreshOnline(status);
//        }
        if (workloadFragment != null){
            workloadFragment.refreshOnline(status);
        }
        if (workmanshipFragment != null){
            workmanshipFragment.refreshOnline(status);
        }
        if (visitorsFragment != null){
            visitorsFragment.refreshOnline(status);
        }
//        if (realTimeMonitorFragment != null){
//            realTimeMonitorFragment.refreshOnline(status);
//        }
//        if (managerAlarmsFragment != null) {
//            managerAlarmsFragment.refreshOnline(status);
//        }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    View.OnClickListener menuItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v instanceof CheckableLayout){
                refreshPressedStatus((CheckableLayout)v);
            }
        }
    };


    public void refreshPressedStatus(CheckableLayout pressedView) {
        for (CheckableLayout item : leftMenuViews) {
            if (item != null){
                item.setChecked(false);
                item.setBackgroundColor(getResources().getColor(R.color.manager_left_menu_item_color_normal));
            }
        }
        pressedView.setChecked(true);
        pressedView.setBackgroundColor(getResources().getColor(R.color.manager_left_menu_item_color_pressed));
        MenuItem pressedMenuItem = MenuItem.Home;
        if (pressedView == homeMenuLayout){
            pressedMenuItem = MenuItem.Home;
        }else if (pressedView == currentSessionMenuLayout) {
            pressedMenuItem = MenuItem.CurrentSession;
        }else if (pressedView == historySessionMenuLayout) {
            pressedMenuItem = MenuItem.HistorySession;
        }else if (pressedView == workloadMenuLayout) {
            pressedMenuItem = MenuItem.Workload;
        }else if (pressedView == workManShipMenuLayout) {
            pressedMenuItem = MenuItem.Workmanship;
        }else if (pressedView == visitorsMenuLayout) {
            pressedMenuItem = MenuItem.Visitors;
        }else if (pressedView == realTimeMonitorLayout) {
            pressedMenuItem = MenuItem.RealTimeMonitor;
        }else if (pressedView == alarmMenuLayout) {
            pressedMenuItem = MenuItem.Alarms;
        }
        clickMenuItem(pressedMenuItem);
    }

//    public void currentSessionIntent(int position, int index) {
//        if (currentSessionFragment != null) {
//            currentSessionFragment.currentSessionIntent(position, index);
//        }
//    }

}
