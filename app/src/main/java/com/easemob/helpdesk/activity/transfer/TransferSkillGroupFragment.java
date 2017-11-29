package com.easemob.helpdesk.activity.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.adapter.AgentQueueAdapter;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentQueue;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 转接子界面：技能组列表界面
 * <p/>
 * Created by liyuzhao on 16/3/7.
 */
public class TransferSkillGroupFragment extends Fragment {

    private static final String TAG = TransferSkillGroupFragment.class.getSimpleName();

    /**
     * 技能组提示View的REQUEST_CODE
     */
    private static final int REQUEST_CODE_SKILLGROUP_ALERTDIALOG = 1;
    /**
     * 技能组数据集合
     */
    private List<AgentQueue> agentQueues = new ArrayList<>();
    /**
     * 列表View
     */
    @BindView(R.id.listview)
    public ListView mListView;
    /**
     * 筛选框的清空按钮
     */
    @BindView(R.id.search_clear)
    public ImageButton clearSearch;
    /**
     * 筛选框
     */
    @BindView(R.id.query)
    public EditText query;
    /**
     * 下拉刷新view
     */
    @BindView(R.id.swipe_ly_agent)
    public SwipeRefreshLayout mSwipeLayout;
    /**
     * 技能组列表适配器
     */
    private AgentQueueAdapter adapter;
    /**
     * 当前选中的技能组
     */
    private AgentQueue currentAgentQueue;

    private Unbinder unbinder;

    /**
     * 加载xml布局中的View
     *
     * @param inflater           加载器
     * @param container          父view
     * @param savedInstanceState 传过来的Bundle数据
     * @return 加载后的View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer_agent, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    /**
     * 界面创建后调用的回调
     *
     * @param savedInstanceState 传过来的Bundle数据
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        getSkillGroupsFromRemote();
    }

    /**
     * 初始化View并设置监听
     */
    private void initView() {
        query.setHint(R.string.hint_search);
        adapter = new AgentQueueAdapter(getActivity(), agentQueues);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AgentQueue agentQueue = (AgentQueue) adapterView.getItemAtPosition(position);
                currentAgentQueue = agentQueue;
                if (agentQueue.totalAgentCount > 0) {
                    startActivityForResult(new Intent(getActivity(), AlertDialog.class).putExtra("msg", "确定转接该会话？"), REQUEST_CODE_SKILLGROUP_ALERTDIALOG);
                }
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // http get skillgroup list
                getSkillGroupsFromRemote();
            }
        });
        mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark,
                R.color.holo_orange_light, R.color.holo_red_light);
        // search edittext
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getAgentFilter().filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                query.getText().clear();
            }
        });
    }

    /**
     * 页面从栈中返回到头部的回调
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        携带的Intent数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SKILLGROUP_ALERTDIALOG) {
                long queueId = currentAgentQueue.queueId;
                Intent intent = new Intent();
                intent.putExtra("queueId", queueId);
                if (getActivity() instanceof TransferActivity){
                    int position = ((TransferActivity)getActivity()).getPosition();
                    if (position != -1){
                        intent.putExtra("position", position);
                    }
                }
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        }
    }


    /**
     * 从网络异步加载技能组数据
     */
    private void getSkillGroupsFromRemote() {
        HDClient.getInstance().agentManager().getSkillGroupsFromRemote(new HDDataCallBack<List<AgentQueue>>() {

            @Override
            public void onSuccess(final List<AgentQueue> value) {
                HDLog.d(TAG, "value:" + value);
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (agentQueues){
                            List<AgentQueue> list = value;
                            agentQueues.clear();
                            for (AgentQueue agent : list) {
                                if (agent.queueGroupType != null && agent.queueGroupType.equals("SystemDefault")){
                                    continue;
                                }
                                agentQueues.add(agent);
                            }
                            adapter.notifyDataSetChanged();
                            mSwipeLayout.setRefreshing(false);
                        }
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
                        Toast.makeText(getActivity(), R.string.error_getAgentUserListFail, Toast.LENGTH_SHORT).show();
                        mSwipeLayout.setRefreshing(false);
                    }
                });
            }

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }
}
