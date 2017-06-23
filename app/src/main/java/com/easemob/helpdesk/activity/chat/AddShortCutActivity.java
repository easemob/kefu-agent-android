package com.easemob.helpdesk.activity.chat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.bean.HDPhrase;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 常用语添加和修改界面
 *
 * @author liyuzhao
 */
public class AddShortCutActivity extends BaseActivity{

    private static final String TAG = AddShortCutActivity.class.getSimpleName();

    /**
     * 最大文字输入限制
     */
    private final static int MAX_TXT_COUNT = 400;

    /**
     * 标题栏
     */
    @BindView(R.id.txtTitle)
    protected TextView txtTitle;

    /**
     * 常用语输入框
     */
    @BindView(R.id.et_add)
    protected EditText edittext;

    /**
     * 返回按钮
     */
    @BindView(R.id.rl_back)
    protected View btnBack;

    /**
     * 保存按钮
     */
    @BindView(R.id.btn_sure)
    protected Button btnSure;

    /**
     * 保存中等待Dialog
     */
    private Dialog pd = null;

    /**
     * 当前常用语对象
     */
    private HDPhrase mEntty;

    /**
     * 显示剩余字段View
     */
    @BindView(R.id.txtCount)
    protected TextView txtCount;

    /**
     * 当前常用语的组ID
     */
    private long parentId = -1;

    /**
     * 当前常用语的ID
     */
    private long currentPhraseId = -1;
    private boolean leaf = true;

    /**
     * Perform initialization of all fragments and loaders.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_add_shortcut);
        ButterKnife.bind(this);
        initDatas();
    }

    public void initDatas(){
        Intent gIntent = getIntent();
        parentId = gIntent.getLongExtra("parentId", -1);
        currentPhraseId = gIntent.getLongExtra("phraseId", -1);
        leaf = gIntent.getBooleanExtra("leaf", true);
        mEntty = HDClient.getInstance().phraseManager().getPhraseById(currentPhraseId);
        //判断当前是添加界面和修改界面
        if (currentPhraseId > 0) {
            if (leaf) {
                txtTitle.setText("修改常用语");
            } else {
                txtTitle.setText("修改分类");
            }
            if (mEntty != null) {
                edittext.setText(mEntty.phrase);
            }
        } else {
            if (leaf) {
                txtTitle.setText(R.string.title_add_phrase);
            } else {
                txtTitle.setText(R.string.title_add_phrase_category);
            }
        }
        //更新字数view
        refreshTxtCount(edittext.getText().length());
        //显示软键盘
        showSoftkeyboard(edittext);
    }

    @OnTextChanged(value = R.id.et_add, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void textChangeByEditText(Editable s){
        refreshTxtCount(s.length());
    }

    /**
     * 更新当前还需要输入多少字
     */
    private void refreshTxtCount(int length) {
        txtCount.setText(String.valueOf(MAX_TXT_COUNT - length));
    }


    /**
     * 监测输入并更新服务器数据
     */
    private void sendRemoteShortCutRequest() {
        final String content = edittext.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_content_no_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPhraseId > 0){
            if(mEntty != null){
                mEntty.phrase = content;
                updateShortCutMsgForServer(mEntty);
            }
        }else{
            addShortCutMsgForServer(content);
        }

    }

    /**
     * 隐藏等待框Dialog
     */
    private void closeDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }


    /**
     * 修改服务器常用语数据
     *
     * @param mEntty
     */
    private void updateShortCutMsgForServer(final HDPhrase mEntty) {
        if (mEntty == null){
            return;
        }
        pd = DialogUtils.getLoadingDialog(this, R.string.pd_updating);
        pd.show();

        HDClient.getInstance().phraseManager().updateShortCutMsgForServer(mEntty, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        setResult(RESULT_OK);
                        AddShortCutActivity.this.finish();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(AddShortCutActivity.this, R.string.error_updateFail, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }

    /**
     * 添加常用语到服务器
     *
     * @param content
     */
    private void addShortCutMsgForServer(String content) {
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null){
            return;
        }
        pd = DialogUtils.getLoadingDialog(this, R.string.pd_adding);
        pd.show();

        HDClient.getInstance().phraseManager().addShortCutMsgForServer(content, parentId, leaf, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        setResult(RESULT_OK);
                        AddShortCutActivity.this.finish();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "error:" + error + ";errorMsg:" + errorMsg);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getApplicationContext(), R.string.error_requestFail, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }


    @OnClick(R.id.rl_back) //返回按钮
    public void onClickByBack(){
        finish();
    }

    @OnClick(R.id.btn_sure) //确认按钮
    public void onClickBySure(){
        sendRemoteShortCutRequest();
    }


}
