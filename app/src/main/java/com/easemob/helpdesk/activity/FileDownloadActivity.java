package com.easemob.helpdesk.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.FileUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.utils.HDLog;

import java.io.File;

/**
 * Created by liyuzhao on 16/8/19.
 */
public class FileDownloadActivity extends BaseActivity {

    private static final String TAG = FileDownloadActivity.class.getSimpleName();

    private View ibBack;
    private NumberProgressBar numberProgressBar;
    private String remoteUrl;
    private String localName;
    private String fileType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_file_download);
        Intent intent = getIntent();
        remoteUrl = intent.getStringExtra("remoteUrl");
        localName = intent.getStringExtra("localName");
        fileType = intent.getStringExtra("type");
        initView();
        downloadFile();
    }

    private void initView() {
        ibBack = $(R.id.rl_back);
        numberProgressBar = $(R.id.number_progress_bar);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * download file
     */
    private void downloadFile() {
        if (TextUtils.isEmpty(remoteUrl)) {
            finish();
            return;
        }
        HDClient.getInstance().visitorManager().downloadFile(localName, remoteUrl, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(localName);
                        if (file.exists()) {
                            if (fileType != null && fileType.equals("audio")){
                                Toast.makeText(getBaseContext(), "语音下载完成！", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                openFile(file);
                                finish();
                            }
                        }
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "errorMsg:" + errorMsg);
                File file = new File(localName);
                if (file.exists()) {
                    file.delete();
                }
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileDownloadActivity.this, "文件下载失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(final int progress) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progress >= 0) {
                            numberProgressBar.setProgress(progress);
                        }
                    }
                });

            }

            @Override
            public void onAuthenticationException() {
                File file = new File(localName);
                if (file.exists()) {
                    file.delete();
                }
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileDownloadActivity.this, "文件下载失败,无权限!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    private void openFile(File file) {
        FileUtils.openFile(file, FileDownloadActivity.this);
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        //设置intent的Action属性
//        intent.setAction(Intent.ACTION_VIEW);
//        //获取文件file的MIME类型
//        String type = CommonUtils.getMIMEType(file);
//        //设置intent的data和Type属性。
//        intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
//        //跳转
//        try {
//            startActivity(intent); //这里最好try一下，有可能会报错。 //比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
//            finish();
//        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "文件无法打开", Toast.LENGTH_SHORT).show();
//        }
    }

}
