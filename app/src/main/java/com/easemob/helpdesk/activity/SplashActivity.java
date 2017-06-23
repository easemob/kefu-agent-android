package com.easemob.helpdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.main.LoginActivity;
import com.easemob.helpdesk.activity.main.MainActivity;
import com.hyphenate.kefusdk.chat.HDClient;



/**
 * 加载界面，也是APP的第一个界面
 */
public class SplashActivity extends BaseActivity {

    /**
     * 页面显示停留时间（ms）
     */
    private static final int LOAD_DISPLAY_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isTaskRoot()){
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (HDClient.getInstance().isLoggedInBefore()) {
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                SplashActivity.this.finish();
            }
        }, LOAD_DISPLAY_TIME);

    }

}
