package com.easemob.helpdesk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.ChannelConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.mvp.LoginActivity;
import com.easemob.helpdesk.mvp.MainActivity;
import com.hyphenate.kefusdk.chat.HDClient;


/**
 * 加载界面，也是APP的第一个界面
 */
public class SplashActivity extends BaseActivity {

    /**
     * 页面显示停留时间（ms）
     */
    private static final int LOAD_DISPLAY_TIME = 1500;
    /**
     * 页面显示的Logo图片
     */
    private ImageView ivLogo;
    /**
     * 声明的copy信息
     */
    private TextView tvCopyFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isTaskRoot()){
            finish();
            return;
        }
        AppConfig.setFitWindowMode(this, android.R.color.white);
        setContentView(R.layout.activity_splash);
        initView();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (HDClient.getInstance().isLoggedInBefore()) {
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, MainActivity.class);
                    intent.putExtra("displayExpireInfo", true);
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

    /**
     * 获取当前界面所用到的View，并设置他们的值
     */
    private void initView() {
        ivLogo = (ImageView) findViewById(R.id.iv_logo);
        ivLogo.setImageResource(R.drawable.splash_screen);
//        tvCopyFrom = (TextView) findViewById(R.id.tv_copy_from);
//        tvCopyFrom.setText(ChannelConfig.getInstance().getCopyFromTxt());
//        tvCopyFrom.setTextColor(ChannelConfig.getInstance().getCopyFromTxtColor());
//        int winWidth, winHeight;
//        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            Point size = new Point();
//            display.getSize(size);
//            winHeight = size.y;
//            winWidth = size.x;
//        } else {
//            winHeight = display.getHeight();
//            winWidth = display.getWidth();
//        }
//        // 440*360 width:winWidth*2/3 height:
//
//        int iconWidth = winWidth * 2 / 3;
////		int iconHeight = iconWidth * 360 / 440;
//        int iconHeight = ChannelConfig.getInstance().getWelcomeLogoHeight(iconWidth);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        layoutParams.topMargin = winHeight / 5;
//        layoutParams.width = iconWidth;
//        layoutParams.height = iconHeight;
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        ivLogo.setLayoutParams(layoutParams);
////		ivLogo.setImageResource(R.drawable.welcome_logo2);
//        ivLogo.setImageResource(ChannelConfig.getInstance().getWelcomeLogo());
    }

}
