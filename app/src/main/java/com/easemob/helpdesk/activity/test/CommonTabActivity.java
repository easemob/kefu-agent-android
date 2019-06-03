package com.easemob.helpdesk.activity.test;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.TabEntity;
import com.easemob.helpdesk.utils.ViewFindUtils;
import com.flyco.roundview.RoundTextView;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.flyco.tablayout.utils.UnreadMsgUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by liyuzhao on 16/2/23.
 */
public class CommonTabActivity extends FragmentActivity {
    private Context mContext = this;
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private ArrayList<Fragment> mFragments2 = new ArrayList<Fragment>();

    private String[] mTitles = {"首页", "消息", "联系人", "更多"};

    private int[] mIconUnselectIds = {
            R.drawable.tab_home_unselect, R.drawable.tab_speech_unselect,
            R.drawable.tab_contact_unselect, R.drawable.tab_more_unselect
    };

    private int[] mIconSelectIds = {
            R.drawable.tab_home_select, R.drawable.tab_speech_select,
            R.drawable.tab_contact_select, R.drawable.tab_more_select};

    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private View mDecorView;
    private ViewPager mViewPager;
    private CommonTabLayout mTabLayout_1;

    private TextView testTV;
    private View icontext;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tab);
        testTV = (TextView) findViewById(R.id.testTv);


        for (String title : mTitles) {
            mFragments.add(SimpleCardFragment.getInstance("Switch ViewPager " + title));
        }

        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }

        mDecorView = getWindow().getDecorView();


        mViewPager = ViewFindUtils.find(mDecorView, R.id.viewpager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        /** with nothing **/
        mTabLayout_1 = ViewFindUtils.find(mDecorView, R.id.tablayout);

        tl_2();

        //两位数
        mTabLayout_1.showMsg(0, 55);
        mTabLayout_1.setMsgMargin(0, -5, 5);

        //三位数
        mTabLayout_1.showMsg(1, 100);
        mTabLayout_1.setMsgMargin(1, -5, 5);
        mTabLayout_1.hideMsg(1);

        //设置未读消息红点
        mTabLayout_1.showDot(2);
        RoundTextView rtv_2_2 = mTabLayout_1.getMsgView(2);
        if (rtv_2_2 != null) {
            UnreadMsgUtils.setSize(rtv_2_2, dp2px(7.5f));
        }


        //设置未读消息背景
        mTabLayout_1.showMsg(3, 5);
        mTabLayout_1.setMsgMargin(3, 0, 5);
        RoundTextView rtv_2_3 = mTabLayout_1.getMsgView(3);
        if (rtv_2_3 != null) {
            rtv_2_3.getDelegate().setBackgroundColor(Color.parseColor("#6D8FB0"));
        }

    }

    Random mRandom = new Random();

    private void tl_2() {
        mTabLayout_1.setTabData(mTabEntities);
        mTabLayout_1.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }


            @Override
            public void onTabReselect(int position) {
                if (position == 0) {
                    mTabLayout_1.showMsg(0, mRandom.nextInt(100) + 1);
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout_1.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(1);
    }


    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }


    protected int dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
