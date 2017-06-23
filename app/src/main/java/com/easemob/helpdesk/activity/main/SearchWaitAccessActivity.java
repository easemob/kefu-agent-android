package com.easemob.helpdesk.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.transfer.TransferActivity;
import com.easemob.helpdesk.adapter.WaitAccessAdapter;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.gsonmodel.main.WaitQueueResponse;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.easemob.helpdesk.fragment.main.WaitAccessFragment.REQUEST_CODE_ALERT_DIALOG_ACCESS;
import static com.easemob.helpdesk.fragment.main.WaitAccessFragment.REQUEST_CODE_ALERT_DIALOG_CLOSE;
import static com.easemob.helpdesk.fragment.main.WaitAccessFragment.REQUEST_CODE_ALERT_DIALOG_TRANSFER;

/**
 * 待接入的搜索界面
 * <p/>
 * Created by liyuzhao on 16/3/15.
 */
public class SearchWaitAccessActivity extends BaseActivity {

    private static final String TAG = SearchWaitAccessActivity.class.getSimpleName();
    /**
     * 进入提示框的REQUEST_CODE
     */
    private static final int REQUEST_CODE_ALERT_DIALOG = 0x01;

    /**
     * 列表View（此处为EasyRecyclerView）
     */
    private EasyRecyclerView recyclerView;
    /**
     * 搜索清空，结束按钮
     */
    private ImageButton btnClear;
    /**
     * 搜索的文本框
     */
    private EditText query;
    /**
     * 列表填充的具体数据
     */
    private List<WaitQueueResponse.ItemsBean> mList = new ArrayList<WaitQueueResponse.ItemsBean>();
    /**
     * 填充数据的适配器
     */
    private WaitAccessAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_history_session);
        initView();
        List<WaitQueueResponse.ItemsBean> list = (List<WaitQueueResponse.ItemsBean>) getIntent().getSerializableExtra("list");
        if (list != null && list.size() > 0) {
            mList.addAll(list);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(mAdapter = new WaitAccessAdapter(this));
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                Intent intent = new Intent(SearchWaitAccessActivity.this, AlertDialog.class);
//                intent.putExtra("position", position);
//                intent.putExtra("msg", "您确认要接待此会话么？");
//                intent.putExtra("okString", "确认");
//                startActivityForResult(intent, REQUEST_CODE_ALERT_DIALOG);

            }
        });

        mAdapter.addAll(mList);
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mAdapter.clear();
                mAdapter.addAll(filter(mList, editable.toString()));
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query.getText().clear();
                finish();
            }
        });
    }

    public void clickAccess(int position, int index){
        if (index == 1){
            Intent intent = new Intent(this, AlertDialog.class);
            intent.putExtra("position", position);
            intent.putExtra("msg", "您确认要接待此会话吗？");
            intent.putExtra("okString", "确认");
            startActivityForResult(intent, REQUEST_CODE_ALERT_DIALOG_ACCESS);
        }else if (index == 2){
            startActivityForResult(new Intent(this, TransferActivity.class).putExtra("position", position), REQUEST_CODE_ALERT_DIALOG_TRANSFER);
        }else if (index == 3){
            //waitAbort
            Intent intent = new Intent(this, AlertDialog.class);
            intent.putExtra("position", position);
            intent.putExtra("msg", "您确认要关闭此会话吗？");
            intent.putExtra("okString", "确认");
            startActivityForResult(intent, REQUEST_CODE_ALERT_DIALOG_CLOSE);
        }

    }



    /**
     * 页面从栈中返回到第一个时，触发的回调
     *
     * @param requestCode 请求码
     * @param resultCode  结束码
     * @param data        Intent数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == REQUEST_CODE_ALERT_DIALOG_ACCESS){
                final int position = data.getIntExtra("position", -1);
                if (position == -1){
                    return;
                }
                WaitQueueResponse.ItemsBean entity = mAdapter.getItem(position);
                setResult(RESULT_OK, new Intent().putExtra("entty", entity).putExtra("index", 1));
                finish();
            }else if(requestCode == REQUEST_CODE_ALERT_DIALOG_TRANSFER){
                final int position = data.getIntExtra("position", -1);
                String userId = data.getStringExtra("userId");
                long queueId = data.getLongExtra("queueId", 0);
                if (position == -1){
                    return;
                }
                WaitQueueResponse.ItemsBean entity = mAdapter.getItem(position);
                setResult(RESULT_OK, new Intent().putExtra("entty", entity).putExtra("index", 2).putExtra("userId", userId).putExtra("queueId",queueId));
                finish();
            }else if(requestCode == REQUEST_CODE_ALERT_DIALOG_CLOSE){
                final int position = data.getIntExtra("position", -1);
                if (position == -1){
                    return;
                }
                WaitQueueResponse.ItemsBean entity = mAdapter.getItem(position);
                setResult(RESULT_OK, new Intent().putExtra("entty", entity).putExtra("index", 3));
                finish();
            }
//            if (requestCode == REQUEST_CODE_ALERT_DIALOG) {
//                final int position = data.getIntExtra("position", -1);
//                if (position == -1) {
//                    return;
//                }
//
//            }
        }
    }

    /**
     * 初始化View控件
     */
    private void initView() {
        recyclerView = $(R.id.recyclerView);
        btnClear = $(R.id.search_clear);
        query = $(R.id.query);
        query.setHint("搜索");
    }

    /**
     * 数据的筛选方法：
     * 根据输入的文本内容模糊筛选
     *
     * @param models 当前所有数据
     * @param query  输入的文本内容
     * @return 筛选后的符合条件的数据
     */
    private List<WaitQueueResponse.ItemsBean> filter(List<WaitQueueResponse.ItemsBean> models, String query) {
        query = query.toLowerCase();
        final List<WaitQueueResponse.ItemsBean> filteredModelList = new ArrayList<WaitQueueResponse.ItemsBean>();
        for (WaitQueueResponse.ItemsBean model : models) {
            final String text = model.getUserName();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

}
