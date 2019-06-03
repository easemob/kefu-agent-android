package com.easemob.helpdesk.activity.manager.realtimemonitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.model.AgentLoad;
import com.easemob.helpdesk.widget.dashboardview.DashboardView;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by liyuzhao on 13/03/2017.
 */

public class AgentLoadInfoFragment extends Fragment {

    private static final String TAG = "AgentLoadInfoFragment";
    @BindView(R.id.dashboardview)
    protected DashboardView dashboardView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_realtime_fragment_agentload, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    private void setViewData(int realValue, int maxValue){
        dashboardView.setMaxValue(maxValue);
        dashboardView.setRealTimeValue(realValue);
    }

    private void loadData() {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null){
            return;
        }
        HelpDeskManager.getInstance().getMonitorAgentLoad(loginUser.getTenantId(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Gson gson = new Gson();
                        AgentLoad agentLoad = gson.fromJson(value, AgentLoad.class);
                        if (agentLoad == null) {
                            return;
                        }
                        try{
                            int processingCount = agentLoad.getEntity().getProcessingSessionCount();
                            int totalCount = agentLoad.getEntity().getTotalMaxServiceSessionCount();
                            setViewData(processingCount, totalCount);

                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "error: " + errorMsg);
                if (getActivity() == null){
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
                if (getActivity() == null){
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

}
