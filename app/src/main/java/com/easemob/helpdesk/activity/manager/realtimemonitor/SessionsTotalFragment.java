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
import com.easemob.helpdesk.activity.manager.model.SessionsTotalResponse;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class SessionsTotalFragment extends Fragment {


	@BindView(R.id.new_sessions_total)
	public TextView newSessionsTotal;

	@BindView(R.id.valid_sessions_total)
	public TextView validSessionsTotal;

	@BindView(R.id.invalid_sessions_total)
	public TextView invalidSessionsTotal;

	@BindView(R.id.ended_sessions_total)
	public TextView endedSessionsTotal;

	Unbinder unbinder;

	public SessionsTotalFragment() {
		// Required empty public constructor
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.manager_realtime_fragment_session_total, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
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
		HelpDeskManager.getInstance().getMonitorSessionTotal(loginUser.getTenantId(), new HDDataCallBack<String>() {
			@Override
			public void onSuccess(final String value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						parseSessionsTotalResponse(value);

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

	private void parseSessionsTotalResponse(String value) {
		Gson gson = new Gson();
		DecimalFormat df=new DecimalFormat("#.#条");
		SessionsTotalResponse response = gson.fromJson(value, SessionsTotalResponse.class);
		if (response != null && response.getTotalElements() > 0) {
			SessionsTotalResponse.EntitiesBean entitiesBean = response.getEntities().get(0);
			newSessionsTotal.setText(df.format(entitiesBean.getCnt_sc()));
			validSessionsTotal.setText(df.format(entitiesBean.getSe_1()));
			invalidSessionsTotal.setText(df.format(entitiesBean.getSe_0()));
			endedSessionsTotal.setText(df.format(entitiesBean.getCnt_csc()));
		}
	}
}
