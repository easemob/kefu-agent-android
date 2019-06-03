package com.easemob.helpdesk.activity.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.TabEntity;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.gsonmodel.main.UnReadCountBean;
import com.hyphenate.kefusdk.manager.main.NoticeManager;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/3/10.
 */
public class NoticeFragment extends Fragment {

    private static final String TAG = NoticeFragment.class.getSimpleName();
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private String[] mTabTitles = { "全部通知", "管理员通知", "系统通知" };
    private String[] mTitles = { "未读通知", "已读通知" };
    private Integer[] drawableIds = { R.drawable.notice_unread_icon, R.drawable.notice_read_icon };

    @BindView(R.id.tv_title) protected TextView tvTitle;
    @BindView(R.id.framelayout) protected FrameLayout mFrameLayout;
    @BindView(R.id.tablayout) protected CommonTabLayout mTabLayout;
    private FragmentManager mFragmentManager;

    @BindView(R.id.notice_tablayout) protected SegmentTabLayout noticeTableLayout;

    @BindView(R.id.iv_avatar) protected ImageView ivAvatar;
    @BindView(R.id.iv_status) protected ImageView ivStatus;
    private int currentSelectedIndex = 0;

    private HDUser loginUser;
    private Unbinder unbinder;

    private NoticeListFragment noticeListFragment;
    private boolean isUnreadSettings = true;
    private String typeSettings = "all";

    private NoticeManager noticeManager;

    private volatile int totalUnread;

    @BindView(R.id.iv_notification) public ImageView ivNotification;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentSelectedIndex = savedInstanceState.getInt("selectedIndex", 0);
        }
        loginUser = HDClient.getInstance().getCurrentUser();
        noticeManager = new NoticeManager();
        for (String mTabTitle : mTabTitles) {
            mTabEntities.add(new TabEntity(mTabTitle));
        }
        mFragmentManager = getChildFragmentManager();
        mTabLayout.setTabData(mTabEntities);
        noticeTableLayout.setTabSpaceEqual(false);
        noticeTableLayout.setTabData(drawableIds, R.drawable.baseline_icon);
        noticeTableLayout.setCurrentTab(0);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                if (position == 0) {
                    typeSettings = NoticeManager.typeAll;
                } else if (position == 1) {
                    typeSettings = NoticeManager.typeAgent;
                } else if (position == 2) {
                    typeSettings = NoticeManager.typeSystem;
                }
                currentSelectedIndex = position;
                setTabSelection(position);
            }

            @Override public void onTabReselect(int position) {

            }
        });

        noticeTableLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                if (isUnreadSettings) {
                    tvTitle.setText(mTitles[1]);
                } else {
                    tvTitle.setText(mTitles[0]);
                }
                isUnreadSettings = !isUnreadSettings;
                setTabSelection(currentSelectedIndex);
            }

            @Override public void onTabReselect(int position) {

            }
        });
        setTabSelection(currentSelectedIndex);
        loadFirstStatus();
        refreshAgentAvatar();

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        //先隐藏掉所有的Fragment,以防止有多个Fragment显示在界面上的情况

        noticeListFragment = new NoticeListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isUnreadSettings", isUnreadSettings);
        bundle.putString("typeSettings", typeSettings);
        noticeListFragment.setArguments(bundle);
        transaction.replace(R.id.framelayout, noticeListFragment);
        transaction.commit();

        refreshTabUnreadCount();
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

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedIndex", currentSelectedIndex);
    }

    private void setTabSelection(int index) {
        //每次选中之前先清除掉上次的选中状态
        //        clearSelection();
        // 开启一个Fragment事务
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        //先隐藏掉所有的Fragment,以防止有多个Fragment显示在界面上的情况

        noticeListFragment = new NoticeListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isUnreadSettings", isUnreadSettings);
        bundle.putString("typeSettings", typeSettings);
        noticeListFragment.setArguments(bundle);
        transaction.replace(R.id.framelayout, noticeListFragment);
        transaction.commit();
        refreshTabUnreadCount();
    }

    public void refreshAgentAvatar() {
        if (ivAvatar != null) AvatarManager.getInstance().refreshAgentAvatar(getActivity(), ivAvatar);
    }

    public void refreshOnline(String status) {
        if (ivStatus == null) {
            return;
        }
        CommonUtils.setAgentStatusView(ivStatus, status);
    }

    private void loadFirstStatus() {
        if (loginUser != null) {
            refreshOnline(loginUser.getOnLineState());
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    public void refreshChildDatas() {
        if (noticeListFragment != null) {
            noticeListFragment.loadTheFirstPageData();
        }
    }

    public void refreshTabUnreadCount() {
        if (isUnreadSettings) {
            noticeManager.getUnReadCount(new HDDataCallBack<List<UnReadCountBean>>() {
                @Override public void onSuccess(final List<UnReadCountBean> value) {
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            try {
                                for (UnReadCountBean bean : value) {
                                    String type = bean.getType();
                                    int count = bean.getCount_unread();
                                    int pos = 0;
                                    switch (type) {
                                        case "all":
                                            pos = 0;
                                            totalUnread = count;
                                            break;
                                        case "agent":
                                            pos = 1;
                                            break;
                                        case "system":
                                            pos = 2;
                                            break;
                                    }
                                    if (mTabLayout == null)
                                        return;
                                    if (count > 0) {
                                        mTabLayout.showMsg(pos, count);
                                        mTabLayout.setMsgMargin(pos, -1, 2);
                                        noticeTableLayout.showMsg(0, count);
                                        noticeTableLayout.setMsgMargin(0, -15, 0);
                                    } else {
                                        mTabLayout.hideMsg(pos);
                                        noticeTableLayout.hideMsg(pos);
                                    }
                                }
                                ((MainActivity) getActivity()).refreshNoticeUnreadCount();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override public void onError(int error, String errorMsg) {
                    HDLog.e(TAG, "error:" + errorMsg);
                }

                @Override public void onAuthenticationException() {
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            HDApplication.getInstance().logout();
                        }
                    });
                }
            });
        } else {
            mTabLayout.hideMsg(0);
            mTabLayout.hideMsg(1);
            mTabLayout.hideMsg(2);
        }
    }

    public int getUnreadCount() {
        return totalUnread;
    }

    /**
     * 递减未读数
     */
    public void diminishingUnreadCount() {
        if (totalUnread > 0) {
            --totalUnread;
        }
    }
}
