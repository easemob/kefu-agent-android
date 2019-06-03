package com.easemob.helpdesk.activity.visitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.ViewFindUtils;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.entity.user.HDVisitorUser;
import java.util.ArrayList;

/**
 * Created by liyuzhao on 16/2/29.
 */
public class VisitorDetailActivity extends BaseActivity implements OnTabSelectListener {

    private SlidingTabLayout tabLayout;
    private View decorView;
    private ViewPager viewPager;
    private final String[] mTitls = {"详情", "标签"};
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private HDVisitorUser hdUser;
    private VisitorInformationFragment informationFragment;
    private TextView tvNick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_visitor_detail);
        Intent intent = getIntent();
        hdUser = intent.getParcelableExtra("user");
        initView();

        informationFragment = new VisitorInformationFragment();
        informationFragment.setArguments(intent.getExtras());
        mFragments.add(informationFragment);

//        mFragments.add(SimpleCardFragment.getInstance(mTitls[2]));

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        tabLayout.setViewPager(viewPager);
        tabLayout.setOnTabSelectListener(this);
    }

    private void initView(){
        decorView = getWindow().getDecorView();
        tabLayout = ViewFindUtils.find(decorView, R.id.slidingtablayout);
        viewPager = ViewFindUtils.find(decorView, R.id.viewpager);
        tvNick = ViewFindUtils.find(decorView, R.id.tv_nick);

        tabLayout.setTabWidth(CommonUtils.convertPx2Dip(this,getWindowManager().getDefaultDisplay().getWidth()/2));
    }

    public void updateCurrentNick(String nick){
        if(tvNick != null){
            tvNick.setText(nick);
        }
    }


    public void back(View view){
        finish();
    }

    @Override
    public void onTabSelect(int position) {
//        Toast.makeText(this, "onTabSelect&position--->" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTabReselect(int position) {

    }


    private class MyPagerAdapter extends FragmentPagerAdapter{
        public MyPagerAdapter(FragmentManager fm){
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
//            return super.getPageTitle(position);
            return mTitls[position];
        }
    }
}
