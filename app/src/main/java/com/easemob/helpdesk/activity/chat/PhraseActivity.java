package com.easemob.helpdesk.activity.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.PhraseAdapter;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.HDPhrase;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 常用语界面
 *
 * @author liyuzhao
 */
public class PhraseActivity extends BaseActivity {

    private static final String TAG = PhraseActivity.class.getSimpleName();


    private static final int REQUEST_CODE_CHILD = 0x01;
    public static final int RESULT_CODE_CANCEL_CHILD = 0x02;

    /**
     * handle刷新
     */
    private static final int MSG_REFRESH_DATA = 0x01;
    /**
     * handle授权失败
     */
    private static final int MSG_AUTHENTICATION = 0x02;

    /*
     * 列表View
     */
    private EasyRecyclerView recyclerView;
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

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PhraseActivity activity = weakReference.get();
            if (null != activity) {
                switch (msg.what) {
                    case MSG_REFRESH_DATA:
                        activity.refreshView((List<HDPhrase>) msg.obj);
                        break;
                    case MSG_AUTHENTICATION:
                        HDApplication.getInstance().logout();
                        break;
                    default:
                        break;
                }
            }


        }
    }


    /**
     * 刷新当前View
     * @param data
     */
    private void refreshView(List<HDPhrase> data) {
        if (data != null) {
            mAdapter.clear();
            mAdapter.addAll(data);
            groupList.clear();
            groupList.addAll(data);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_phrase);
        ButterKnife.bind(this);
        initView();
        mWeakHandler = new WeakHandler(this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(mAdapter = new PhraseAdapter(this));
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                HDPhrase entity = mAdapter.getItem(i);
                if (entity.leaf){
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(PhraseActivity.this, PhraseItemActivity.class);
                intent.putExtra("manager",TextUtils.isEmpty(entity.agentUserId));
                intent.putExtra("parentId", entity.id);
                startActivityForResult(intent, REQUEST_CODE_CHILD);
            }
        });
        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFirstData();
            }
        });

        mAdapter.addAll(groupList);

        final List<HDPhrase> rootList = HDClient.getInstance().phraseManager().getAllPhrase();
        if(rootList != null && rootList.size() > 0){
            Message message = mWeakHandler.obtainMessage();
            message.what = MSG_REFRESH_DATA;
            message.obj = rootList;
            mWeakHandler.sendMessage(message);
        }else{
            loadFirstData();
        }

    }


    private void initView() {
        recyclerView = $(R.id.recyclerView);
    }

    /**
     * 返回按钮点击事件
     * @param view
     */
    @OnClick({R.id.rl_back})
    public void back(View view) {
        finish();
    }

    /**
     * 第一次从服务器加载数据
     */
    private void loadFirstData() {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null){
            return;
        }

        pb = new ProgressDialog(this);
        pb.setMessage("数据加载中...");
        pb.show();

        HDClient.getInstance().phraseManager().getAllPhraseFromServer(new HDDataCallBack<List<HDPhrase>>() {
            @Override
            public void onSuccess(List<HDPhrase> value) {
                if (isFinishing()){
                    return;
                }
                closeDialog();
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                message.obj = value;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_REFRESH_DATA;
                mWeakHandler.sendMessage(message);
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                Message message = mWeakHandler.obtainMessage();
                message.what = MSG_AUTHENTICATION;
                mWeakHandler.sendMessage(message);
            }
        });

    }

    @OnClick(R.id.refreshPhrase)
    public void reloadData(){
        loadFirstData();
    }


    public void closeDialog(){
        if (pb != null && pb.isShowing()){
            pb.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        mWeakHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        closeDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_CHILD){
                setResult(RESULT_OK, data);
                finish();
            }
        }else if(resultCode == RESULT_CODE_CANCEL_CHILD){
//            if(requestCode == REQUEST_CODE_CHILD){
//                int position = data.getIntExtra("position", -1);
//                ArrayList<ShortCutEntity> entty = data.getParcelableArrayListExtra("item");
//                if(position == -1){
//                    return;
//                }
//                HDPhrase groupEntity = groupList.get(position);
//                groupEntity.shortCutEntitys = entty;
//                groupList.add(position, groupEntity);
//                mAdapter.notifyDataSetChanged();
//            }
        }



    }
}
