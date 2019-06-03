package com.easemob.helpdesk.activity.transfer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.entity.TabEntity;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;

/**
 * 会话转接界面，包含转接到客服和转接到技能组
 * <p/>
 * Created by liyuzhao on 16/3/7.
 */
public class TransferActivity extends BaseActivity {

    /**
     * 界面中含有的子界面集合
     */
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

    /**
     * 显示的Fragment的标题
     */
    private String[] mTitles = { "客服", "技能组" };
    /**
     * 滑动的ViewPager，
     */
    private ViewPager mViewPager;
    /**
     * 放标题的自定义View，
     */
    private CommonTabLayout mTabLayout;
    /**
     * 自定义CommonTabLayout用到的标题集合
     */
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    /**
     * 客服列表界面
     */
    private TransferAgentFragment agentFragment = new TransferAgentFragment();
    /**
     * 技能组列表界面
     */
    private TransferSkillGroupFragment skillGroupFragment = new TransferSkillGroupFragment();

    private View viewBack;

    private int position = -1;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_transfer);
        mFragments.add(agentFragment);
        mFragments.add(skillGroupFragment);
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i]));
        }
        initView();
    }

    /**
     * 初始化View，并为View添加监听
     */
    private void initView() {
        position = getIntent().getIntExtra("position", -1);
        viewBack = $(R.id.iv_back);
        mViewPager = $(R.id.viewpager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        /** with nothing **/
        mTabLayout = $(R.id.tablayout);
        mTabLayout.setTabData(mTabEntities);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override public void onTabReselect(int position) {

            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);

            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });
        viewBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

    //    /**
    //     * 返回按钮点击事件
    //     *
    //     * @param view
    //     */
    //    public void back(View view) {
    //        finish();
    //    }

    public int getPosition() {
        return position;
    }

    /**
     * 滑动的View适配器，功能是根据滑动切换界面
     */
    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public int getCount() {
            return mFragments.size();
        }

        @Override public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
}
