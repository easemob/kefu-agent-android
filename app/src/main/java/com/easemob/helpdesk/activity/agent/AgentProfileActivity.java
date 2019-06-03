package com.easemob.helpdesk.activity.agent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.ModifyActivity;
import com.easemob.helpdesk.image.ImageHandleUtils;
import com.easemob.helpdesk.mvp.LoginActivity;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.ImageTools;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentProfileEntity;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.PathUtil;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.iwf.photopicker.PhotoPickerActivity;

/**
 * Created by liyuzhao on 16/3/2.
 */
public class AgentProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AgentProfileActivity.class.getSimpleName();

    private final int REQUEST_CODE_MODIFY_NICKNAME = 0x01;
    private final int REQUEST_CODE_MODIFY_TURENAME = 0x02;
    private final int REQUEST_CODE_MODIFY_AGENTNUMBER = 0x03;
    private final int REQUEST_CODE_MODIFY_MOBILE = 0x04;
    private final int REQUEST_CODE_MODIFY_PWD = 0x05;
    private final int REQUEST_CODE_CHOOSE_PICTURE = 0x06;
    private final int REQUEST_CODE_PICTURE_CROP = 0x07;
    private final int REQUEST_CODE_AVATAR_UPLOAD = 0x08;
    private final int REQUEST_CODE_MODIFY_WEL_CONTENT = 0x09;

    @BindView(R.id.et_nickname)
    protected TextView tvNickname;
    @BindView(R.id.et_truename)
    protected TextView tvTruename;
    @BindView(R.id.et_number)
    protected TextView tvNumber;
    @BindView(R.id.et_mobile)
    protected TextView tvMobile;
    @BindView(R.id.et_email)
    protected TextView tvEmail;
    @BindView(R.id.et_pwd)
    protected TextView tvPwd;
    @BindView(R.id.tv_nick)
    protected TextView tvNick;
    @BindView(R.id.iv_avatar)
    protected ImageView ivAvatar;

    @BindView(R.id.rl_nickname)
    protected RelativeLayout rlNickname;
    @BindView(R.id.rl_truename)
    protected RelativeLayout rlTruename;
    @BindView(R.id.rl_number)
    protected RelativeLayout rlNumber;
    @BindView(R.id.rl_mobile)
    protected RelativeLayout rlMobile;
    @BindView(R.id.rl_email)
    protected RelativeLayout rlEmail;
    @BindView(R.id.rl_pwd)
    protected RelativeLayout rlPwd;

    @BindView(R.id.switch_button)
    protected SwitchButton switchButton;
    @BindView(R.id.tv_welcome_content)
    protected TextView tvWelcomeContent;

    @BindView(R.id.st_broadcast_unreadcount)
    protected SwitchButton stBroadcastUnreadcount;
    @BindView(R.id.st_new_msg_noti)
    protected SwitchButton stNewMsgNoti;
    @BindView(R.id.st_noti_alert_sound)
    protected SwitchButton stNotiAlertSound;
    @BindView(R.id.st_noti_alert_vibrate)
    protected SwitchButton stNotiAlertVibrate;
    @BindView(R.id.st_noti_alarm)
    protected SwitchButton stNotiAlarm;

    @BindView(R.id.ll_noti_alert_sound)
    protected LinearLayout llNotiAlertSound;
    @BindView(R.id.ll_noti_alert_vibrate)
    protected LinearLayout llNotiAlertVibrate;
    @BindView(R.id.ll_noti_alarm)
    protected LinearLayout llNotiAlarm;


    @BindView(R.id.tv_version)
    protected TextView tvVersion;

    private Dialog dialog;
    private AgentProfileEntity userInfo = new AgentProfileEntity();
    private AgentProfileEntity oldUserInfo = new AgentProfileEntity();
    private final Context mContext = this;
    private HDUser loginUser;
    private String oldWelcomeContent;

    String cropOutputPath = null;
    @BindView(R.id.rl_back)
    protected View viewBack;

    @Override
    protected void onResume() {
        super.onResume();
        if (HDApplication.getInstance().isNewMsgNotiStatus()) {
            llNotiAlertSound.setVisibility(View.VISIBLE);
            llNotiAlertVibrate.setVisibility(View.VISIBLE);
            if (loginUser != null && loginUser.getRoles() != null){
                if (loginUser.getRoles().contains("admin")){
                    llNotiAlarm.setVisibility(View.VISIBLE);
                }else{
                    llNotiAlarm.setVisibility(View.INVISIBLE);
                }
            } else {
                llNotiAlarm.setVisibility(View.INVISIBLE);
            }
        } else {
            llNotiAlertSound.setVisibility(View.INVISIBLE);
            llNotiAlertVibrate.setVisibility(View.INVISIBLE);
            llNotiAlarm.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_agent_profile);
        ButterKnife.bind(this);
        loginUser = HDClient.getInstance().getCurrentUser();
        setVersionText();
        initListener();
        getAgentInfo();
        getAgentGreetingEnable();
        getAgentGreetingContent();
    }


    private void setVersionText(){
        //设置版本号
        String versionName = CommonUtils.getAppVersionNameFromApp(this);
        if (!TextUtils.isEmpty(versionName)){
            tvVersion.setText("v" + versionName);
        }
    }

    private void getAgentGreetingEnable(){
        HDClient.getInstance().agentManager().getAgentGreetingEnable(new HDDataCallBack<Boolean>() {
            @Override
            public void onSuccess(final Boolean value) {
                HDLog.d(TAG, "getGreetingMsgAgentEnable -> :" + value);
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    switchButton.setChecked(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private void getAgentGreetingContent(){
        HDClient.getInstance().agentManager().getAgentGreetingContent(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String optionValue) {
                HDLog.d(TAG, "getGreetingMsgAgentContent -> :" + optionValue);
                if (isFinishing()){return;}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (optionValue != null) {
                            oldWelcomeContent = optionValue;
                            tvWelcomeContent.setText(optionValue);
                        }
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });

    }



    /**
     * 关闭加载框
     */
    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    private void initListener() {
        ivAvatar.setOnClickListener(this);
        rlNickname.setOnClickListener(this);
        rlTruename.setOnClickListener(this);
        rlNumber.setOnClickListener(this);
        rlMobile.setOnClickListener(this);
        rlEmail.setEnabled(false);
        rlPwd.setOnClickListener(this);
        tvWelcomeContent.setOnClickListener(this);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setAgentWelcomeMsgEnable(isChecked);
            }
        });

        viewBack.setOnClickListener(this);

        stBroadcastUnreadcount.setChecked(HDApplication.getInstance().isBroadcastUnreadCount());
        stBroadcastUnreadcount.setOnCheckedChangeListener(broadcastUnreadcountListener);

        stNewMsgNoti.setChecked(HDApplication.getInstance().isNewMsgNotiStatus());
        stNewMsgNoti.setOnCheckedChangeListener(newMsgNotiListener);

        stNotiAlertSound.setChecked(HDApplication.getInstance().isNotiAlertSoundStatus());
        stNotiAlertSound.setOnCheckedChangeListener(notiAlertSoundListener);

        stNotiAlertVibrate.setChecked(HDApplication.getInstance().isNotiAlertVibrateStatus());
        stNotiAlertVibrate.setOnCheckedChangeListener(notiAlertVibrateListener);

        stNotiAlarm.setChecked(HDApplication.getInstance().isNotiAlertAlarmStatus());
        stNotiAlarm.setOnCheckedChangeListener(notiAlertAlarmListener);
    }

    CompoundButton.OnCheckedChangeListener broadcastUnreadcountListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HDApplication.getInstance().setBroadcastUnreadCount(isChecked);
        }
    };

    CompoundButton.OnCheckedChangeListener newMsgNotiListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HDApplication.getInstance().setNewMsgNotiStatus(isChecked);
            if (isChecked) {
                llNotiAlertSound.setVisibility(View.VISIBLE);
                llNotiAlertVibrate.setVisibility(View.VISIBLE);
                loginUser = HDClient.getInstance().getCurrentUser();
                if (loginUser != null && loginUser.getRoles() != null){
                    if (loginUser.getRoles().contains("admin")){
                        llNotiAlarm.setVisibility(View.VISIBLE);
                    }else{
                        llNotiAlarm.setVisibility(View.INVISIBLE);
                    }
                } else {
                    llNotiAlarm.setVisibility(View.INVISIBLE);
                }
            } else {
                llNotiAlertSound.setVisibility(View.INVISIBLE);
                llNotiAlertVibrate.setVisibility(View.INVISIBLE);
                llNotiAlarm.setVisibility(View.INVISIBLE);
            }
        }
    };

    CompoundButton.OnCheckedChangeListener notiAlertSoundListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HDApplication.getInstance().setNotiAlertSoundStatus(isChecked);
        }
    };

    CompoundButton.OnCheckedChangeListener notiAlertVibrateListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HDApplication.getInstance().setNotiAlertVibrateStatus(isChecked);
        }
    };

    CompoundButton.OnCheckedChangeListener notiAlertAlarmListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HDApplication.getInstance().setNotiAlertAlarmStatus(isChecked);
        }
    };

    private void setAgentWelcomeMsgEnable(boolean enable) {
        HDClient.getInstance().agentManager().setAgentWelcomeMsgEnable(enable, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {

            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
    }


    private void getAgentInfo() {
        dialog = DialogUtils.getLoadingDialog(this, R.string.info_loading);
        dialog.show();
        HDClient.getInstance().agentManager().getAgentInfo(new HDDataCallBack<AgentProfileEntity>() {
            @Override
            public void onSuccess(final AgentProfileEntity result) {
                HDLog.d(TAG, "value:" + result);
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    closeDialog();
                    if (result == null){
                        return;
                    }
                    userInfo = result;
                    userInfo.password = null;
                    oldUserInfo = userInfo;
                    if (userInfo != null) {
                        refreshUI();
                    }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "errorMsg:" + errorMsg);
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), getString(R.string.toast_getdata_fail), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

    private void parseActivityResult(int requestCode, String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        if (isFinishing()){
            return;
        }

        dialog = DialogUtils.getLoadingDialog(this, R.string.info_saveing);
        dialog.show();
        switch (requestCode) {
            case REQUEST_CODE_MODIFY_NICKNAME:
                userInfo.nickName = content;
                break;
            case REQUEST_CODE_MODIFY_TURENAME:
                userInfo.trueName = content;
                break;
            case REQUEST_CODE_MODIFY_AGENTNUMBER:
                userInfo.agentNumber = content;
                break;
            case REQUEST_CODE_MODIFY_MOBILE:
                userInfo.mobilePhone = content;
                break;
            case REQUEST_CODE_MODIFY_PWD:
                userInfo.password = content;
                break;
            case REQUEST_CODE_AVATAR_UPLOAD:
                userInfo.avatar = content;
                break;
        }
        saveUserProfile(requestCode, userInfo);
    }


    private void refreshUI() {
        if (userInfo == null) {
            return;
        }
        if (userInfo.agentNumber != null) {
            tvNumber.setText(String.valueOf(userInfo.agentNumber));
        }
        if (userInfo.nickName != null) {
            tvNickname.setText(String.valueOf(userInfo.nickName));
            tvNick.setText(String.valueOf(userInfo.nickName));
        }
        if (userInfo.trueName != null) {
            tvTruename.setText(String.valueOf(userInfo.trueName));
        }
        if (userInfo.userName != null) {
            tvEmail.setText(String.valueOf(userInfo.userName));
        }
        if (userInfo.mobilePhone != null) {
            tvMobile.setText(String.valueOf(userInfo.mobilePhone));
        }
        if (userInfo.avatar != null) {
            String remoteUrl = String.valueOf(userInfo.avatar);
            HDLog.d(TAG, "download avatar url:" + remoteUrl);
            if (TextUtils.isEmpty(remoteUrl)) {
                return;
            }
            if (remoteUrl.startsWith("//")) {
                remoteUrl = "http:" + remoteUrl;
            }

            if(remoteUrl.contains("/images/uikit/")){
                return;
            }
            AvatarManager.getInstance().asyncGetAvatar(ivAvatar, remoteUrl, this);
        }

    }


    private void saveUserProfile(int requestCode, final AgentProfileEntity entity) {
        HDClient.getInstance().agentManager().saveUserProfile(entity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        oldUserInfo = userInfo;
                        refreshUI();
                        Toast.makeText(mContext, getString(R.string.toast_getdata_success), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                closeDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userInfo = oldUserInfo;
                        refreshUI();
                        Toast.makeText(mContext, getString(R.string.toast_save_fail_p_check_net), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        closeDialog();
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_MODIFY_NICKNAME || requestCode == REQUEST_CODE_MODIFY_TURENAME
                    || requestCode == REQUEST_CODE_MODIFY_AGENTNUMBER || requestCode == REQUEST_CODE_MODIFY_MOBILE || requestCode == REQUEST_CODE_MODIFY_PWD) {
                String content = data.getStringExtra("content");
                parseActivityResult(requestCode, content);
            } else if (requestCode == REQUEST_CODE_CHOOSE_PICTURE) {
                if (data != null) {
                    ArrayList<String> picPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                    if (picPathList == null || picPathList.size() == 0) {
                        return;
                    }
                    String picPath = picPathList.get(0);
                    HDLog.d(TAG, "picPath:" + picPath);
                    cropOutputPath = new File(PathUtil.getInstance().getAvatarPath(), System.currentTimeMillis() + ".png").getPath();
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(Uri.fromFile(new File(picPath)), "image/*");
                    intent.putExtra("crop", true);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 300);
                    intent.putExtra("outputY", 300);
                    intent.putExtra("scale", true);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cropOutputPath);
                    intent.putExtra("return-data", true);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG);
                    intent.putExtra("noFaceDetection", true);
                    startActivityForResult(intent, REQUEST_CODE_PICTURE_CROP);
                }
            } else if (requestCode == REQUEST_CODE_PICTURE_CROP) {
                if(data == null){
                    return;
                }
                Bundle extras = data.getExtras();
                if (extras == null) {
                    return;
                }
                Bitmap bitmap = extras.getParcelable("data");
                if (bitmap == null) {
                    return;
                }
                if (cropOutputPath == null) {
                    cropOutputPath = new File(PathUtil.getInstance().getAvatarPath(), System.currentTimeMillis() + ".png").getPath();
                }
                ImageTools.savePhotoToSDCard(bitmap, cropOutputPath);
                dialog = DialogUtils.getLoadingDialog(this, R.string.info_avatar_uploding);
                dialog.show();
                uploadAvatarToServer();
            } else if(requestCode == REQUEST_CODE_MODIFY_WEL_CONTENT){
                String content = data.getStringExtra("content");
                asyncUpdateAgentWelcontent(content);
            }
        }
    }

    private void asyncUpdateAgentWelcontent(final String content) {
        tvWelcomeContent.setText(content);
        HDClient.getInstance().agentManager().asyncUpdateAgentWelcontent(content, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {

            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()){return;}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvWelcomeContent.setText(oldWelcomeContent);
                    }
                });

            }
        });
    }

    private void uploadAvatarToServer() {
        if (TextUtils.isEmpty(cropOutputPath)) {
            closeDialog();
            return;
        }
        if (!new File(cropOutputPath).exists()) {
            closeDialog();
            return;
        }
        try {
            HDClient.getInstance().agentManager().uploadAvatarToServer(cropOutputPath, new HDDataCallBack<String>() {
                @Override
                public void onSuccess(final String value) {
                    if (isFinishing()){return;}
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
                            HDApplication.getInstance().avatarBitmap = null;
                            HDApplication.getInstance().avatarIsUpdate = true;
                            parseActivityResult(REQUEST_CODE_AVATAR_UPLOAD, value);
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    if (isFinishing()){return;}
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
                            Toast.makeText(mContext, getString(R.string.toast_pic_upload_fail), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            closeDialog();
        }


    }

    @OnClick(R.id.agentprofile_logout)
    public void logout(){
        // 退出时必须有网
        dialog = DialogUtils.getLoadingDialog(this, R.string.info_logouting);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        HDClient.getInstance().logout(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (isFinishing()){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().finishAllActivity();
                        Intent intent = new Intent(AgentProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(AgentProfileActivity.this, "退出失败，请检查网络！", Toast.LENGTH_SHORT).show();
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
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });


    }


    @Override
    public void onClick(View v) {
        String content;
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;

            case R.id.iv_avatar:
                //打开相册新方法
                Intent intent = ImageHandleUtils.pickSingleImage(this, true);
                this.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);


                break;
            case R.id.rl_nickname:
                content = tvNickname.getText().toString();
                startActivityForResult(new Intent(mContext, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_NICKNAME), REQUEST_CODE_MODIFY_NICKNAME);
                break;
            case R.id.rl_truename:
                content = tvTruename.getText().toString();
                startActivityForResult(new Intent(mContext, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_TRUENAME), REQUEST_CODE_MODIFY_TURENAME);
                break;
            case R.id.rl_number:
                content = tvNumber.getText().toString();
                startActivityForResult(new Intent(mContext, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_AGENTNUMBER), REQUEST_CODE_MODIFY_AGENTNUMBER);
                break;
            case R.id.rl_mobile:
                content = tvMobile.getText().toString();
                startActivityForResult(new Intent(mContext, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_MOBILE), REQUEST_CODE_MODIFY_MOBILE);
                break;
            case R.id.rl_pwd:
                startActivityForResult(new Intent(mContext, ModifyActivity.class)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_PWD), REQUEST_CODE_MODIFY_PWD);
                break;
            case R.id.tv_welcome_content:
                content = tvWelcomeContent.getText().toString();
                startActivityForResult(new Intent(mContext, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.MODIFY_WEL_CONTENT), REQUEST_CODE_MODIFY_WEL_CONTENT);
                break;

        }
    }

}
