package com.easemob.helpdesk.activity.transfer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.adapter.AgentsAdapter;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 转接界面的子界面：客服列表界面
 * <p/>
 * Created by liyuzhao on 16/3/7.
 */
public class TransferAgentFragment extends Fragment {

    /**
     * 跳转弹窗的REQUEST_CODE
     */
    private static final int REQUEST_CODE_ALERTDIALOG = 1;

    /**
     * 列表View
     */
    @BindView(R.id.listview)
    public ListView mListView;
    /**
     * 客服列表数据
     */
    private List<AgentUser> agentUsers = new ArrayList<AgentUser>();
    /**
     * 列表适配器
     */
    private AgentsAdapter adapter;
    /**
     * 搜索框清空按钮
     */
    @BindView(R.id.search_clear)
    public ImageButton clearSearch;
    /**
     * 搜索框文本先写控件
     */
    @BindView(R.id.query)
    public EditText query;
    /**
     * 下拉刷新的View
     */
    @BindView(R.id.swipe_ly_agent)
    public SwipeRefreshLayout mSwipeLayout;
    /**
     * 加载中的提示View
     */
    private Dialog pd = null;

    /**
     * 选中的客服人员
     */
    private AgentUser currentAgentUser;

    /**
    * 管理员模式惨淡
    */
    private boolean fromManager = false;

    private Unbinder unbinder;
    /**
     * 加载xml布局中的View
     *
     * @param inflater           View的加载器
     * @param container          父view
     * @param savedInstanceState 传过来的数据
     * @return 加载后的View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer_agent, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    /**
     * 当界面创建后，执行的回调
     *
     * @param savedInstanceState 界面传过来的Bundle数据
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fromManager = getActivity().getIntent().getBooleanExtra("manager", false);

        initView();

        pd = DialogUtils.getLoadingDialog(getActivity(), "加载中...");
        pd.show();

        getAgentUserListFromRemote();
    }

    /**
     * 加载View，并为View添加监听
     */
    private void initView() {
        // search edittext
        query.setHint(R.string.hint_search);
        adapter = new AgentsAdapter(getActivity(), agentUsers);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentAgentUser = (AgentUser) adapterView.getItemAtPosition(i);
                startActivityForResult(
                        new Intent(getActivity(), AlertDialog.class).putExtra("msg", "确定转接该会话？"),
                        REQUEST_CODE_ALERTDIALOG);
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // http get agentUser list
                getAgentUserListFromRemote();
            }
        });
        mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_dark,
                R.color.holo_orange_light, R.color.holo_red_light);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable;
                if (mListView != null && mListView.getChildCount() > 0) {
                    //check if the first item of the list is visible
                    boolean firstItemVisible = mListView.getFirstVisiblePosition() == 0;
                    //check if the top of the first item is visible
                    boolean topOfFirstItemVisible = mListView.getChildAt(0).getTop() == 0;
                    //enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                    mSwipeLayout.setEnabled(enable);
                } else {
                    mSwipeLayout.setEnabled(true);
                }
            }
        });

        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getChatFilter().filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

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
     * 页面返回的回调
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        Intent数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ALERTDIALOG) {
                String userId = currentAgentUser.user.getUserId();
                Intent intent = new Intent();
                intent.putExtra("userId", userId);
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


    /*
     * 从网络异步加载客服人员列表
     */
    private void getAgentUserListFromRemote() {
        HDClient.getInstance().agentManager().getAgentList(fromManager, new HDDataCallBack<List<AgentUser>>() {
            @Override
            public void onSuccess(final List<AgentUser> value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        agentUsers.clear();
                        agentUsers.addAll(value);
                        Collections.sort(agentUsers, new Comparator<AgentUser>() {
                            @Override
                            public int compare(AgentUser lhs, AgentUser rhs) {
                                try {
                                    String lState = lhs.user.getOnLineState();
                                    String rState = rhs.user.getOnLineState();
                                    int lIntState = CommonUtils.getAgentStatus(lState);
                                    int rIntState = CommonUtils.getAgentStatus(rState);
                                    return lIntState-rIntState;
                                }catch (Exception e){
                                }
                                return 0;
                            }
                        });
                        adapter.refresh();
                        mSwipeLayout.setRefreshing(false);
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
                        closeDialog();
                        Toast.makeText(getActivity(), R.string.error_getAgentUserListFail,
                                Toast.LENGTH_SHORT).show();
                        mSwipeLayout.setRefreshing(false);
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
                        closeDialog();
                        mSwipeLayout.setRefreshing(false);
                        HDApplication.getInstance().logout();
                    }
                });
            }

        });

    }

    /**
     * 销毁隐藏加载提示View
     */
    public void closeDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
            pd = null;
        }
    }


    /**
     * 界面结束时调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDialog();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }
}
