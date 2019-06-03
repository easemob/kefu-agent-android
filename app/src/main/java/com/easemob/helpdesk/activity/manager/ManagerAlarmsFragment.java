package com.easemob.helpdesk.activity.manager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.adapter.manager.AlarmsAdapter;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.gsonmodel.manager.AlarmsReponse;
import com.hyphenate.kefusdk.utils.HDLog;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiancruyff on 2017/11/13.
 */

public class ManagerAlarmsFragment extends Fragment {

    private static final String TAG = "ManagerAlarmsFragment";

    private Unbinder unbinder;

    @BindView(R.id.iv_avatar) protected ImageView ivAvatar;

    @BindView(R.id.iv_status) protected ImageView ivStatus;

    @BindView(R.id.alarms_list) protected EasyRecyclerView recyclerView;

    private AlarmsAdapter alarmsAdapter;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_fragment_alarms, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        recyclerView.setRefreshing(true);
        loadData();
        loadFirstStatus();
        refreshAgentAvatar();
    }

    private void initViews() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        alarmsAdapter = new AlarmsAdapter(getActivity());
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(alarmsAdapter);
        alarmsAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override public void onItemClick(int position) {
                AlarmsReponse.EntitiesBean bean = alarmsAdapter.getItem(position);
                Intent intent = new Intent();
                intent.setClass(getContext(), ManagerChatActivity.class);
                intent.putExtra("fromAlarms", true);
                intent.putExtra("sessionId", bean.getServiceSessionId());
                startActivity(intent);
            }
        });
        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                refresh();
            }
        });
    }

    public void refresh() {
        loadData();
    }

    private void loadFirstStatus() {
        HDApplication.getInstance().setHasAlarmNoti(false);
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser != null) {
            refreshOnline(loginUser.getOnLineState());
            ((ManagerHomeActivity) getActivity()).agentStatusUpdated(loginUser.getOnLineState());
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

    private void loadData() {
        HelpDeskManager.getInstance().getManageAlarms(new HDDataCallBack<String>() {
            @Override public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                recyclerView.setRefreshing(false);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        parseAlarmsResponse(value);
                    }
                });
            }

            @Override public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "error:" + error + ", errorMsg:" + errorMsg);
                recyclerView.setRefreshing(false);
            }
        });
    }

    private void parseAlarmsResponse(String value) {
        Gson gson = new Gson();
        AlarmsReponse reponse = gson.fromJson(value, AlarmsReponse.class);
        if (reponse != null && reponse.getEntities() != null) {
            alarmsAdapter.clear();
            if (reponse.getEntities().size() > 0) {
                alarmsAdapter.addAll(reponse.getEntities());
            } else {
                recyclerView.showEmpty();
            }
            alarmsAdapter.notifyDataSetChanged();
        }
    }

    @Override public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
