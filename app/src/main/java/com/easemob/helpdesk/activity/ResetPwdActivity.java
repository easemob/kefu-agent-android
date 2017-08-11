package com.easemob.helpdesk.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/9/2.
 */
public class ResetPwdActivity extends BaseActivity {

    @BindView(R.id.et_pwd)
    public EditText etPwd;
    @BindView(R.id.et_confirm_pwd)
    public EditText etConfirmPwd;
    @BindView(R.id.et_code)
    public EditText etEmailCode;
    @BindView(R.id.btn_send)
    public Button btnSend;
    private ProgressDialog pd;


    private Unbinder unbinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_reset_pwd);
        unbinder = ButterKnife.bind(this);

        etPwd.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        etPwd.setLongClickable(false);

        etConfirmPwd.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        etConfirmPwd.setLongClickable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideDialog();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    @OnClick(R.id.btn_send)
    public void clickBySend(View view){
        String strPwd = etPwd.getText().toString().trim();
        if (TextUtils.isEmpty(strPwd)){
            Toast.makeText(this, "密码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        String strConfirmPwd = etConfirmPwd.getText().toString().trim();
        if (TextUtils.isEmpty(strConfirmPwd)){
            Toast.makeText(this, "确认密码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        String strEmailCode = etEmailCode.getText().toString().trim();
        if (TextUtils.isEmpty(strEmailCode)){
            Toast.makeText(this, "邮件验证码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        //有效长度6~22位
        //匹配Email地址的正则表达式：w+([-+.]w+)*@w+([-.]w+)*.w+([-.]w+)*
        //匹配网址URL的正则表达式：[a-zA-z]+://[^s]*
        //匹配帐号是否合法(字母开头，允许5-16字节，允许字母数字下划线)：^[a-zA-Z][a-zA-Z0-9_]{4,15}$
        if (!strPwd.matches("^[a-zA-Z0-9_,\\.;\\:\"'!*&]{6,22}$")){
            Toast.makeText(this, "有效长度6~22位!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!strPwd.matches("^(?![A-Z]+$)(?![a-z]+$)(?!\\d+$)(?![\\W_]+$)\\S+$")){
            Toast.makeText(this, "密码至少包含大写字母,小写字母,数字,符号中两种!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!strPwd.equals(strConfirmPwd)){
            Toast.makeText(this, "两次密码不一致!", Toast.LENGTH_SHORT).show();
            return;
        }

        showDialog();

        HDClient.getInstance().userControler().recoveryPassword(strPwd, strConfirmPwd, strEmailCode, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        try {
                            JSONObject jsonResult = new JSONObject(value);
                            if(jsonResult.has("error")){
                                String error = jsonResult.getString("error");
                                Toast.makeText(ResetPwdActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (jsonResult.has("success")){
                                Toast.makeText(ResetPwdActivity.this, "密码重置成功!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                        hideDialog();
                        Toast.makeText(ResetPwdActivity.this, "请求失败,请检查网络!", Toast.LENGTH_SHORT).show();
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
                        hideDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });

    }

    @OnClick(R.id.rl_back)
    public void onClickByBack(View view){
        finish();
    }


    public void showDialog(){
        if (pd == null){
            pd = new ProgressDialog(this);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage("发送中...");
        pd.show();
    }

    public void hideDialog(){
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }




}
