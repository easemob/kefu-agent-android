package com.easemob.helpdesk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.StrictMode;

import com.easemob.helpdesk.widget.SystemBarTintManager;

public class AppConfig {

    //开启严苛模式
    public static final boolean DEVELOPER_MODE = true;

    /**
     * 严苛模式的工具方法
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setDeveloperMode() {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()//or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    }


    public static void setFitWindowMode(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.bg_top_bar_2);
        }

    }


    public static void setFitWindowMode(Activity activity, int resColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(resColor);
        }

    }


}
