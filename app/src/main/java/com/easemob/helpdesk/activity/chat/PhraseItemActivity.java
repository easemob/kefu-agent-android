package com.easemob.helpdesk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.PhraseItemAdapter;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.bean.HDPhrase;
import com.hyphenate.kefusdk.chat.HDClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by liyuzhao on 16/3/16.
 */
public class PhraseItemActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = PhraseItemActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ADD_SHORTCUT = 0x01;
    public static final int REQUEST_CODE_UPDATE_SHORTCUT = 0x02;
    public static final int REQUEST_CODE_ADD_PHRASE_FOLDER = 0x03;
    private RecyclerView recyclerView;
    private EditText query;
    private ImageButton ibClear;

    private List<HDPhrase> mList = Collections.synchronizedList(new ArrayList<HDPhrase>());

    private List<Long> mParentIds = Collections.synchronizedList(new ArrayList<Long>());
    private PhraseItemAdapter mAdapter;
    private TextView tvTitle;
    private TextView tvEdit;
    private ImageView ivBack;
    private LinearLayout addLayout;
    private long parentId = -1;
    private boolean isEditMod;
    private boolean isManager = false;

    @BindView(R.id.addPhraseGroup)
    protected FloatingActionButton addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_phrase_item);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        parentId = intent.getLongExtra("parentId", -1);
        isManager = intent.getBooleanExtra("manager", false);
        initView();
        initListener();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

//        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapter(mAdapter = new PhraseItemAdapter(this, mList));
//        recyclerView.setItemAnimator(new MyItemAnimator());
        List<HDPhrase> list = HDClient.getInstance().phraseManager().getPhrasesByParentId(parentId);
        if (list != null){
            mList.addAll(list);
            notifyBtnCategory();
        }
        mAdapter.refresh();
    }

    private void initView(){
        ivBack = $(R.id.iv_back);
        tvEdit = $(R.id.tv_edit);
        tvTitle = $(R.id.tv_title);
        recyclerView = $(R.id.recyclerView);
        tvEdit.setText("编辑");
        addLayout = $(R.id.add_layout);
        addLayout.setVisibility(View.GONE);
        query = $(R.id.query);
        query.setHint("搜索");
        ibClear = $(R.id.search_clear);
        if (!isManager){
            if(parentId > 0){
                tvEdit.setVisibility(View.VISIBLE);
            }else{
                tvEdit.setVisibility(View.GONE);
            }
        }else {
            tvEdit.setVisibility(View.GONE);
        }
        addButton.setVisibility(View.GONE);
    }

    public void setOnItemClick(View view, int position) {
        HDPhrase item = mAdapter.getItem(position);
        if (item != null) {
            mParentIds.add(parentId);
            parentId = item.id;
            List<HDPhrase> list = HDClient.getInstance().phraseManager().getPhrasesByParentId(parentId);
            mList.clear();
            mList.addAll(list);
            notifyBtnCategory();
            mAdapter.refresh();
        }
    }



    private void initListener(){
        ivBack.setOnClickListener(this);
        tvEdit.setOnClickListener(this);
        addLayout.setOnClickListener(this);
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.getFilter().filter(s);
                if (s.length() > 0) {
                    ibClear.setVisibility(View.VISIBLE);
                } else {
                    ibClear.setVisibility(View.INVISIBLE);
                }
            }
        });
        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideKeyboard();
            }
        });


    }

    public void notifyEditText(){
        if (!isManager){
            if (isEditMod){
                tvEdit.setText("完成");
            }else{
                tvEdit.setText("编辑");
            }
            if (isEditMod && parentId > 0){
                addLayout.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
            }else{
                addLayout.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);
            }
        }else{
            tvEdit.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
        }
    }


    public void queryClear(){
        query.getText().clear();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            btnBack();
//            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void notifyBtnCategory(){
        if (!isManager && mParentIds.size() > 0 && isEditMod){
            addButton.setVisibility(View.VISIBLE);
        }else{
            addButton.setVisibility(View.GONE);
        }
    }

    public void btnBack(){
        if (mParentIds.size() > 0){
            parentId = mParentIds.remove(mParentIds.size() - 1);
            List<HDPhrase> list = HDClient.getInstance().phraseManager().getPhrasesByParentId(parentId);
            mList.clear();
            mList.addAll(list);
            notifyBtnCategory();
            mAdapter.refresh();
        }else{
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                btnBack();
                break;
            case R.id.tv_edit:
                isEditMod = !isEditMod;
                mAdapter.setEditable(isEditMod);
                notifyEditText();
                mAdapter.refresh();
                break;
            case R.id.add_layout:
                Intent intent = new Intent();
                intent.setClass(this, AddShortCutActivity.class);
                intent.putExtra("parentId", parentId);
                startActivityForResult(intent, REQUEST_CODE_ADD_SHORTCUT);
                break;
        }
    }

    @OnClick(R.id.addPhraseGroup)
    public void addPhraseGroup(View view){
        Intent intent = new Intent();
        intent.setClass(this, AddShortCutActivity.class);
        intent.putExtra("parentId", parentId);
        intent.putExtra("leaf", false);
        startActivityForResult(intent, REQUEST_CODE_ADD_PHRASE_FOLDER);
    }


    public void reloadFromDB(){

        List<HDPhrase> list = HDClient.getInstance().phraseManager().getPhrasesByParentId(parentId);
        mList.clear();
        mList.addAll(list);
        mAdapter.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADD_SHORTCUT || requestCode == REQUEST_CODE_UPDATE_SHORTCUT ||requestCode == REQUEST_CODE_ADD_PHRASE_FOLDER) {
                if (isEditMod) {
                    isEditMod = false;
                    notifyEditText();
                    mAdapter.setEditable(isEditMod);
                }
                reloadFromDB();

            }
        }
    }
}
