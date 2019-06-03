package com.easemob.helpdesk.activity.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.PhraseAdapter;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDPhrase;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * 常用语界面
 *
 * @author liyuzhao
 */
public class PhraseActivity extends BaseActivity {

    private static final String TAG = PhraseActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CHILD = 0x01;
    private static final int REQUEST_CODE_SEARCH = 0x03;

    /**
     * handle刷新
     */
    private static final int MSG_REFRESH_DATA = 0x01;
    /**
     * 常用语组
     */
    private List<HDPhrase> groupList = new ArrayList<>();
    /*
     * 弱引用刷新UI
     */
    private WeakHandler mWeakHandler;

    private ProgressDialog pb;

    /**
     * 适配器
     */
    private PhraseAdapter mAdapter;

    private static class WeakHandler extends android.os.Handler {
        WeakReference<PhraseActivity> weakReference;

        public WeakHandler(PhraseActivity activity) {
            this.weakReference = new WeakReference<PhraseActivity>(activity);
        }

        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PhraseActivity activity = weakReference.get();
            if (null != activity) {
                switch (msg.what) {
                    case MSG_REFRESH_DATA:
                        activity.refreshView((List<HDPhrase>) msg.obj);
                        break;
                }
            }
        }
    }

    /**
     * 刷新当前View
     */
    private void refreshView(List<HDPhrase> data) {
        if (data != null) {
            //mAdapter.clear();
            //mAdapter.addAll(data);
            groupList.clear();
            groupList.addAll(data);
            mAdapter.createPhraseItemList(groupList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_phrase);
        initView();
        mWeakHandler = new WeakHandler(this);

        final List<HDPhrase> rootList = HDClient.getInstance().phraseManager().getAllPhrase();
        if (rootList != null && rootList.size() > 0) {
            Message message = mWeakHandler.obtainMessage();
            message.what = MSG_REFRESH_DATA;
            message.obj = rootList;
            mWeakHandler.sendMessage(message);
        } else {
            loadFirstData();
        }
    }

    private void initView() {
        /*
         * 列表View
         */
        EasyRecyclerView recyclerView = $(R.id.recyclerView);
        View searchLayout = $(R.id.rl_search);
        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhraseActivity.this, PhraseSearchActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SEARCH);
            }
        });
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(mAdapter = new PhraseAdapter(this));
        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                loadFirstData();
            }
        });
    }

    /**
     * 返回按钮点击事件
     */
    @OnClick({R.id.iv_back})
    public void back(View view) {
        finish();
    }

    /**
     * 第一次从服务器加载数据
     */
    private void loadFirstData() {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null) {
            return;
        }

        pb = new ProgressDialog(this);
        pb.setMessage("数据加载中...");
        pb.show();

        HDClient.getInstance().phraseManager().getAllPhraseFromServer(new HDDataCallBack<List<HDPhrase>>() {
            @Override public void onSuccess(List<HDPhrase> value) {
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = value;
                mWeakHandler.sendMessage(message);
            }

            @Override public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                mWeakHandler.sendMessage(message);
            }
        });
    }

    @OnClick(R.id.refreshPhrase)
    public void reloadData() {
        loadFirstData();
    }

    public void closeDialog() {
        if (pb != null && pb.isShowing()) {
            pb.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWeakHandler != null) {
            mWeakHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
        closeDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHILD) {
                setResult(RESULT_OK, data);
                finish();
            } else if (requestCode == REQUEST_CODE_SEARCH) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }
}
