package com.easemob.helpdesk.activity.manager;

import android.app.Activity;
import android.content.Intent;
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
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.TechChannel;
import com.hyphenate.kefusdk.entity.option.WorkloadScreenEntity;
import com.hyphenate.kefusdk.entity.user.HDUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/22.
 */
public class ManagerWorkloadFragment extends Fragment {

    private static final String TAG = ManagerWorkloadFragment.class.getSimpleName();
    private static final int REQUEST_CODE_SCREENING = 0x01;
    private String[] mTitle = { "图表", "客服" };
    private Integer[] drawableId = { R.drawable.chart_icon, R.drawable.agents_icon };

    @BindView(R.id.iv_back) protected ImageView back;

    @BindView(R.id.tv_title) protected TextView tvTitle;

    protected HDUser currentLoginUser;

    @BindView(R.id.content_layout) protected FrameLayout contentLayout;

    @BindView(R.id.tablayout) protected SegmentTabLayout segmentTabLayout;

    private WorkloadChartFragment workloadChartFragment;
    private WorkloadAgentsFragment workloadAgentsFragment;

    private int currentSelectedIndex = 0;

    private FragmentManager fragmentManager;

    private WorkloadScreenEntity screenEntity = new WorkloadScreenEntity();

    private Unbinder unbinder;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_fragment_workload, container, false);
        unbinder = ButterKnife.bind(this, view);
        tvTitle.setText("工作量");
        return view;
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
            workloadChartFragment = new WorkloadChartFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_layout, workloadChartFragment).commit();
        } else {
            workloadAgentsFragment = new WorkloadAgentsFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_layout, workloadAgentsFragment).commit();
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
                        if (workloadChartFragment == null) {
                            workloadChartFragment = new WorkloadChartFragment();
                        }
                        fragmentManager.beginTransaction().replace(R.id.content_layout, workloadChartFragment).commit();
                        tvTitle.setText("工作量");
                        break;
                    case 1:
                        if (workloadAgentsFragment == null) {
                            workloadAgentsFragment = new WorkloadAgentsFragment();
                        }
                        fragmentManager.beginTransaction().replace(R.id.content_layout, workloadAgentsFragment).commit();
                        tvTitle.setText("客服");
                        break;
                }
            }

            @Override public void onTabReselect(int position) {

            }
        });

        segmentTabLayout.setCurrentTab(currentSelectedIndex);
    }

    private void initView() {
    }

    @OnClick(R.id.iv_filter) public void onClickByfilter(View view) {

        Intent intent = new Intent();
        intent.setClass(getContext(), WorkloadFilter.class);
        intent.putExtra("timeinfo", new TimeInfo(screenEntity.getCurrentTimeInfo().getStartTime(), screenEntity.getCurrentTimeInfo().getEndTime()));
        intent.putExtra("showtag", true);
        intent.putExtra("showtechchannel", true);
        startActivityForResult(intent, REQUEST_CODE_SCREENING);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SCREENING) {
                TimeInfo currentTimeInfo = (TimeInfo) data.getSerializableExtra("timeinfo");

                screenEntity.setCurrentTimeInfo(currentTimeInfo.getStartTime(), currentTimeInfo.getEndTime());

                if (data.hasExtra("techChannel")) {
                    screenEntity.setCurrentTechChannel((TechChannel) data.getSerializableExtra("techChannel"));
                } else {
                    screenEntity.setCurrentTechChannel(null);
                }
                if (data.hasExtra("ids")) {
                    String currentTagIds = data.getStringExtra("ids");
                    screenEntity.setCurrentTagIds(currentTagIds.replace("[", "").replace("]", ""));
                } else {
                    screenEntity.setCurrentTagIds("all");
                }
                if (data.hasExtra("agentUserId")) {
                    screenEntity.setAgentUserId(data.getStringExtra("agentUserId"));
                } else {
                    screenEntity.setAgentUserId(null);
                }
                refreshFragment();
            }
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedIndex", currentSelectedIndex);
    }

    private void refreshFragment() {
        if (workloadChartFragment != null) {
            workloadChartFragment.setScreenEntity(screenEntity);
            workloadChartFragment.refreshCurrentView();
        }
        if (workloadAgentsFragment != null) {
            workloadAgentsFragment.setScreenEntity(screenEntity);
            workloadAgentsFragment.onRefresh();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
