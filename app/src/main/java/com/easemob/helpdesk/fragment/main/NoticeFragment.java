package com.easemob.helpdesk.fragment.main;

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
import com.easemob.helpdesk.activity.main.MainActivity;
import com.easemob.helpdesk.entity.TabEntity;
import com.easemob.helpdesk.utils.AvatarUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.gsonmodel.main.UnReadCountBean;
import com.hyphenate.kefusdk.manager.NoticeManager;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/3/10.
 */
public class NoticeFragment extends Fragment {

    private static final String TAG = NoticeFragment.class.getSimpleName();
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private String[] mTabTitles = {"全部通知", "管理员通知", "系统通知"};
    private String[] mTitles = {"未读通知", "已读通知"};

    @BindView(R.id.tv_title)
    protected TextView tvTitle;
    @BindView(R.id.framelayout)
    protected FrameLayout mFrameLayout;
    @BindView(R.id.tablayout)
    protected CommonTabLayout mTabLayout;
    private FragmentManager mFragmentManager;

    @BindView(R.id.tv_readed_notice)
    protected TextView tvReadedNotice;

    @BindView(R.id.iv_avatar)
    protected ImageView ivAvatar;
    @BindView(R.id.iv_status)
    protected ImageView ivStatus;
    private int currentSelectedIndex = 0;

    private HDUser loginUser;
    private Unbinder unbinder;

    private NoticeListFragment noticeListFragment;
    private boolean isUnreadSettings = true;
    private String  typeSettings = "all";

    private NoticeManager noticeManager;

    private volatile int totalUnread;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            currentSelectedIndex = savedInstanceState.getInt("selectedIndex", 0);
        }
        loginUser = HDClient.getInstance().getCurrentUser();
        noticeManager = new NoticeManager(loginUser);
        for (int i = 0; i < mTabTitles.length; i++){
            mTabEntities.add(new TabEntity(mTabTitles[i]));
        }
        mFragmentManager = getChildFragmentManager();
        mTabLayout.setTabData(mTabEntities);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                if (position == 0){
                    typeSettings = "all";
                }else if (position == 1){
                    typeSettings = "agent";
                }else if (position == 2){
                    typeSettings = "system";
                }
                currentSelectedIndex = position;
                setTabSelection(position);
            }

            @Override
            public void onTabReselect(int position) {

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


    @Override
    public void onSaveInstanceState(Bundle outState) {
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
        if(ivAvatar != null)
            AvatarUtils.refreshAgentAvatar(getActivity(), ivAvatar);
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


    @OnClick(R.id.tv_readed_notice)
    public void clickByReadedNotice(View view){
        if (isUnreadSettings){
            tvReadedNotice.setText("查看未读");
            tvTitle.setText(mTitles[1]);
        }else{
            tvReadedNotice.setText("查看已读");
            tvTitle.setText(mTitles[0]);
        }
        isUnreadSettings = !isUnreadSettings;
        setTabSelection(currentSelectedIndex);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }


    public void refreshTabUnreadCount(){
        if (loginUser == null){
            return;
        }
        if (isUnreadSettings){
            noticeManager.getUnReadCount(new HDDataCallBack<List<UnReadCountBean>>() {
                @Override
                public void onSuccess(final List<UnReadCountBean> value) {
                    if (getActivity() == null){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                for (UnReadCountBean bean : value){
                                    String type = bean.getType();
                                    int count = bean.getCount_unread();
                                    int pos = 0;
                                    if (type.equals("all")){
                                        pos = 0;
                                        totalUnread = count;
                                    }else if (type.equals("agent")){
                                        pos = 1;
                                    }else if (type.equals("system")){
                                        pos = 2;
                                    }
                                    if (count > 0){
                                        mTabLayout.showMsg(pos, count);
                                    }else{
                                        mTabLayout.hideMsg(pos);
                                    }
                                }
                                ((MainActivity)getActivity()).refreshNoticeUnreadCount();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    HDLog.e(TAG, "error:" + errorMsg);

                }

                @Override
                public void onAuthenticationException() {
                    if (getActivity() == null){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HDApplication.getInstance().logout();
                        }
                    });
                }
            });
        }else{
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
    public void  diminishingUnreadCount(){
        if (totalUnread > 0){
            --totalUnread;
        }
    }

}
