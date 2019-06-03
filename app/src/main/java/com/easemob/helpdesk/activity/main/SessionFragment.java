package com.easemob.helpdesk.activity.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.easemob.helpdesk.widget.popupwindow.GuideTipsPopupWindow;
import com.easemob.helpdesk.widget.relative.ViewPagerContainerLayout;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SessionFragment extends Fragment {
    @BindView(R.id.viewpager) public ViewPager myViewPager;
    private Boolean isExit = false;
    private CurrentSessionFragment currentSessionFragment;
    private AgentsFragment mAgentsFragment;
    @BindView(R.id.myLayout) public ViewPagerContainerLayout viewPagerContainerLayout;
    //private String[] mTitles = { "进行中", "客服" };
    private Integer[] drawableIds = { R.drawable.session_icon, R.drawable.agent_icon };
    @BindView(R.id.tablayout) public SegmentTabLayout tabLayout;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    @BindView(R.id.iv_avatar) public ImageView ivAvatar;
    @BindView(R.id.iv_status) public ImageView ivStatus;
    @BindView(R.id.tv_current_session) public TextView tvCurrentSession;
    @BindView(R.id.tv_session_count) public TextView tvSessionCount;
    @BindView(R.id.ll_session_count) public LinearLayout llSessionCount;
    @BindView(R.id.iv_limit) public ImageView ivLimit;
    @BindView(R.id.iv_notification) public ImageView ivNotification;
    private View view;
    private GuideTipsPopupWindow guideTipsPopupwindow;

    private int currentSessionCount = 0;
    private Unbinder unbinder;

    private HDUser currentUser;
    private MyPagerAdapter adapter;

    public static int onlineAgentCount = 0;
    public static int AllAgentCount = 0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_session_2, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HDLog.d("SessionFragment", "SessionFragment onActivityCreated");
        currentUser = HDClient.getInstance().getCurrentUser();
        initView();
        loadFirstStatus();
        refreshAgentAvatar();

        //--------start---------
        //将Fragment添加入集合
        currentSessionFragment = new CurrentSessionFragment();
        mAgentsFragment = new AgentsFragment();
        mFragments.clear();
        mFragments.add(currentSessionFragment);
        mFragments.add(mAgentsFragment);
        tabLayout.setTabSpaceEqual(false);
        tabLayout.setTabData(drawableIds, R.drawable.baseline_icon);
        myViewPager.setAdapter(adapter = new MyPagerAdapter(getChildFragmentManager()));

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                myViewPager.setCurrentItem(position);
                //tabLayout.hideMsg(position);
                if (position == 0) {
                    exitBy2Click();
                    if (currentUser != null) {
                        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
                    }
                } else if (position == 1) {
                    long lastUpdateTime = HDApplication.AgentLastUpdateTime;
                    if (lastUpdateTime > 0 && System.currentTimeMillis() - lastUpdateTime > 30000) {
                        if (mAgentsFragment != null) {
                            mAgentsFragment.refreshByRemote();
                        }
                    }
                    if (currentUser != null) {
                        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
                    }
                }
            }

            @Override public void onTabReselect(int position) {

            }
        });

        myViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
                //tabLayout.hideMsg(position);
                if (position == 1) {
                    if (tvCurrentSession != null) {
                        tvCurrentSession.setText("同事");
                    }
                    long lastUpdateTime = HDApplication.AgentLastUpdateTime;
                    if (lastUpdateTime > 0 && System.currentTimeMillis() - lastUpdateTime > 30000) {
                        if (mAgentsFragment != null) {
                            mAgentsFragment.refreshByRemote();
                        }
                    }
                    if (currentUser != null) {
                        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
                    }
                } else {
                    if (tvCurrentSession != null) {
                        tvCurrentSession.setText("进行中会话");
                    }
                    if (currentUser != null) {
                        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
                    }
                }
            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setCurrentTab(0);
        llSessionCount.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //                Intent intent = new Intent();
                //                intent.setClass(getActivity(), AgentInfoActivity.class);
                //                startActivity(intent);
                ((MainActivity) getActivity()).showMaxAccess(view);
            }
        });
        if (PreferenceUtils.getInstance().getIsFirst()) {

            ivStatus = view.findViewById(R.id.iv_status);
            llSessionCount = view.findViewById(R.id.ll_session_count);
            ivStatus.post(new Runnable() {
                @Override public void run() {
                    showGuideTops();
                }
            });
        }
    }

    void showGuideTops() {
        guideTipsPopupwindow = new GuideTipsPopupWindow(getActivity());
        guideTipsPopupwindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        guideTipsPopupwindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        if (ivStatus != null) guideTipsPopupwindow.showAsDropDown(ivStatus);
        guideTipsPopupwindow.setClickListener(new GuideTipsPopupWindow.ClickListener() {
            @Override public void onClick() {
                if (llSessionCount != null) guideTipsPopupwindow.showAsDropDown(llSessionCount);
            }
        });
    }

    public void showNotification(boolean isShow) {
        if (ivNotification != null) {
            if (isShow) {
                ivNotification.setImageResource(R.drawable.tip_audio_unread);
            } else {
                ivNotification.setImageResource(0);
            }
        }
    }

    public void setSettingButtonVisible(boolean enabled) {
        if (enabled) {
            if (tvSessionCount != null) {
                llSessionCount.setEnabled(true);
                tvSessionCount.setTextColor(Color.parseColor("#000000"));
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.limited_icon));
                DrawableCompat.setTint(drawable, Color.parseColor("#000000"));
                if (ivLimit != null) {
                    ivLimit.setImageDrawable(drawable);
                }
            }
        } else {
            if (tvSessionCount != null) {
                llSessionCount.setEnabled(false);
                tvSessionCount.setTextColor(Color.parseColor("#9e9e9e"));
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.limited_icon));
                DrawableCompat.setTint(drawable, Color.parseColor("#9e9e9e"));
                if (ivLimit != null) {
                    ivLimit.setImageDrawable(drawable);
                }
            }
        }
    }

    public void refreshTabBarUnread(boolean isAgent) {
        if (tabLayout == null) {
            return;
        }
        if (isAgent) {
            if (getAgentUnreadCount() > 0) {
                tabLayout.showMsg(1, getAgentUnreadCount());
                tabLayout.setMsgMargin(1, -15, 0);
            } else {
                tabLayout.hideMsg(1);
            }
        } else {
            if (getSessionUnreadCount() > 0) {
                tabLayout.showMsg(0, getSessionUnreadCount());
                tabLayout.setMsgMargin(0, -15, 0);
            } else {
                tabLayout.hideMsg(0);
            }
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override public int getCount() {
            return mFragments.size();
        }

        @Override public CharSequence getPageTitle(int position) {
            return drawableIds[position].toString();
        }
    }

    public void initView() {
        if (viewPagerContainerLayout != null) {
            viewPagerContainerLayout.setChildViewPager(myViewPager);
        }
    }

    private void loadFirstStatus() {
        if (currentUser != null) {
            refreshOnline(currentUser.getOnLineState());
        }
    }

    public void refreshOnline(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
    }

    public void refreshAgentAvatar() {
        if (ivAvatar != null) AvatarManager.getInstance().refreshAgentAvatar(getActivity(), ivAvatar);
    }

    public void refreshSessionCount(int count) {
        currentUser = HDClient.getInstance().getCurrentUser();
        if (tvSessionCount == null || currentUser == null) {
            return;
        }
        if (count >= 0) {
            currentSessionCount = count;
        }
        //if (tabLayout.getCurrentTab() == 0)

        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
    }

    public void refreshSessionCount() {
        refreshSessionCount(currentSessionCount);
    }

    @Override public void onResume() {
        super.onResume();
        if (tvSessionCount == null || currentUser == null) {
            return;
        }
        if (tabLayout.getCurrentTab() == 0) {
            tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
        } else {
            //tvSessionCount.setText(onlineAgentCount + "/" + AllAgentCount);
            tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
        }
    }

    public int getSessionUnreadCount() {
        if (currentSessionFragment != null) {
            return currentSessionFragment.getUnreadCount();
        }
        return 0;
    }

    public int getAgentUnreadCount() {
        if (mAgentsFragment != null) {
            return mAgentsFragment.getUnReadCount();
        }
        return 0;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        currentSessionFragment.onActivityResult(requestCode,resultCode,data);
    }

    /*
         * 双击会话返回顶部
         */
    private void exitBy2Click() {
        Timer tExit;
        if (isExit == false) {
            isExit = true; // 准备退出
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            if (currentSessionFragment != null && currentSessionFragment.mRecyclerView != null) {
                currentSessionFragment.mRecyclerView.scrollToPosition(0);
            }
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
        adapter = null;
        mFragments.clear();
        currentSessionFragment = null;
        mAgentsFragment = null;
        if (guideTipsPopupwindow != null) guideTipsPopupwindow.dismiss();
    }
}

