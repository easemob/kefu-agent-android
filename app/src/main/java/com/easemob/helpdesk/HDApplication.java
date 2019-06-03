package com.easemob.helpdesk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.easemob.badger.BadgeUtil;
import com.easemob.helpdesk.mvp.LoginActivity;
import com.easemob.helpdesk.utils.ActivityUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.hyphenate.autoupdate.CheckVersion;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.push.EMPushConfig;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

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
    private boolean notiAlertAlarmStatus;
    private boolean isHasAlarmNoti;

    @Override
    public void onCreate() {
        super.onCreate();
	    instance = this;
        ActivityUtils.getInstance().init(this);
	    HMSPushHelper.getInstance().initHMSAgent(this);
        HDClient.getInstance().setDebugMode(true);
        HDClient.Options options = new HDClient.Options();
        EMPushConfig.Builder pushBuilder = new EMPushConfig.Builder(this);
        pushBuilder.enableVivoPush() // 需要在AndroidManifest.xml中配置appId和appKey
                .enableMeiZuPush("120517", "9a43d4aae67c4fc48572af474d749b26")
                .enableMiPush("2882303761517428629", "5941742837629")
                .enableOppoPush("3483386", "1mnvxW1exj4008k80C88w4cSW")
                .enableHWPush(); // 需要在AndroidManifest.xml中配置appId
        options.setPushConfig(pushBuilder.build());
        HDClient.getInstance().init(this, options);
        CheckVersion.getInstance().setUpdateUrl(ChannelConfig.getInstance().getCheckUpdateVersion());
        PreferenceUtils.getInstance().init(this);

        registerActivityListener();
        IMHelper.getInstance().setGlobalListener();

        if (HDClient.getInstance().isLoggedInBefore()){
            HDUser currentUser = HDClient.getInstance().getCurrentUser();
            if (currentUser != null){
                putGrowingIO(currentUser);
                long companyId = currentUser.getTenantId();
                String strCompanyId = "" + companyId;
                initBugly(strCompanyId);
            }
        }

        isBroadcastUnreadCount = PreferenceUtils.getInstance().getBroadcastUnReadCount();

        newMsgNotiStatus = PreferenceUtils.getInstance().getNewMsgNotiStatus();
        notiAlertSoundStatus = PreferenceUtils.getInstance().getNotiAlertSoundStatus();
        notiAlertVibrateStatus = PreferenceUtils.getInstance().getNotiAlertVibrateStatus();
        notiAlertAlarmStatus = PreferenceUtils.getInstance().getNotiAlertAlarmStatus();
        isHasAlarmNoti = PreferenceUtils.getInstance().getHasAlarmNotiStatus();

        if (LeakCanary.isInAnalyzerProcess(this)){
            // This process is dedicated to LeakCanary for heap analysis.
            return;
        }
//            com.squareup.leakcanary.ExcludedRefs excludedRefs = com.squareup.leakcanary.AndroidExcludedRefs.createAppDefaults()
//                    .instanceField("android.view.inputmethod.InputMethodManager", "sInstance")
//                    .instanceField("android.view.inputmethod.InputMethodManager", "mLastSrvView")
//                    .instanceField("android.view.inputmethod.InputMethodManager$1", "this$0")
//                    .instanceField("android.view.inputmethod.InputMethodManager$ControlledInputConnectionWrapper", "mParentInputMethodManager")
//                    .instanceField("com.android.internal.policy.PhoneWindow$DecorView", "mContext")
//                    .instanceField("android.support.v7.widget.SearchView$SearchAutoComplete", "mContext")
//                    .build();
//            LeakCanary.refWatcher(this)
//                    .listenerServiceClass(com.squareup.leakcanary.DisplayLeakService.class)
//                    .excludedRefs(excludedRefs)
//                    .buildAndInstall();
            LeakCanary.install(this);

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

    public void setNotiAlertAlarmStatus(boolean notiAlertAlarmStatus) {
        this.notiAlertAlarmStatus = notiAlertAlarmStatus;
        PreferenceUtils.getInstance().setNotiAlertAlarmStatus(notiAlertAlarmStatus);

    }

    public boolean isHasAlarmNoti() {
        return isNotiAlertAlarmStatus() && isHasAlarmNoti;
    }

    public void setHasAlarmNoti(boolean isHasAlarmNoti) {
        this.isHasAlarmNoti = isHasAlarmNoti;
        PreferenceUtils.getInstance().setHasAlarmNotiStatus(isHasAlarmNoti);
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

    public boolean isNotiAlertAlarmStatus() {
        return newMsgNotiStatus && notiAlertAlarmStatus;
    }

    /**
     * 注册Bugly
     */
    public void initBugly(String strCompanyId){
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setAppChannel(ChannelConfig.getInstance().getChannelString());
        strategy.setAppVersion(CommonUtils.getAppVersionNameFromApp(this));
        strategy.setAppPackageName(getPackageName());
        strategy.setAppReportDelay(20000);
        CrashReport.initCrashReport(getApplicationContext(), "900027600", HDClient.getInstance().isDebugMode(), strategy);
        CrashReport.setUserId("companyId:" + strCompanyId);
    }

    public void putGrowingIO(HDUser loginUser) {
        if (loginUser == null){
            return;
        }
        String userId = loginUser.getUsername();
        long companyId = loginUser.getTenantId();
        String strCompanyId = "" + companyId;

        initBugly(strCompanyId);
    }

    public synchronized int getUnReadMsgCount() {
        return HDClient.getInstance().ongoingSessionManager().getTotalUnReadCount();
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
                }catch (Exception ignored){
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
