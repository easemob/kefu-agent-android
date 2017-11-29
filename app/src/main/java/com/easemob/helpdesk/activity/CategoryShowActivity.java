package com.easemob.helpdesk.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDCategorySummary;
import com.hyphenate.kefusdk.manager.main.CategorySummaryManager;
import com.zdxd.tagview.OnTagDeleteListener;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CategoryShowActivity extends BaseActivity {

    private static final int REQUEST_CODE_ADD_CATEGORY = 0x01;

    @BindView(R.id.right)
    protected TextView saveBtn;

    @BindView(R.id.et_comment)
    protected EditText etComment;

    @BindView(R.id.tag_group)
    protected TagView tagGroup;

    private ProgressDialog pd;
    private boolean isClose;
    private int position;

    private CategorySummaryManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_category_show);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String sessionId = intent.getStringExtra("sessionId");
        String value = intent.getStringExtra("summarys");
        isClose = intent.getBooleanExtra("close", false);
        position = intent.getIntExtra("position", -1);
        manager = new CategorySummaryManager(sessionId);
        initView();
        loadData(value);
        loadComment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
        hideKeyboard();
    }

    private void loadComment() {
        manager.asyncGetComment(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String comment) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etComment.setText(comment);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private void setTagViews(List<HDCategorySummary> list){

        Tag tag;
        if(list == null || list.size() == 0){
            tagGroup.addTags(new java.util.ArrayList<Tag>());
            return;
        }
        ArrayList<Tag> tags = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            HDCategorySummary entty = list.get(i);
            String rootName = (TextUtils.isEmpty(entty.rootName)) ? "" : entty.rootName + ">";
            tag = new Tag(rootName + entty.name);
            tag.id = entty.id;
            tag.radius = 10f;
            int color = (int)entty.color;
            String strColor;
            if(color == 0){
                strColor = "#000000";
            }else if(color == 255){
                strColor = "#ffffff";
            }else{
                strColor = "#" + Integer.toHexString(color);
                strColor = strColor.substring(0, 7);
            }
            tag.layoutColor = Color.parseColor(strColor);
            tag.isDeletable = true;
            tags.add(tag);

        }
        tagGroup.addTags(tags);
    }

    private void setTagView(HDCategorySummary entty){
        if(entty == null){
            return;
        }
        String rootName = (TextUtils.isEmpty(entty.rootName)) ? "" : entty.rootName + ">";
       Tag tag = new Tag(rootName + entty.name);
        tag.id = entty.id;
        tag.radius = 10f;
        int color = (int)entty.color;
        String strColor;
        if(color == 0){
            strColor = "#000000";
        }else if(color == 255){
            strColor = "#ffffff";
        }else{
            strColor = "#" + Integer.toHexString(color);
            strColor = strColor.substring(0, 7);
        }
        tag.layoutColor = Color.parseColor(strColor);
        tag.isDeletable = true;
        tagGroup.addTag(tag);
    }

    private void initView() {
        tagGroup.setOnTagDeleteListener(new OnTagDeleteListener() {
            @Override
            public void onTagDeleted(TagView view, Tag tag, int position) {
                view.remove(position);
                manager.removeAtPosition(position);
            }
        });
        if(isClose){
            saveBtn.setText("保存并关闭");
        }else{
            saveBtn.setText("保存");
        }
        saveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (manager.getUnsavedCategorySummaryIds().isEmpty()) {
                    Toast.makeText(CategoryShowActivity.this, "标签为空不能保存！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pd == null){
                    pd = new ProgressDialog(CategoryShowActivity.this);
                    pd.setCanceledOnTouchOutside(false);
                }
                pd.setMessage("正在保存...");
                pd.show();
                manager.asyncUpdateCategorySummarys(new HDDataCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                Toast.makeText(CategoryShowActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                                if (isClose) {
                                    setResult(RESULT_OK, new Intent().putExtra("close", true).putExtra("position", position));
                                    finish();
                                }

                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                Toast.makeText(CategoryShowActivity.this, "保存失败！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationException() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                            }
                        });

                    }
                });
                String tempString = etComment.getText().toString();
                manager.asyncUpdateComment(tempString, new HDDataCallBack<String>() {
                    @Override
                    public void onSuccess(final String value) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etComment.setText(value);
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {

                    }
                });

            }
        });
    }


    private void loadData(String value){
        if (!TextUtils.isEmpty(value)) {
            List<HDCategorySummary> list = manager.getCategorySummarysFromString(value);
            setTagViews(list);
        } else {
            manager.asyncGetSessionSummary(new HDDataCallBack<List<HDCategorySummary>>() {
                @Override
                public void onSuccess(final List<HDCategorySummary> value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTagViews(value);
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {

                }
            });
        }
    }

    public void back(View view){
        hideKeyboard();
        setResult(RESULT_OK, new Intent().putExtra("value", manager.getCategorySummaryIds().toString()).putExtra("comment", manager.getComment()));
        finish();
    }

    @Override
    public void onBackPressed() {
        back(null);
        super.onBackPressed();
    }

    public void add(View view){
        startActivityForResult(new Intent(this, SelectCategoryTreeActivity.class)
                .putExtra("sessionId", manager.getSessionId())
                .putExtra("ids", manager.getUnsavedCategorySummaryIds().toString())
                , REQUEST_CODE_ADD_CATEGORY);
        overridePendingTransition(R.anim.activity_open, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_ADD_CATEGORY){
                if(data == null){
                    return;
                }
                HDCategorySummary entty = (HDCategorySummary) data.getSerializableExtra("tree");
                manager.addCategorySummary(entty);
                setTagView(entty);
            }
        }
    }


    private void closeDialog(){
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }

}
