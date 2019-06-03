package com.easemob.helpdesk.activity.manager.realtimemonitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.model.VisitorTotalResponse;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class VisitorTotalFragment extends Fragment {

	Unbinder unbinder;

	@BindView(R.id.web_total)
	public TextView webTotal;

	@BindView(R.id.wechat_total)
	public TextView wechatTotal;

	@BindView(R.id.weibo_total)
	public TextView weiboTotal;

	@BindView(R.id.apps_total)
	public TextView appsTotal;


	public VisitorTotalFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.manager_realtime_fragment_visitor_total, container, false);

		unbinder = ButterKnife.bind(this, view);

		return view;
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();

		super.onDestroyView();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadData();
	}

	private void loadData() {
		HDUser loginUser = HDClient.getInstance().getCurrentUser();
		if (loginUser == null) {
			return;
		}
		HelpDeskManager.getInstance().getMonitorVisitorTotal(loginUser.getTenantId(), new HDDataCallBack<String>() {
			@Override
			public void onSuccess(final String value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						parseVisitorTotalResponse(value);

					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {

					}
				});
			}

			@Override
			public void onAuthenticationException() {
				if (getActivity() == null) {
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
	}

	private void parseVisitorTotalResponse(String value) {
		Gson gson = new Gson();
		VisitorTotalResponse response = gson.fromJson(value, VisitorTotalResponse.class);
		if (response != null && response.getTotalElements() > 0) {
			VisitorTotalResponse.EntitiesBean entitiesBean = response.getEntities().get(0);
			webTotal.setText(String.format("%d个", entitiesBean.getWebim()));
			wechatTotal.setText(String.format("%d个", entitiesBean.getWeixin()));
			weiboTotal.setText(String.format("%d个", entitiesBean.getWeibo()));
			appsTotal.setText(String.format("%d个", entitiesBean.getApp()));
		}
	}

}
