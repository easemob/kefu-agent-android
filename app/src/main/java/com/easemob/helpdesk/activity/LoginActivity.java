package com.easemob.helpdesk.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.EMToast;
import com.easemob.helpdesk.widget.CircleImageView;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;


/**
 * 登录界面
 * Created by liyuzhao on 16/8/24.
 */
public class LoginActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private static final int REQUEST_CODE_REQUEST_PERMISSIONS = 1;

    /**
     * 版本号TextView
     */
    @BindView(R.id.tv_version)
    public TextView tvVersionName;

    /**
     * 账号文本框
     */
    @BindView(R.id.etAccount)
    public EditText etAccount;

    /**
     * 密码框
     */
    @BindView(R.id.etPwd)
    public EditText etPwd;


    /**
     * 加载提示Dialog
     */
    public Dialog pd = null;
    /**
     * 账号 密码暂存变量
     */
    private String strName, strPwd;
    /**
     * 账号清空的ImageView
     */
    @BindView(R.id.ivAccountClear)
    public ImageView ivAccountClear;

    /**
     * 密码清空的ImageView
     */
    @BindView(R.id.ivPwdClear)
    public ImageView ivPwdClear;

    /**
     * 密码显示和隐藏切换CheckBox
     */
    @BindView(R.id.cb_input_hide)
    public CheckBox cbInputHidden;

    /**
     * 在线或隐身登录的CheckBox
     */
    @BindView(R.id.cb_hidden_login)
    public CheckBox cbHiddenLogin;

    /**
     * logo显示ImageView
     */
    @BindView(R.id.logo_imageview)
    public CircleImageView logoImageView;

    @BindView(R.id.tv_register)
    public TextView tvRegister;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder = ButterKnife.bind(this);
        tvRegister.setVisibility(View.VISIBLE);
        //检测是否登录过此应用
        if(HDClient.getInstance().isLoggedInBefore()){
            toMainActivity();
            return;
        }
        initView();
    }

    private void initView(){
        logoImageView.setImageResource(R.drawable.logo_300);
        cbInputHidden.setOnCheckedChangeListener(this);
        etAccount.addTextChangedListener(new AccountTextWatch());
        //默认英文
        etAccount.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        etPwd.addTextChangedListener(new PwdTextWatch());

        //设置版本号
        String versionName = CommonUtils.getAppVersionNameFromApp(this);
        if (!TextUtils.isEmpty(versionName)) {
            tvVersionName.setText("v" + versionName);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_input_hide:
                if (isChecked) {
                    etPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                break;

            default:
                break;
        }
    }

    /**
     * 监控账号输入
     */
    class AccountTextWatch implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                ivAccountClear.setVisibility(View.VISIBLE);
            } else {
                ivAccountClear.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    /**
     * 监控密码输入
     */
    class PwdTextWatch implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (s.length() > 0) {
                ivPwdClear.setVisibility(View.VISIBLE);
                cbInputHidden.setVisibility(View.VISIBLE);
            } else {
                ivPwdClear.setVisibility(View.INVISIBLE);
                cbInputHidden.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                ivPwdClear.setVisibility(View.VISIBLE);
                cbInputHidden.setVisibility(View.VISIBLE);
            } else {
                ivPwdClear.setVisibility(View.INVISIBLE);
                cbInputHidden.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }



    @OnClick({R.id.btnLogin, R.id.ivAccountClear, R.id.ivPwdClear})
    public void clickMethod(View v){
        switch (v.getId()) {
            case R.id.btnLogin://点击按钮
                int hasWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWritePermission != PackageManager.PERMISSION_GRANTED){
                    PermissionGen.with(this)
                            .addRequestCode(REQUEST_CODE_REQUEST_PERMISSIONS)
                            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ).request();
                } else {
                    login();
                }
                break;
            case R.id.ivAccountClear: //账号清除按钮
                etAccount.getText().clear();
                break;
            case R.id.ivPwdClear://密码清除按钮
                etPwd.getText().clear();
                break;
        }
    }

    private void login(){
        if (!checkInputVaid()) {
            return;
        }
        showLoading();
        final String uName = getUsername();
        final String uPwd = getPassword();
        HDClient.getInstance().login(uName, uPwd, isHiddenLogin(), new HDDataCallBack() {
            @Override
            public void onSuccess(Object value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        toMainActivity();
                    }
                });
            }

            @Override
            public void onError(int error,final String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        showFailedError("登录失败, 请检查网络!");
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        showFailedError("登录失败!");
                    }
                });
            }
        });


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    public String getUsername() {
        return etAccount.getText().toString().trim();
    }

    public String getPassword() {
        return etPwd.getText().toString().trim();
    }


    /**
     * 检测是否为隐身登录
     * @return
     */
    public boolean isHiddenLogin() {
        return cbHiddenLogin.isChecked();
    }

    public void showLoading() {
        //显示登录提示对话框
        pd = DialogUtils.getLoadingDialog(LoginActivity.this, R.string.loading_login);
        pd.show();
    }

    public void hideLoading() {
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }

    /**
     * 跳转到主界面,并关闭当前界面
     */
    public void toMainActivity() {
        hideLoading();
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void showFailedError(final String errorMsg) {
        EMToast.makeLoginFailStyleableToast(LoginActivity.this, errorMsg).show();
    }


    /**
     * 检测输入是否合法
     */
    public boolean checkInputVaid() {
        //获取输入的账号和密码
        strName = etAccount.getText().toString().trim();
        strPwd = etPwd.getText().toString().trim();
        if (TextUtils.isEmpty(strName)) {//检测账号是否为空
            Toast.makeText(getApplicationContext(), R.string.toast_account_notIsNull, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(strPwd)) {//检测密码是否为空
            Toast.makeText(getApplicationContext(), R.string.toast_pwd_notIsNull, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!CommonUtils.isNetWorkConnected(this)) {//检测当前是否有网
            Toast.makeText(getApplicationContext(), R.string.toast_network_isnot_available, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_REQUEST_PERMISSIONS)
    public void storageAuthSuccess(){
        login();
    }

    @PermissionFail(requestCode = REQUEST_CODE_REQUEST_PERMISSIONS)
    public void storageAuthFail(){
        new AlertDialog.Builder(this).setMessage("app需要读写手机存储权限 \n请在权限管理->读写手机存储->设为允许!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }).create().show();

    }



}
