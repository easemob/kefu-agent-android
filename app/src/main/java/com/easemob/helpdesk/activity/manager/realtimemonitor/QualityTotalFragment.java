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
import com.easemob.helpdesk.activity.manager.model.QualityTotalResponse;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class QualityTotalFragment extends Fragment {

	Unbinder unbinder;

	@BindView(R.id.avg_ar)
	public TextView avgAr;

	@BindView(R.id.avg_fr)
	public TextView avgfr;

	@BindView(R.id.avg_vm)
	public TextView avgVm;

	public QualityTotalFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.manager_realtime_fragment_quality_total, container, false);
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

	void loadData() {
		HDUser user = HDClient.getInstance().getCurrentUser();
		if (user == null) {
			return;
		}
		HelpDeskManager.getInstance().getMonitorQualityTotal(user.getTenantId(), new HDDataCallBack<String>() {
			@Override
			public void onSuccess(final String value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						parseQualityTotalResponse(value);

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
	private void parseQualityTotalResponse(String value) {
		Gson gson = new Gson();
		DecimalFormat df1=new DecimalFormat("#.#秒");
		DecimalFormat df2=new DecimalFormat("#.##分");
		QualityTotalResponse response = gson.fromJson(value, QualityTotalResponse.class);
		if (response != null && response.getTotalElements() > 0) {
			QualityTotalResponse.EntitiesBean bean = response.getEntities().get(0);
			avgAr.setText(df1.format(bean.getAvg_ar()));
			avgfr.setText(df1.format(bean.getAvg_fr()));
			avgVm.setText(df2.format(bean.getAvg_vm()));
		}
	}
}
