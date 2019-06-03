package com.easemob.helpdesk.activity.manager.realtimemonitor;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.model.ResponseTimeResponse;
import com.easemob.helpdesk.adapter.manager.RealTimeBaseAdapterItem;
import com.easemob.helpdesk.adapter.manager.RealtimeAdapter;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListResponseTimeFragment extends Fragment {

	private boolean isGroup = false;
	private static boolean isProcessing = false;

	private RadioGroup radioGroup;
	private EasyRecyclerView responseTimeList;
	private RealtimeAdapter responseTimeAdapter;

	private List<RealTimeBaseAdapterItem> responseTimeListData = new ArrayList<>();

	public ListResponseTimeFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.manager_realtime_fragment_list, container, false);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

		radioGroup = (RadioGroup) view.findViewById(R.id.realtime_list_radioGroup);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
				switch (checkedId) {
					case R.id.realtime_list_radio_kefu:
						isGroup = false;
						break;
					case R.id.realtime_list_radio_skill_group:
						isGroup = true;
						break;
				}
				loadData();
			}
		});

		responseTimeList = (EasyRecyclerView) view.findViewById(R.id.realtime_list);
		responseTimeList.setLayoutManager(layoutManager);
		responseTimeAdapter = new RealtimeAdapter(getActivity());
		responseTimeAdapter.sort(new Comparator<RealTimeBaseAdapterItem>() {
			@Override
			public int compare(RealTimeBaseAdapterItem o1, RealTimeBaseAdapterItem o2) {
				if (o1 == null || o2 == null) {
					return 0;
				}
				if (o1.getIndex() > o2.getIndex()) {
					return 1;
				} else if (o1.getIndex() > o2.getIndex()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		responseTimeList.setAdapter(responseTimeAdapter);
		loadData();
		return view;
	}

	private void loadData() {
		HDUser user = HDClient.getInstance().getCurrentUser();
		if (user == null || isProcessing) {
			return;
		}

		synchronized (this) {
			isProcessing = true;
		}

		clearList();
		HelpDeskManager.getInstance().getMonitorListResponseTime(user.getTenantId(), isGroup, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(final String value) {
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						parseListResponseTimeResponse(value);

						if (isGroup) {
							radioGroup.check(R.id.realtime_list_radio_skill_group);
						} else {
							radioGroup.check(R.id.realtime_list_radio_kefu);
						}

						synchronized (this) {
							isProcessing = false;
						}
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {
				synchronized (this) {
					isProcessing = false;
				}

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
				synchronized (this) {
					isProcessing = false;
				}

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

	private void clearList() {
		responseTimeAdapter.clear();
		responseTimeListData.clear();
		responseTimeAdapter.notifyDataSetChanged();
	}

	private void parseListResponseTimeResponse(String value) {
		Gson gson = new Gson();
		DecimalFormat df=new DecimalFormat("#.#秒");
		ResponseTimeResponse response = gson.fromJson(value, ResponseTimeResponse.class);
		if (response != null && response.getTotalElements() > 0) {
			for(int i = 0; i < response.getTotalElements(); i++) {
				RealTimeBaseAdapterItem item = new RealTimeBaseAdapterItem();
				ResponseTimeResponse.EntitiesBean bean = response.getEntities().get(i);
				item.setIndex(bean.getIndex());
				item.setName(bean.getName());
				item.setValue(df.format(bean.getAvg_rt()));
				item.setImg(null);
				responseTimeListData.add(item);
			}
			responseTimeAdapter.addAll(responseTimeListData);
			responseTimeAdapter.notifyDataSetChanged();
		}
	}

}
