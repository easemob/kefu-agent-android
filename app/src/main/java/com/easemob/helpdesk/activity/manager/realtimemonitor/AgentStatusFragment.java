package com.easemob.helpdesk.activity.manager.realtimemonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.model.AgentStatusDist;
import com.easemob.helpdesk.widget.barview.BarView;
import com.easemob.helpdesk.widget.barview.Pillar;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 13/03/2017.
 */

public class AgentStatusFragment extends Fragment {

    private static final String TAG = "AgentStatusFragment";
    @BindView(R.id.barView)
    protected BarView barView;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_realtime_fragment_agentstatus, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadData();

    }

    private void setViewData(int offline, int hidden, int leave,int busy, int online){

        int max = Math.max(Math.max(Math.max(Math.max(offline, hidden), leave), busy), online);
        barView.setMaxValue(max);
        barView.setBackgroundColor(Color.WHITE);
        List<Pillar> pillars = new ArrayList<Pillar>();
        pillars.add(new Pillar(online,"空闲","#ffffff","#9ef14d"));
        pillars.add(new Pillar(busy, "忙碌","#ffffff","#fb8b46"));
        pillars.add(new Pillar(leave,"离开","#ffffff","#5cbcf2"));
        pillars.add(new Pillar(hidden,"隐身","#ffffff","#fece53"));
        pillars.add(new Pillar(offline,"离线","#ffffff","#c6cacb"));
        barView.setPillars(pillars);
    }



    private void loadData(){
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null){
            return;
        }

        HelpDeskManager.getInstance().getMonitorAgentStatus(loginUser.getTenantId(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        AgentStatusDist dist = gson.fromJson(value, AgentStatusDist.class);
                        if (dist == null) {
                            return;
                        }
                        try {
                            if (dist.getEntities().size() > 0){
                                AgentStatusDist.EntitiesBean bean =   dist.getEntities().get(0);
                                int offline = bean.getOffline();
                                int hidden = bean.getHidden();
                                int leave = bean.getLeave();
                                int busy = bean.getBusy();
                                int online = bean.getOnline();
                                setViewData(offline, hidden, leave, busy, online);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "error:" + errorMsg);
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

        });

    }



    @Override
    public void onDestroyView() {
        HDLog.d(TAG, "onDestroyView");
        super.onDestroyView();
        unbinder.unbind();
    }


}
