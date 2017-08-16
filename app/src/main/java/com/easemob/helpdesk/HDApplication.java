package com.easemob.helpdesk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;

import com.easemob.helpdesk.mvp.LoginActivity;
import com.easemob.helpdesk.service.WorkService;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.manager.session.CurrentSessionManager;
import com.liyuzhao.badger.BadgeUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HDApplication extends Application {
    private static final String TAG = HDApplication.class.getSimpleName();
    private final List<Activity> activityList = Collections.synchronizedList(new LinkedList<Activity>());

    public static long AgentLastUpdateTime = 0;
    public Bitmap avatarBitmap = null;
    public boolean avatarIsUpdate;

    private static HDApplication instance;

    private Handler mHandler = new Handler();

    private boolean isBroadcastUnreadCount;

    private boolean newMsgNotiStatus;
    private boolean notiAlertSoundStatus;
    private boolean notiAlertVibrateStatus;


    @Override
    public void onCreate() {
        super.onCreate();
	    instance = this;
        HDClient.getInstance().init(this);
        PreferenceUtils.getInstance().init(this);
        registerActivityListener();
        IMHelper.getInstance().setGlobalListener();
        HDClient.getInstance().setDebugMode(true);

        if (HDClient.getInstance().isLoggedInBefore()){
            // 我们现在需要服务运行，将标志位重置为 false;
            WorkService.sShouldStopService = false;
            startService(new Intent(this, WorkService.class));
        }

        isBroadcastUnreadCount = PreferenceUtils.getInstance().getBroadcastUnReadCount();

        newMsgNotiStatus = PreferenceUtils.getInstance().getNewMsgNotiStatus();
        notiAlertSoundStatus = PreferenceUtils.getInstance().getNotiAlertSoundStatus();
        notiAlertVibrateStatus = PreferenceUtils.getInstance().getNotiAlertVibrateStatus();
    }


    public void setBroadcastUnreadCount(boolean enable){
        isBroadcastUnreadCount = enable;
        PreferenceUtils.getInstance().setBroadcastUnReadCount(enable);
    }

    public boolean isBroadcastUnreadCount(){
        return isBroadcastUnreadCount;
    }

    public void setNewMsgNotiStatus(boolean newMsgNotiStatus) {
        this.newMsgNotiStatus = newMsgNotiStatus;
        PreferenceUtils.getInstance().setNewMsgNotiStatus(newMsgNotiStatus);
    }

    public void setNotiAlertSoundStatus(boolean notiAlertSoundStatus) {
        this.notiAlertSoundStatus = notiAlertSoundStatus;
        PreferenceUtils.getInstance().setNotiAlertSoundStatus(notiAlertSoundStatus);
    }

    public void setNotiAlertVibrateStatus(boolean notiAlertVibrateStatus) {
        this.notiAlertVibrateStatus = notiAlertVibrateStatus;
        PreferenceUtils.getInstance().setNotiAlertVibrateStatus(notiAlertVibrateStatus);
    }

    public boolean isNewMsgNotiStatus() {
        return newMsgNotiStatus;
    }

    public boolean isNotiAlertSoundStatus() {
        return notiAlertSoundStatus;
    }

    public boolean isNotiAlertVibrateStatus() {
        return notiAlertVibrateStatus;
    }

    public synchronized int getUnReadMsgCount() {
        return CurrentSessionManager.getInstance().getTotalUnReadCount();
    }

    /**
     * Class Instance Object
     *
     * @return HDApplication
     */
    public static HDApplication getInstance() {
        return instance;
    }

    /**
     * Save Activity to existing lists
     *
     * @param activity
     */
    public void pushActivity(Activity activity) {
        synchronized (activityList){
            if (!activityList.contains(activity)) {
                activityList.add(activity);
            }
        }
    }

    public void popActivity(Activity activity) {
        synchronized (activityList){
            if (activityList.contains(activity)) {
                activityList.remove(activity);
            }
        }
    }

    public boolean isNoActivity(){
        return activityList.isEmpty();
    }


    /**
     * 获取当前最顶部的activity的实例
     * @return
     */
    public Activity getTopActivity(){
        Activity mBaseActivity;
        synchronized (activityList){
            final int size = activityList.size() - 1;
            if(size < 0){
                return null;
            }
            mBaseActivity = activityList.get(size);
        }
        return mBaseActivity;
    }


    /**
     * 结束所有的Activity
     */
    public void finishAllActivity() {
        synchronized (activityList){
            for (Activity activity : activityList) {
                if (activity != null) {
                    if (!activity.isFinishing()) {
                        activity.finish();
                    }
                }
            }
        }
    }


    private void registerActivityListener(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    /**
                     * 监听 activity 创建事件 将该activity 加入list
                     */
                    if(!(activity instanceof LoginActivity)){
                        pushActivity(activity);
                    }

                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    /**
                     * 监听到Activity销毁事件 将该Activity 从list中移除
                     */
                    popActivity(activity);
                }
            });
        }
    }


    public synchronized void logout() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (avatarBitmap != null && !avatarBitmap.isRecycled()) {
                    avatarBitmap.recycle();
                    avatarBitmap = null;
                }
                try{
                    BadgeUtil.resetBadgeCount(getApplicationContext());
                }catch (Exception e){
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
