package com.easemob.helpdesk.activity.visitor;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.ModifyActivity;
import com.easemob.helpdesk.image.ImageHandleUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.ImageTools;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentProfileEntity;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.PathUtil;

import java.io.File;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickerActivity;

/**
 * 客服信息界面
 * Created by lyuzhao on 2015/12/18.
 */
public class UserProfileActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private final int REQUEST_CODE_MODIFY_NICKNAME = 0x01;
    private final int REQUEST_CODE_MODIFY_TURENAME = 0x02;
    private final int REQUEST_CODE_MODIFY_AGENTNUMBER = 0x03;
    private final int REQUEST_CODE_MODIFY_MOBILE = 0x04;
    private final int REQUEST_CODE_MODIFY_PWD = 0x05;
    private final int REQUEST_CODE_CHOOSE_PICTURE = 0x06;
    private final int REQUEST_CODE_PICTURE_CROP = 0x07;
    private final int REQUEST_CODE_AVATAR_UPLOAD = 0x08;

    private TextView tvNickname;
    private TextView tvTruename;
    private TextView tvNumber;
    private TextView tvMobile;
    private TextView tvEmail;
    private TextView tvPwd;
    private ImageView ivAvatar;

    private RelativeLayout rlNickname;
    private RelativeLayout rlTruename;
    private RelativeLayout rlNumber;
    private RelativeLayout rlMobile;
    private RelativeLayout rlEmail;
    private RelativeLayout rlPwd;
    private RelativeLayout rlAvator;

    private Dialog dialog;

    private AgentProfileEntity userInfo = new AgentProfileEntity();
    private AgentProfileEntity oldUserInfo = new AgentProfileEntity();
    private Bitmap avatarBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_profile);
        initView();
        initListener();
        getData();
    }

    /**
     * 关闭加载框
     */
    private void closeDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    private void getData() {
        dialog = DialogUtils.getLoadingDialog(this,"加载中...");
        dialog.show();
        HDClient.getInstance().agentManager().getAgentInfo(new HDDataCallBack<AgentProfileEntity>() {
            @Override
            public void onSuccess(final AgentProfileEntity value) {
                HDLog.d(TAG, "value:" + value);
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        userInfo = value;
                        if(userInfo == null){
                            return;
                        }
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
                        Toast.makeText(getBaseContext(), "数据获取失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                HDLog.e(TAG, "onAuthenticationException:");
                if (isFinishing()) {
                    return;
                }
                closeDialog();
            }
        });
    }

    private void refreshUI(){
        if(userInfo == null){
            return;
        }
        if(userInfo.agentNumber != null){
            tvNumber.setText(String.valueOf(userInfo.agentNumber));
        }
        if(userInfo.nickName != null){
            tvNickname.setText(String.valueOf(userInfo.nickName));
        }
        if(userInfo.trueName != null){
            tvTruename.setText(String.valueOf(userInfo.trueName));
        }
        if(userInfo.userName != null){
            tvEmail.setText(String.valueOf(userInfo.userName));
        }
        if(userInfo.mobilePhone != null){
            tvMobile.setText(String.valueOf(userInfo.mobilePhone));
        }
        if(userInfo.avatar != null){
            String remoteUrl = String.valueOf(userInfo.avatar);
            HDLog.d(TAG,"download avatar url:" + remoteUrl);
            if(TextUtils.isEmpty(remoteUrl)){
                return;
            }
//            if(remoteUrl.contains("ossimages")){
//                remoteUrl = remoteUrl.replace("ossimages/","");
//            }
            if(remoteUrl.startsWith("//")){
                remoteUrl = "http:"+remoteUrl;
            }
            asyncGetAndSetAvatar(remoteUrl);
        }

    }

    private void asyncGetAndSetAvatar(String remoteUrl){

        final String localPath = CommonUtils.getAvatarPath(remoteUrl);
        final File localFile = new File(localPath);
        if(localFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(localPath);
            ivAvatar.setImageBitmap(bitmap);
            return;
        }

        HDClient.getInstance().visitorManager().downloadFile(localPath, remoteUrl, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (localFile.exists()) {
                    avatarBitmap = BitmapFactory.decodeFile(localPath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.e(TAG,"download avatar success");
                            if (avatarBitmap != null) {
                                ivAvatar.setImageBitmap(avatarBitmap);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
//                Log.e(TAG,"download avatar fail");
                File file = new File(localPath);
                if (file.exists()) {
                    file.delete();
                }
            }

            @Override
            public void onAuthenticationException() {
                File file = new File(localPath);
                if (file.exists()) {
                    file.delete();
                }
            }
        });

    }


    private void initListener() {
        rlAvator.setOnClickListener(this);
        rlNickname.setOnClickListener(this);
        rlTruename.setOnClickListener(this);
        rlNumber.setOnClickListener(this);
        rlMobile.setOnClickListener(this);
        rlEmail.setEnabled(false);
        rlPwd.setOnClickListener(this);
    }

    private void initView() {
        tvNickname = (TextView) findViewById(R.id.et_nickname);
        tvTruename = (TextView) findViewById(R.id.et_truename);
        tvNumber = (TextView) findViewById(R.id.et_number);
        tvMobile = (TextView) findViewById(R.id.et_mobile);
        tvEmail = (TextView) findViewById(R.id.et_email);
        tvPwd = (TextView) findViewById(R.id.et_pwd);
        ivAvatar = (ImageView) findViewById(R.id.iv_avatar);


        rlAvator = (RelativeLayout) findViewById(R.id.rl_avatar);
        rlNickname = (RelativeLayout) findViewById(R.id.rl_nickname);
        rlTruename = (RelativeLayout) findViewById(R.id.rl_truename);
        rlNumber = (RelativeLayout) findViewById(R.id.rl_number);
        rlMobile = (RelativeLayout) findViewById(R.id.rl_mobile);
        rlEmail = (RelativeLayout) findViewById(R.id.rl_email);
        rlPwd = (RelativeLayout) findViewById(R.id.rl_pwd);

    }


    public void back(View view) {
        finish();
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
        if(avatarBitmap != null){
            avatarBitmap.recycle();
            avatarBitmap = null;
        }
    }

    String cropOutputPath = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        closeDialog();
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_MODIFY_NICKNAME || requestCode == REQUEST_CODE_MODIFY_TURENAME
                    ||requestCode == REQUEST_CODE_MODIFY_AGENTNUMBER||requestCode == REQUEST_CODE_MODIFY_MOBILE||requestCode == REQUEST_CODE_MODIFY_PWD){
                String content  = data.getStringExtra("content");
                parseActivityResult(requestCode, content);
            }else if(requestCode == REQUEST_CODE_CHOOSE_PICTURE){
                if(data != null){
//                    sendPicByUri();
                    ArrayList<String> picPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                    if(picPathList == null ||  picPathList.size() == 0){
                        return;
                    }
                    String picPath = picPathList.get(0);
                    HDLog.e(TAG,"picPath:" + picPath);
                    cropOutputPath = new File(PathUtil.getInstance().getAvatarPath(),System.currentTimeMillis()+".png").getPath();
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(Uri.fromFile(new File(picPath)), "image/*");
                    intent.putExtra("crop", true);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 300);
                    intent.putExtra("outputY", 300);
                    intent.putExtra("scale", true);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cropOutputPath);
                    intent.putExtra("return-data",true);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG);
                    intent.putExtra("noFaceDetection", true);
                    startActivityForResult(intent,REQUEST_CODE_PICTURE_CROP);
                }
            }else if(requestCode == REQUEST_CODE_PICTURE_CROP){
                Bundle extras = data.getExtras();
                if(extras == null){
                    return;
                }
                Bitmap bitmap = extras.getParcelable("data");
                if(bitmap == null){
                    return;
                }
                if(cropOutputPath == null){
                    cropOutputPath = new File(PathUtil.getInstance().getAvatarPath(),System.currentTimeMillis()+".png").getPath();
                }
                ImageTools.savePhotoToSDCard(bitmap,cropOutputPath);
                    //ivAvatar.setImageBitmap(photo);
                dialog = DialogUtils.getLoadingDialog(this,"头像上传中...");
                dialog.show();
                uploadAvatarToServer();

            }
        }


    }

    private  void uploadAvatarToServer(){
        if(TextUtils.isEmpty(cropOutputPath)){
            closeDialog();
            return;
        }
        if(!new File(cropOutputPath).exists()){
            closeDialog();
            return;
        }
        try {
            HDClient.getInstance().agentManager().uploadAvatarToServer(cropOutputPath, new HDDataCallBack<String>() {
                @Override
                public void onSuccess(final String value) {
                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              closeDialog();
                              parseActivityResult(REQUEST_CODE_AVATAR_UPLOAD, value);
                              Toast.makeText(UserProfileActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                          }
                      });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
                            Toast.makeText(UserProfileActivity.this,"图片上传失败！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onAuthenticationException() {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            closeDialog();
        }


    }


    private void parseActivityResult(int requestCode, String content){
        if(TextUtils.isEmpty(content)){
            return;
        }
        dialog = DialogUtils.getLoadingDialog(this, "保存中...");
        dialog.show();
        switch(requestCode){
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

    private void saveUserProfile(int requestCode, AgentProfileEntity entity){
        HDClient.getInstance().agentManager().saveUserProfile(entity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if(isFinishing()){
                    return;
                }
                closeDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        oldUserInfo = userInfo;
                        refreshUI();
                        Toast.makeText(UserProfileActivity.this, "数据保存成功！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if(isFinishing()){
                    return;
                }
                closeDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userInfo = oldUserInfo;
                        refreshUI();
                        Toast.makeText(UserProfileActivity.this, "保存失败，请检查网络！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }



    @Override
    public void onClick(View v) {
        String content;
        switch (v.getId()) {
            case R.id.rl_avatar:
                //打开相册新方法
                Intent intent = ImageHandleUtils.pickSingleImage(this, true);
                this.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);


                break;
            case R.id.rl_nickname:
                content = tvNickname.getText().toString();
                startActivityForResult(new Intent(UserProfileActivity.this, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_NICKNAME), REQUEST_CODE_MODIFY_NICKNAME);
                break;
            case R.id.rl_truename:
                content = tvTruename.getText().toString();
                startActivityForResult(new Intent(UserProfileActivity.this, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_TRUENAME), REQUEST_CODE_MODIFY_TURENAME);
                break;
            case R.id.rl_number:
                content = tvNumber.getText().toString();
                startActivityForResult(new Intent(UserProfileActivity.this, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_AGENTNUMBER), REQUEST_CODE_MODIFY_AGENTNUMBER);
                break;
            case R.id.rl_mobile:
                content = tvMobile.getText().toString();
                startActivityForResult(new Intent(UserProfileActivity.this, ModifyActivity.class)
                        .putExtra("content", content)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_MOBILE), REQUEST_CODE_MODIFY_MOBILE);
                break;
            case R.id.rl_pwd:
                startActivityForResult(new Intent(UserProfileActivity.this, ModifyActivity.class)
                        .putExtra("index", ModifyActivity.PROFILE_MODIFY_PWD), REQUEST_CODE_MODIFY_PWD);
                break;
        }
    }
}
