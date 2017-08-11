package com.easemob.helpdesk.activity.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.relative.ViewPagerContainerLayout;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SessionFragment extends Fragment {
    @BindView(R.id.viewpager)
    public ViewPager myViewPager;
    @BindView(R.id.ib_setting)
    public View imageButtonSetting;
    private Boolean isExit = false;
    private CurrentSessionFragment currentSessionFragment;
    private AgentsFragment mAgentsFragment;
    @BindView(R.id.myLayout)
    public ViewPagerContainerLayout viewPagerContainerLayout;
    private String[] mTitles = {"进行中", "客服"};
    @BindView(R.id.tablayout)
    public SegmentTabLayout tabLayout;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    @BindView(R.id.iv_avatar)
    public ImageView ivAvatar;
    @BindView(R.id.iv_status)
    public ImageView ivStatus;
    @BindView(R.id.tv_session_count)
    public TextView tvSessionCount;

    private int currentSessionCount= 0;
    private Unbinder unbinder;

    private HDUser currentUser;
    private MyPagerAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_2, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
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
        tabLayout.setTabData(mTitles);
        myViewPager.setAdapter(adapter = new MyPagerAdapter(getChildFragmentManager()));
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                myViewPager.setCurrentItem(position);
                tabLayout.hideMsg(position);
                if (position == 0) {
                    exitBy2Click();
                } else if (position == 1) {
                    long lastUpdateTime = HDApplication.AgentLastUpdateTime;
                    if (lastUpdateTime > 0 && System.currentTimeMillis() - lastUpdateTime > 30000) {
                        if (mAgentsFragment != null) {
                            mAgentsFragment.refreshByRemote();
                        }
                    }
                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        myViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
                tabLayout.hideMsg(position);
                if(position == 1){
                    long lastUpdateTime = HDApplication.AgentLastUpdateTime;
                    if (lastUpdateTime > 0 && System.currentTimeMillis() - lastUpdateTime > 30000) {
                        if (mAgentsFragment != null) {
                            mAgentsFragment.refreshByRemote();
                        }

                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setCurrentTab(0);
        imageButtonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setClass(getActivity(), AgentInfoActivity.class);
//                startActivity(intent);
                ((MainActivity)getActivity()).showMaxAccess(view);
            }
        });
    }


    public void refreshTabBarUnread(boolean isAgent) {
        if(tabLayout == null) {
            return;
        }
        int currentTab = tabLayout.getCurrentTab();
        if (isAgent) {
            if (currentTab == 0) {
                tabLayout.showDot(1);
            }
        } else {
            if (currentTab == 1) {
                tabLayout.showDot(0);
            }
        }
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

    public void initView() {
        if(viewPagerContainerLayout != null){
            viewPagerContainerLayout.setChildViewPager(myViewPager);
        }
    }

    private void loadFirstStatus(){
            if(currentUser != null){
                refreshOnline(currentUser.getOnLineState());
            }
        }

    public void refreshOnline(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
    }

    public void refreshAgentAvatar() {
        if(ivAvatar != null)
            AvatarManager.getInstance(getContext()).refreshAgentAvatar(getActivity(), ivAvatar);
    }


    public void refreshSessionCount(int count){
        if(tvSessionCount == null || currentUser == null){
            return;
        }
        if (count >= 0){
            currentSessionCount = count;
        }
        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
    }

    public void refreshSessionCount(){
        refreshSessionCount(currentSessionCount);
    }



    @Override
    public void onResume() {
        super.onResume();
        if(tvSessionCount == null || currentUser == null){
            return;
        }
        tvSessionCount.setText(currentSessionCount + "/" + currentUser.maxServiceSessionCount);
    }


    public int getSessionUnreadCount() {
        if (currentSessionFragment != null) {
            return currentSessionFragment.getUnreadCount();
        }
        return 0;
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
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            if(currentSessionFragment != null && currentSessionFragment.mRecyclerView != null){
                currentSessionFragment.mRecyclerView.scrollToPosition(0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
        adapter = null;
    }
}

