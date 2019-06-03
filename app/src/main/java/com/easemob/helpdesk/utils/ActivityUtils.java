package com.easemob.helpdesk.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

public class ActivityUtils {

    private static ActivityUtils INSTANCE = new ActivityUtils();
    private ActivityUtils(){}
    private SimpleActivityLifecycle lifecycle;

    public static ActivityUtils getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        Application appContext = (Application) context.getApplicationContext();
        if (lifecycle == null) {
            lifecycle = new SimpleActivityLifecycle();
        }
        appContext.registerActivityLifecycleCallbacks(lifecycle);
    }


    public boolean isForeground() {
        if (lifecycle == null) {
            throw new IllegalStateException("please init first");
        }
        return lifecycle.isForeground();
    }

    private class SimpleActivityLifecycle implements Application.ActivityLifecycleCallbacks {

        private boolean isForeground = false; //应用是否处于前端

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            isForeground = true;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            isForeground = false;
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        public boolean isForeground() {
            return isForeground;
        }
    }
}
