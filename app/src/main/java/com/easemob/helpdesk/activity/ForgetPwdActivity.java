package com.easemob.helpdesk.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
public class ForgetPwdActivity extends BaseActivity {

    @BindView(R.id.et_email)
    public EditText etEmail;

    @BindView(R.id.et_code)
    public EditText etCode;

    @BindView(R.id.iv_code)
    public ImageView ivCode;
    private ProgressDialog pd;
    private String codeId;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_forget_pwd);
        unbinder = ButterKnife.bind(this);
        loadVerifyCode();
    }

    private void loadVerifyCode(){
        showDialog("验证码获取中...");
        HDClient.getInstance().accountManager().postImgVerifyCode(new HDDataCallBack<Bitmap>() {
            @Override
            public void onSuccess(final Bitmap value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        if (value != null) {
                            ivCode.setImageBitmap(value);
                            etCode.setText("");
                            codeId = HDClient.getInstance().accountManager().getLastCodeId();
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
                        Toast.makeText(ForgetPwdActivity.this, "加载验证码失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {

            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideDialog();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    @OnClick(R.id.rl_back)
    public void clickByBack(View view){
        finish();
    }

    @OnClick(R.id.btn_send)
    public void clickBySend(View view){
        String strEmail = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(strEmail)){
            Toast.makeText(this, "账号不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        String strCode = etCode.getText().toString().trim();
        if (TextUtils.isEmpty(strCode)){
            Toast.makeText(this, "验证码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        showDialog();

        HDClient.getInstance().accountManager().forgotPwd(strEmail, codeId, strCode, new HDDataCallBack<String>() {
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
                            if (jsonResult.has("error")) {
                                String error = jsonResult.getString("error");
                                Toast.makeText(ForgetPwdActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                                loadVerifyCode();
                                return;
                            }
                            if (jsonResult.has("success")) {
                                Toast.makeText(ForgetPwdActivity.this.getApplicationContext(), "发送成功,请去邮件获取验证码!", Toast.LENGTH_LONG).show();
                                toResetPwd();
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
                        Toast.makeText(ForgetPwdActivity.this, "请求失败,请检查网络!", Toast.LENGTH_SHORT).show();
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



    @OnClick(R.id.iv_code)
    public void onClickIvCode(View view) {
        loadVerifyCode();
    }


    private void toResetPwd(){
        Intent intent = new Intent(this, ResetPwdActivity.class);
        startActivity(intent);
        finish();
    }

    public void showDialog(String message){
        if (pd == null){
            pd = new ProgressDialog(this);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage(message);
        pd.show();
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
