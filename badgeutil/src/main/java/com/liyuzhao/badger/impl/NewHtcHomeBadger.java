package com.liyuzhao.badger.impl;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.liyuzhao.badger.Badger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liyuzhao on 16/5/5.
 */
public class NewHtcHomeBadger extends Badger {

    public static final String INTENT_UPDATE_SHORTCUT = "com.htc.launcher.action.UPDATE_SHORTCUT";
    public static final String INTENT_SET_NOTIFICATION = "com.htc.launcher.action.SET_NOTIFICATION";
    public static final String PACKAGENAME = "packagename";
    public static final String COUNT = "count";
    public static final String EXTRA_COMPONET = "com.htc.launcher.extra.COMPONENT";
    public static final String EXTRA_COUNT = "com.htc.launcher.extra.COUNT";


    @Override
    public void executeBadge(Context context, Notification notification, int notificationId, int thisNotificationCount, int count) {
        setNotification(notification, notificationId, context);
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        ComponentName localComponentName = new ComponentName(context.getPackageName(), getLauncherClassName(context));
        Intent intent = new Intent(INTENT_SET_NOTIFICATION);
        intent.putExtra(EXTRA_COMPONET, localComponentName.flattenToShortString());
        intent.putExtra(EXTRA_COUNT, count);
        context.sendBroadcast(intent);

        Intent intent1 = new Intent(INTENT_UPDATE_SHORTCUT);
        intent1.putExtra(PACKAGENAME, context.getPackageName());
        intent1.putExtra(COUNT, count);
        context.sendBroadcast(intent1);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.htc.launcher");
    }
}
