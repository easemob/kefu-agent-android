package com.easemob.helpdesk.mvp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/8/24.
 */
public class RegisterActivity  extends BaseActivity {

    private static final int REQUEST_CODE_PHONE_VERIFY = 0x01;

    @BindView(R.id.title)
    public TextView txtTitle;

    @BindView(R.id.tv_personal)
    public TextView tvPersonal;


    @BindView(R.id.et_email)
    public EditText etEmail;
    @BindView(R.id.et_password)
    public EditText etPassword;
    @BindView(R.id.et_confirm_pwd)
    public EditText etConfirmPwd;
    @BindView(R.id.et_phone)
    public EditText etPhoneNumber;

    @BindView(R.id.et_company)
    public EditText etCompanyName;

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
        setContentView(R.layout.activity_register);
        unbinder = ButterKnife.bind(this);
        //默认英文
        etEmail.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
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
                        Toast.makeText(RegisterActivity.this, "加载验证码失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {

            }
        });



    }



    @OnClick(R.id.submit)
    public void onClickSubmit(View view){
        final String strEmail = etEmail.getText().toString().trim();
        if(TextUtils.isEmpty(strEmail)){
            Toast.makeText(this, "邮箱不能为空!", Toast.LENGTH_SHORT).show();
            return ;
        }
        if(!strEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")){
            Toast.makeText(this, "邮箱格式不正确!", Toast.LENGTH_SHORT).show();
            return ;
        }
        final String strPwd = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(strPwd)){
            Toast.makeText(this, "密码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!strPwd.matches("^[a-zA-Z0-9_,\\.;\\:\"'!*&]{6,22}$")){
            Toast.makeText(this, "密码有效长度6~22位!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!strPwd.matches("^(?![A-Z]+$)(?![a-z]+$)(?!\\d+$)(?![\\W_]+$)\\S+$")){
            Toast.makeText(this, "密码至少包含大写字母,小写字母,数字,符号中两种!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String strConPwd = etConfirmPwd.getText().toString().trim();
        if (TextUtils.isEmpty(strConPwd)){
            Toast.makeText(this, "确认密码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!strPwd.equals(strConPwd)){
            Toast.makeText(this, "两次输入的密码不一致!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String strCompanyName = etCompanyName.getText().toString().trim();
        if (TextUtils.isEmpty(strCompanyName)){
            Toast.makeText(this, "公司名称不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String strPhoneNum = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(strPhoneNum)){
            Toast.makeText(this, "手机号不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!strPhoneNum.matches("^1[3|4|5|7|8]\\d{9}$")){
            Toast.makeText(this, "手机号格式不正确!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String strCode = etCode.getText().toString().trim();
        if (TextUtils.isEmpty(strCode)){
            Toast.makeText(this, "验证码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> postBody = new HashMap<>();
        postBody.put("company", strCompanyName);
        postBody.put("phone", strPhoneNum);
        postBody.put("username", strEmail);
        postBody.put("password", strPwd);
        postBody.put("confirmPsw", strConPwd);
        postBody.put("codeId", codeId);
        postBody.put("codeValue", strCode);

        showDialog("提交中...");
        HDClient.getInstance().accountManager().postSendSmsVerifyCode(postBody, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(value);
                            if (jsonObject.has("error")) {
                                String error = jsonObject.getString("error");
                                Toast.makeText(RegisterActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                                loadVerifyCode();
                                return;
                            }
                            if (jsonObject.has("success")) {
                                String rCodeId = jsonObject.getString("codeId");
//                                String success = jsonObject.getString("success");
                                Intent intent = new Intent();
                                intent.setClass(RegisterActivity.this, PhoneVerifyActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("company", strCompanyName);
                                bundle.putString("phone", strPhoneNum);
                                bundle.putString("username", strEmail);
                                bundle.putString("password", strPwd);
                                bundle.putString("confirmPsw", strConPwd);
                                bundle.putString("codeId", codeId);
                                bundle.putString("codeValue", strCode);
                                intent.putExtra("body", bundle);
                                intent.putExtra("code", rCodeId);
                                startActivityForResult(intent, REQUEST_CODE_PHONE_VERIFY);
                            }
                          } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        Toast.makeText(RegisterActivity.this, "提交失败,请检查网络!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                    }
                });
            }
        });

    }

    public void showDialog(String message){
        if (pd == null){
            pd = new ProgressDialog(this);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage(message);
        pd.show();
    }

    public void hideDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }


    @OnClick(R.id.iv_code)
    public void onClickIvCode(View view) {
        loadVerifyCode();
    }

    @OnClick(R.id.rl_back)
    public void clickByBack(View view) {
        finish();
    }


    @OnClick(R.id.tv_personal)
    public void onClickByPersonal(View view){
        Intent intent = new Intent();
        intent.setClass(this, RegisterPersonalActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null){
            unbinder.unbind();
        }
        hideDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_CODE_PHONE_VERIFY){
                finish();
            }
        }

    }
}
