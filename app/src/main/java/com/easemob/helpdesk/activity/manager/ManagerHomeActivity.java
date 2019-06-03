package com.easemob.helpdesk.activity.manager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.main.LeaveMessageGroupFragment;
import com.easemob.helpdesk.activity.main.NoticeFragment;
import com.easemob.helpdesk.activity.main.SessionFragment;
import com.easemob.helpdesk.activity.main.WaitAccessFragment;
import com.easemob.helpdesk.adapter.FragmentViewPagerAdapter;
import com.easemob.helpdesk.entity.TabEntity;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.CheckableLayout;
import com.easemob.helpdesk.widget.pickerview.StatusPickerView;
import com.easemob.slidingmenu.lib.SlidingMenu;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
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

    /**
     * 用户告警信息状态  （左侧菜单）
     */
    private View ivAlarmStatus;

    private HomeFragment homeFragment;

    private ManagerCurrentSessionFragment currentSessionFragment;

    private ManagerHistoryFragment historyFragment;

    private ManagerWorkloadFragment workloadFragment;

    private ManagerWorkmanshipFragment workmanshipFragment;

    private ManagerVisitorsFragment visitorsFragment;

    private RealTimeMonitorFragment realTimeMonitorFragment;

    private ManagerRealtimeSuperviseFragment managerRealtimeSuperviseFragment;

    private ManagerAlarmsFragment managerAlarmsFragment;

    private StatusPickerView statusPickerView;

    private HDUser currentLoginUser;

    private CheckableLayout historySessionMenuLayout;
    private CheckableLayout realTimeSuperviseLayout;

    private CheckableLayout workloadMenuLayout;
    private CheckableLayout workManShipMenuLayout;
    private CheckableLayout visitorsMenuLayout;

    private FragmentManager fragmentManager;

    private LinearLayout modelChangeLayout;

    private List<CheckableLayout> leftMenuViews = new ArrayList<>();

    /**
     * ViewPager
     */
    private ViewPager mViewPager;

    /**
     * Fragment Datas
     */
    private List<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles = { "概况", "当前会话", "实时监控", "告警记录" };

    private int currentSelectedIndex = 0;
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    /**
     * Bottom Tab bar
     */
    private CommonTabLayout mTabLayout;
    private RelativeLayout pagerLayout;
    private FrameLayout sideLayout;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.manager_main_activity);
        currentLoginUser = HDClient.getInstance().getCurrentUser();
        mTabLayout = $(R.id.tablayout);
        configureSlidingMenu();
        fragmentManager = getSupportFragmentManager();
        pagerLayout = $(R.id.pager_layout);
        sideLayout = $(R.id.main_layout);
        pagerLayout.setVisibility(View.VISIBLE);
        sideLayout.setVisibility(View.GONE);
        initView();
    }

    /**
     * Initalization Component
     */
    private void initView() {
        //======start=======
        mFragments.clear();
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        if (currentSessionFragment == null) {
            currentSessionFragment = new ManagerCurrentSessionFragment();
        }
        if (realTimeMonitorFragment == null) {
            realTimeMonitorFragment = new RealTimeMonitorFragment();
        }
        if (managerAlarmsFragment == null) {
            managerAlarmsFragment = new ManagerAlarmsFragment();
        }

        mFragments.add(homeFragment);
        mFragments.add(currentSessionFragment);

        mFragments.add(realTimeMonitorFragment);
        mFragments.add(managerAlarmsFragment);

        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i]));
        }
        mViewPager = $(R.id.viewpager);

        FragmentViewPagerAdapter viewPagerAdapter = new FragmentViewPagerAdapter(fragmentManager, mViewPager, mFragments);
        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        setDataToLayout();
        //========end=========
    }

    /**
     * 为CommonTablayout填充数据
     */
    private void setDataToLayout() {
        mTabLayout.setTabData(mTabEntities);
        mTabLayout.setBaseLineId(R.drawable.baseline_icon);
        mTabLayout.setTextsize(14);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
                if (position == 3){
                    mTabLayout.hideMsg(3);
                }
            }

            @Override public void onTabReselect(int position) {
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
                if (position == 3){
                    mTabLayout.hideMsg(3);
                }
            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setCurrentTab(currentSelectedIndex);
        mViewPager.setCurrentItem(currentSelectedIndex);
        refreshMenuNickAndStatus();
        refreshAllAvatar();
    }

    protected void clickMenuItem(MenuItem clickMenuItem) {
        if (fragmentManager == null) {
            return;
        }
        pagerLayout.setVisibility(View.GONE);
        sideLayout.setVisibility(View.VISIBLE);
        switch (clickMenuItem) {
            case HistorySession:
                if (historyFragment == null) {
                    historyFragment = new ManagerHistoryFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_layout, historyFragment).commit();
                break;
            case Workload:
                if (workloadFragment == null) {
                    workloadFragment = new ManagerWorkloadFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_layout, workloadFragment).commit();
                break;
            case Workmanship:
                if (workmanshipFragment == null) {
                    workmanshipFragment = new ManagerWorkmanshipFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_layout, workmanshipFragment).commit();
                break;
            case Visitors:
                if (visitorsFragment == null) {
                    visitorsFragment = new ManagerVisitorsFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_layout, visitorsFragment).commit();
                break;
            case RealTimeSupervise:
                if (managerRealtimeSuperviseFragment == null) {
                    managerRealtimeSuperviseFragment = new ManagerRealtimeSuperviseFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_layout, managerRealtimeSuperviseFragment).commit();
                break;
        }
        if (menu != null) {
            menu.toggle();
        }
    }

    enum MenuItem {
        HistorySession, Workload, Workmanship, Visitors, RealTimeSupervise,
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
        ivAlarmStatus = view.findViewById(R.id.iv_alarm_status);

        historySessionMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_history_session_layout);

        workloadMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_workload_layout);
        workManShipMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_workmanship_layout);
        visitorsMenuLayout = (CheckableLayout) view.findViewById(R.id.menu_visitors_layout);
        realTimeSuperviseLayout = (CheckableLayout) view.findViewById(R.id.menu_realtime_supervise_layout);

        modelChangeLayout = (LinearLayout) view.findViewById(R.id.model_change_layout);
        modelChangeLayout.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick(View v) {
                setResult(RESULT_OK, new Intent());
                ManagerHomeActivity.this.finish();
            }
        });

        historySessionMenuLayout.setOnClickListener(menuItemClickListener);
        workloadMenuLayout.setOnClickListener(menuItemClickListener);
        workManShipMenuLayout.setOnClickListener(menuItemClickListener);
        visitorsMenuLayout.setOnClickListener(menuItemClickListener);
        realTimeSuperviseLayout.setOnClickListener(menuItemClickListener);

        leftMenuViews.clear();
        leftMenuViews.add(historySessionMenuLayout);
        leftMenuViews.add(workloadMenuLayout);
        leftMenuViews.add(workManShipMenuLayout);
        leftMenuViews.add(visitorsMenuLayout);
        leftMenuViews.add(realTimeSuperviseLayout);

        menu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            @Override public void onOpened() {
                refreshMenuNickAndStatus();
                if (ivAvatar != null) {
                    AvatarManager.getInstance().refreshAgentAvatar(ManagerHomeActivity.this, ivAvatar);
                }
            }
        });
    }

    private void refreshAllAvatar() {
        if (homeFragment != null) {
            homeFragment.refreshAgentAvatar();
        }

        if (currentSessionFragment != null) {
            currentSessionFragment.refreshAgentAvatar();
        }
        if (realTimeMonitorFragment != null) {
            realTimeMonitorFragment.refreshAgentAvatar();
        }

        if (managerAlarmsFragment != null) {
            managerAlarmsFragment.refreshAgentAvatar();
        }
        if (ivAvatar != null) {
            AvatarManager.getInstance().refreshAgentAvatar(ManagerHomeActivity.this, ivAvatar);
        }
    }

    private void refreshMenuNickAndStatus() {
        if (currentLoginUser != null) {
            tvMenuNick.setText(currentLoginUser.getNicename());
            CommonUtils.setAgentStatusView(ivStatus, currentLoginUser.getOnLineState());
            CommonUtils.setAgentStatusTextView(tvMenuStatus, currentLoginUser.getOnLineState());
            if (HDApplication.getInstance().isHasAlarmNoti()) {
                if (ivAlarmStatus != null) {
                    ivAlarmStatus.setVisibility(View.VISIBLE);
                }
                mTabLayout.showDot(3);
            } else {
                if (ivAlarmStatus != null) {
                    ivAlarmStatus.setVisibility(View.GONE);
                }
                mTabLayout.hideMsg(3);
            }
        }
    }

    public void agentStatusUpdated(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
        CommonUtils.setAgentStatusTextView(tvMenuStatus, status);
        if (HDApplication.getInstance().isHasAlarmNoti()) {
            if (ivAlarmStatus != null) {
                ivAlarmStatus.setVisibility(View.VISIBLE);
            }
        } else {
            if (ivAlarmStatus != null) {
                ivAlarmStatus.setVisibility(View.GONE);
            }
        }
        if (homeFragment != null) {
            homeFragment.refreshOnline(status);
        }
        if (currentSessionFragment != null) {
            currentSessionFragment.refreshOnline(status);
        }
        if (realTimeMonitorFragment != null) {
            realTimeMonitorFragment.refreshOnline(status);
        }

        if (managerAlarmsFragment != null) {
            managerAlarmsFragment.refreshOnline(status);
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

    public void back() {
        if (pagerLayout.getVisibility() == View.GONE) {
            pagerLayout.setVisibility(View.VISIBLE);
            sideLayout.setVisibility(View.GONE);
        }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (menu.isMenuShowing()) {
                menu.toggle();
            } else if (pagerLayout.getVisibility() == View.GONE) {
                pagerLayout.setVisibility(View.VISIBLE);
                sideLayout.setVisibility(View.GONE);
            } else {
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    View.OnClickListener menuItemClickListener = new View.OnClickListener() {

        @Override public void onClick(View v) {
            if (v instanceof CheckableLayout) {
                refreshPressedStatus((CheckableLayout) v);
            }
        }
    };

    public void refreshPressedStatus(CheckableLayout pressedView) {
        //for (CheckableLayout item : leftMenuViews) {
        //    if (item != null){
        //        item.setChecked(false);
        //        item.setBackgroundColor(getResources().getColor(R.color.manager_left_menu_item_color_normal));
        //    }
        //}
        //pressedView.setChecked(true);
        //pressedView.setBackgroundColor(getResources().getColor(R.color.manager_left_menu_item_color_pressed));
        MenuItem pressedMenuItem = MenuItem.HistorySession;
        if (pressedView == historySessionMenuLayout) {
            pressedMenuItem = MenuItem.HistorySession;
        } else if (pressedView == workloadMenuLayout) {
            pressedMenuItem = MenuItem.Workload;
        } else if (pressedView == workManShipMenuLayout) {
            pressedMenuItem = MenuItem.Workmanship;
        } else if (pressedView == visitorsMenuLayout) {
            pressedMenuItem = MenuItem.Visitors;
        } else if (pressedView == realTimeSuperviseLayout) {
            pressedMenuItem = MenuItem.RealTimeSupervise;
        }
        clickMenuItem(pressedMenuItem);
    }

    public void currentSessionIntent(int position, int index) {
        if (currentSessionFragment != null) {
            currentSessionFragment.currentSessionIntent(position, index);
        }
    }

    public void refreshAlarmsFragment() {
        if (managerAlarmsFragment != null) {
            managerAlarmsFragment.refresh();
        }
    }
}
