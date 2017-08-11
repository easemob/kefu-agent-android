package com.easemob.helpdesk.activity.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.FileUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDFileMessageBody;
import com.hyphenate.kefusdk.entity.HDMessage;

import java.io.File;

/**
 * Created by lyuzhao on 2016/1/21.
 */
public class ShowNormalFileActivity extends BaseActivity {

    private ProgressBar progressBar;
    private String localPath;
    private String remoteUrl;
    private HDMessage message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_show_file);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        message = getIntent().getParcelableExtra("message");
        HDFileMessageBody messageBody = (HDFileMessageBody) message.getBody();
        localPath = messageBody.getLocalPath();
        remoteUrl = messageBody.getRemoteUrl();
        String fileName = messageBody.getFileName();
        if(!TextUtils.isEmpty(localPath)){
            checkAndOpenFile(new File(localPath));
        }else if(!TextUtils.isEmpty(remoteUrl)){
            //download file
            localPath = CommonUtils.getFilePath(remoteUrl, fileName);
            if(TextUtils.isEmpty(localPath)){
                return;
            }
            checkAndOpenFile(new File(localPath));
        }
    }


    public void checkAndOpenFile(File file) {
        if (file == null) {
            return;
        }
        if (file.exists()) {
            FileUtils.openFile(file, this);
            return;
        }
        message.setMessageCallback(new HDDataCallBack() {
            @Override
            public void onSuccess(Object value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.openFile(new File(localPath), ShowNormalFileActivity.this);
                        finish();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File localFile = new File(localPath);
                        if (localFile.exists() && localFile.isFile()) {
                            localFile.delete();
                        }
                        Toast.makeText(ShowNormalFileActivity.this, "文件下载失败！", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
            }
        });

        HDClient.getInstance().chatManager().downloadAttachment(message);

    }



}
