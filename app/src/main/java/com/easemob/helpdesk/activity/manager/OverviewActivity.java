package com.easemob.helpdesk.activity.manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.entity.WorkQualityAgent;
import com.easemob.helpdesk.entity.WorkloadAgent;
import com.easemob.helpdesk.utils.DateUtils;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static butterknife.ButterKnife.bind;

/**
 * Created by liyuzhao on 16/6/23.
 */
public class OverviewActivity extends BaseActivity {

    public static final String INDEX_INTENT_KEY = "index";
    public static final int INDEX_WORK_QUALITY_AGENT_DETAIL = 2;
    public static final int INDEX_WORK_LOAD_AGENT_DETAIL = 1;

    @BindView(R.id.tv_title)
    protected TextView tvTitle;

    @BindView(R.id.rl_back)
    protected View itvBack;

    @BindView(R.id.content_layout)
    protected FrameLayout contentLayout;

    static DecimalFormat df = new DecimalFormat("#.##");

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.manage_activity_overview);

        unbinder = ButterKnife.bind(this);
        if (savedInstanceState == null) {
            int index = getIntent().getIntExtra(INDEX_INTENT_KEY, 0);
            if (index == INDEX_WORK_QUALITY_AGENT_DETAIL){
                QuanlityAgentFragment agentFragment = new QuanlityAgentFragment();
                agentFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().add(R.id.content_layout, agentFragment).commit();

            }else if(index == INDEX_WORK_LOAD_AGENT_DETAIL){
                AgentDetailFragment agentDetailFragment = new AgentDetailFragment();
                agentDetailFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().add(R.id.content_layout, agentDetailFragment).commit();
            }
        }
        String title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)){
            tvTitle.setText(title);
        }
    }


    @OnClick(R.id.rl_back)
    public void onClickByBack(View view) {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    public static class QuanlityAgentFragment extends Fragment{

        @BindView(R.id.tv_satisfaction)
        protected TextView tvSatisfaction;

        @BindView(R.id.tv_answer_avg)
        protected TextView tvAnswerAvg;

        @BindView(R.id.tv_answer_max)
        protected TextView tvAnswerMax;

        @BindView(R.id.tv_session_answer_avg)
        protected TextView tvSessionAnswerAvg;

        @BindView(R.id.tv_session_answer_max)
        protected TextView tvSessionAnswerMax;
        private Unbinder unbinder;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.layout_workman_agent, container, false);
            unbinder = bind(this, rootView);
            return rootView;
        }


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null){
                WorkQualityAgent workQualityAgent = bundle.getParcelable("agent");
                if (workQualityAgent != null){

                    tvSatisfaction.setText(df.format(workQualityAgent.avg_vm));
                    tvAnswerAvg.setText(DateUtils.convertFromSecond((int) workQualityAgent.avg_fr));
                    tvAnswerMax.setText(DateUtils.convertFromSecond(workQualityAgent.max_fr));
                    tvSessionAnswerAvg.setText(DateUtils.convertFromSecond((int) workQualityAgent.avg_ar));
                    tvSessionAnswerMax.setText(DateUtils.convertFromSecond(workQualityAgent.max_ar));

                }
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (unbinder != null){
                unbinder.unbind();
            }

        }
    }



    public static class AgentDetailFragment extends Fragment {
        @BindView(R.id.tv_session_count)
        protected TextView tvSessionCount;

        @BindView(R.id.tv_accept_count)
        protected TextView tvAcceptCount;

        @BindView(R.id.tv_accept_tran_in_session_count)
        protected TextView tvAcceptTranInSessionCount;

        @BindView(R.id.tv_tran_out_end_session_count)
        protected TextView tvTranOutEndSessionCount;

        @BindView(R.id.tv_message_count)
        protected TextView tvMessageCount;

        @BindView(R.id.tv_sys_message_count)
        protected TextView tvSysMessageCount;

        @BindView(R.id.tv_messages_avg)
        protected TextView tvMessagesAvg;

        @BindView(R.id.tv_messages_max)
        protected TextView tvMessagesMax;

        @BindView(R.id.tv_session_time_avg)
        protected TextView tvSessionTimeAvg;

        @BindView(R.id.tv_session_time_max)
        protected TextView tvSessionTimeMax;

        private Unbinder unbinder;

        public AgentDetailFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.manage_fragment_agent_detail, container, false);
            unbinder = bind(this, rootView);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null){
                WorkloadAgent workloadAgent = bundle.getParcelable("agent");
                if (workloadAgent != null){
                    tvSessionCount.setText(df.format(workloadAgent.getCnt_sc()));
                    tvAcceptCount.setText(df.format(workloadAgent.getCnt_sdc()));
                    tvAcceptTranInSessionCount.setText(df.format(workloadAgent.getCnt_oc()) + "/" + df.format(workloadAgent.getCnt_tic()));
                    tvTranOutEndSessionCount.setText(df.format(workloadAgent.getCnt_toc()) + "/" + df.format(workloadAgent.getCnt_tc()));
                    tvMessageCount.setText(df.format(workloadAgent.getSum_am()) + "/" + df.format(workloadAgent.getSum_vm()));
                    tvSysMessageCount.setText(df.format(workloadAgent.getSum_sm()));
                    tvMessagesAvg.setText(df.format(workloadAgent.getAvg_mc()));
                    tvMessagesMax.setText(df.format(workloadAgent.getMax_mc()));
                    tvSessionTimeAvg.setText(DateUtils.convertFromSecond((int) workloadAgent.getAvg_wt()));
                    tvSessionTimeMax.setText(DateUtils.convertFromSecond((int) workloadAgent.getMax_wt()));
                }
            }

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (unbinder != null){
                unbinder.unbind();
            }
        }
    }


}
