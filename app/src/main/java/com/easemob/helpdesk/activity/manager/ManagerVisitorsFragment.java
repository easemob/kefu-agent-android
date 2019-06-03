package com.easemob.helpdesk.activity.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.option.VisitorsScreenEntity;
import com.hyphenate.kefusdk.entity.user.HDUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/24.
 */
public class ManagerVisitorsFragment extends Fragment {

    private static final String TAG = ManagerVisitorsFragment.class.getSimpleName();
    private static final int REQUEST_CODE_FILTER = 0x01;
    private String[] mTitle = { "图表", "访客" };
    private Integer[] drawableId = { R.drawable.chart_icon, R.drawable.agents_icon };

    @BindView(R.id.iv_back) protected ImageView back;

    protected HDUser currentLoginUser;

    @BindView(R.id.content_layout) protected FrameLayout contentLayout;

    @BindView(R.id.tablayout) protected SegmentTabLayout segmentTabLayout;

    @BindView(R.id.tv_title) protected TextView tvTitle;

    private VisitorChartFragment visitorChartFragment;
    private VisitorLoadFragment visitorLoadFragment;

    private int currentSelectedIndex = 0;

    private FragmentManager fragmentManager;

    private VisitorsScreenEntity screenEntity = new VisitorsScreenEntity();

    private Unbinder unbinder;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.manage_fragment_workload, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentSelectedIndex = savedInstanceState.getInt("selectedIndex", 0);
        }
        currentLoginUser = HDClient.getInstance().getCurrentUser();
        screenEntity.setCurrentTimeInfo(DateUtils.getTimeInfoByCurrentWeek().getStartTime(), DateUtils.getTimeInfoByCurrentWeek().getEndTime());
        fragmentManager = getFragmentManager();
        if (currentSelectedIndex == 0) {
            visitorChartFragment = new VisitorChartFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_layout, visitorChartFragment).commit();
        } else {
            visitorLoadFragment = new VisitorLoadFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_layout, visitorLoadFragment).commit();
        }

        initView();
        settingTabLayout();
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ((ManagerHomeActivity) getActivity()).back();
            }
        });
    }

    private void settingTabLayout() {
        //将Fragment添加入集合
        segmentTabLayout.setTabData(drawableId, R.drawable.baseline_icon);
        segmentTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                if (position == currentSelectedIndex) {
                    return;
                }
                currentSelectedIndex = position;
                switch (position) {
                    case 0:
                        if (visitorChartFragment == null) {
                            visitorChartFragment = new VisitorChartFragment();
                        }
                        fragmentManager.beginTransaction().replace(R.id.content_layout, visitorChartFragment).commit();
                        tvTitle.setText("来访客户统计");
                        break;
                    case 1:
                        if (visitorLoadFragment == null) {
                            visitorLoadFragment = new VisitorLoadFragment();
                        }
                        fragmentManager.beginTransaction().replace(R.id.content_layout, visitorLoadFragment).commit();
                        tvTitle.setText("访客");
                        break;
                }
            }

            @Override public void onTabReselect(int position) {

            }
        });
        segmentTabLayout.setCurrentTab(0);
    }

    private void initView() {
        segmentTabLayout.setVisibility(View.VISIBLE);
        tvTitle.setText("来访客户统计");
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedIndex", currentSelectedIndex);
    }

    @OnClick(R.id.iv_filter) public void onClickByFilter(View view) {
        Intent intent = new Intent();
        intent.setClass(getContext(), VisitorsFilterActivity.class);
        intent.putExtra("channel", screenEntity.getCurrentChannelString());
        intent.putExtra("timeinfo", new TimeInfo(screenEntity.getCurrentTimeInfo().getStartTime(), screenEntity.getCurrentTimeInfo().getEndTime()));
        startActivityForResult(intent, REQUEST_CODE_FILTER);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_FILTER) {
                TimeInfo timeInfo = (TimeInfo) data.getSerializableExtra("timeinfo");
                screenEntity.setCurrentTimeInfo(timeInfo.getStartTime(), timeInfo.getEndTime());
                String channelString = data.getStringExtra("channel");
                if (!TextUtils.isEmpty(channelString)) {
                    screenEntity.setCurrentChannelString(channelString);
                } else {
                    screenEntity.setCurrentChannelString("V_ORIGINTYPE");
                }

                if (visitorLoadFragment != null) {
                    visitorLoadFragment.setScreenEntity(screenEntity);
                    visitorLoadFragment.onRefresh();
                }
                if (visitorChartFragment != null) {
                    visitorChartFragment.setScreenEntity(screenEntity);
                    visitorChartFragment.refreshCurrentView();
                }
            }
        }
    }

    public VisitorsScreenEntity getScreenEntity() {
        return screenEntity;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
