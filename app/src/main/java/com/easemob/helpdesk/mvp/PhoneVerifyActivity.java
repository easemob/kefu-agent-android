package com.easemob.helpdesk.mvp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/8/25.
 */
public class PhoneVerifyActivity extends BaseActivity {

    @BindView(R.id.et_code)
    public EditText etCode;
    @BindView(R.id.et_phone)
    public EditText etPhone;
    @BindView(R.id.submit)
    public Button btnSubmit;

    private String strPhone;
    private Bundle bundle;
    private ProgressDialog pd;
    private Dialog successDialog;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_phone_verify);
        unbinder = ButterKnife.bind(this);
        Intent intent = getIntent();
        strPhone = intent.getStringExtra("code");
        bundle = intent.getBundleExtra("body");
        etPhone.setText(strPhone);


    }


    @OnClick(R.id.rl_back)
    public void onClickBack(View view){
        finish();
    }


    @OnClick(R.id.submit)
    public void onClickSubmit(View view){
        String strCode = etCode.getText().toString().trim();
        if (TextUtils.isEmpty(strCode)){
            Toast.makeText(this, "手机验证码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> postBody = new HashMap<>();
        if (bundle.getString("company", null) != null){
            postBody.put("company", bundle.getString("company"));
        }else{
            postBody.put("company", "个人");
        }
        postBody.put("phone", bundle.getString("phone"));
        postBody.put("username", bundle.getString("username"));
        postBody.put("password", bundle.getString("password"));
        postBody.put("confirmPsw", bundle.getString("confirmPsw"));
        postBody.put("codeId", bundle.getString("codeId"));
        postBody.put("codeValue", bundle.getString("codeValue"));
        postBody.put("verifyCode", strCode);

        showDialog();
        HDClient.getInstance().accountManager().postRegister(postBody, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        try{
                            JSONObject jsonObject = new JSONObject(value);
                            if (jsonObject.has("error")){
                                String error = jsonObject.getString("error");
                                Toast.makeText(getApplicationContext(), "" + error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (jsonObject.has("success")){
                                String success = jsonObject.getString("success");
                                Toast.makeText(getApplicationContext(), "" + success, Toast.LENGTH_SHORT).show();
                                showSuccessDialog();
                            }

                        }catch (Exception e){
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
                        Toast.makeText(getApplication(), "注册失败!", Toast.LENGTH_SHORT).show();
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


    public void showDialog(){
        if (pd == null){
            pd = new ProgressDialog(this);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("注册中...");
        }
        pd.show();
    }

    public void hideDialog(){
        if (pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }

    public void showSuccessDialog(){
        successDialog = new Dialog(this, R.style.MyDialogStyle);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dialog_login_success, null);
        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                successDialog.dismiss();
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        successDialog.setContentView(view);
        successDialog.setCanceledOnTouchOutside(true);
        successDialog.setCancelable(false);
        successDialog.show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null){
            unbinder.unbind();
        }
        hideDialog();
        if (successDialog != null && successDialog.isShowing()){
            successDialog.dismiss();
            successDialog = null;
        }
    }
}
