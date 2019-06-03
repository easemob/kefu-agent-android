package com.easemob.helpdesk.activity.manager;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.realtimemonitor.AgentLoadInfoFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.AgentStatusFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.ListFirstResponseFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.ListResponseTimeFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.ListSessionStartFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.QualityTotalFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.SessionsTotalFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.VisitorTotalFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.VistorMarkFragment;
import com.easemob.helpdesk.activity.manager.realtimemonitor.WaitCountFragment;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class RealTimeMonitorFragment extends Fragment implements OnTabSelectListener {

	private static String TAG = "RealTimeMonitorFragment";

	@BindView(R.id.iv_avatar)
	protected ImageView ivAvatar;

	@BindView(R.id.iv_status)
	protected ImageView ivStatus;

	@BindView(R.id.slidingtablayout)
	protected SlidingTabLayout mTabLayout;

	@BindView(R.id.viewpager)
	protected ViewPager mViewPager;

	private AgentStatusFragment agentStatusFragment;
	private AgentLoadInfoFragment agentLoadInfoFragment;
	private WaitCountFragment waitCountFragment;
	private SessionsTotalFragment sessionsTotalFragment;
	private VisitorTotalFragment visitorTotalFragment;
	private QualityTotalFragment qualityTotalFragment;
	private ListSessionStartFragment sessionStartFragment;
	private ListFirstResponseFragment firstResponseFragment;
	private VistorMarkFragment vistorMarkFragment;
	private ListResponseTimeFragment responseTimeFragment;

	private ArrayList<Fragment> mFragments = new ArrayList<>();

	private String[] mTitles = {"客服状态分布", "客服负载情况", "访客排队情况", "会话数", "访客来源",
			"服务质量", "接起会话数", "平均首次响应时长", "满意度", "平均响应时长"};

	private Unbinder unbinder;

	private int currentSelect = 0;


	public RealTimeMonitorFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.manage_fragment_realtime_monitor, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}


	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadFirstStatus();
		addFragments();
		refreshAgentAvatar();

		mTabLayout.setViewPager(mViewPager, mTitles, this, mFragments);
		mTabLayout.setOnTabSelectListener(this);

	}

	private void addFragments() {
		if (agentStatusFragment == null) {
			agentStatusFragment = new AgentStatusFragment();
		}
		if (agentLoadInfoFragment == null) {
			agentLoadInfoFragment = new AgentLoadInfoFragment();
		}
		if (waitCountFragment == null) {
			waitCountFragment = new WaitCountFragment();
		}
		if (sessionsTotalFragment == null) {
			sessionsTotalFragment = new SessionsTotalFragment();
		}
		if (visitorTotalFragment == null) {
			visitorTotalFragment = new VisitorTotalFragment();
		}
		if (qualityTotalFragment == null) {
			qualityTotalFragment = new QualityTotalFragment();
		}
		if (sessionStartFragment == null) {
			sessionStartFragment = new ListSessionStartFragment();
		}
		if (firstResponseFragment == null) {
			firstResponseFragment = new ListFirstResponseFragment();
		}
		if (vistorMarkFragment == null) {
			vistorMarkFragment = new VistorMarkFragment();
		}
		if (responseTimeFragment == null) {
			responseTimeFragment = new ListResponseTimeFragment();
		}
		mFragments.add(agentStatusFragment);
		mFragments.add(agentLoadInfoFragment);
		mFragments.add(waitCountFragment);
		mFragments.add(sessionsTotalFragment);
		mFragments.add(visitorTotalFragment);
		mFragments.add(qualityTotalFragment);
		mFragments.add(sessionStartFragment);
		mFragments.add(firstResponseFragment);
		mFragments.add(vistorMarkFragment);
		mFragments.add(responseTimeFragment);
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();
		super.onDestroyView();
	}

	private void loadFirstStatus() {
		HDUser loginUser = HDClient.getInstance().getCurrentUser();
		if (loginUser != null) {
			refreshOnline(loginUser.getOnLineState());
		}
	}

	public void refreshOnline(String status) {
		CommonUtils.setAgentStatusView(ivStatus, status);
	}

	public void refreshAgentAvatar() {
		if (ivAvatar != null) {
			AvatarManager.getInstance().refreshAgentAvatar(getActivity(), ivAvatar);
		}
	}

	@Override
	public void onTabSelect(int position) {
		HDLog.d(TAG ,"onTabSelect position =" + position);
		currentSelect = position;
	}

	@Override
	public void onTabReselect(int position) {
	}
}