package me.leolin.shortcutbadger.impl;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.util.Collections;
import java.util.List;

import me.leolin.shortcutbadger.Badger;
import me.leolin.shortcutbadger.ShortcutBadgeException;

/**
 * Created by wuxuejian on 2016/10/9.
 * 需在设置 -- 通知和状态栏 -- 应用角标管理 中开启应用
 */
public class ZukHomeBadger extends Badger {

    private final Uri CONTENT_URI = Uri.parse("content://com.android.badge/badge");

    @Override
    public void executeBadge(Context context, ComponentName componentName, Notification notification, int notificationId, int thisNotificationCount, int badgeCount) throws ShortcutBadgeException {
        setNotification(notification, notificationId, context);
        Bundle extra = new Bundle();
        extra.putInt("app_badge_count", badgeCount);
        context.getContentResolver().call(CONTENT_URI, "setAppBadgeCount", null, extra);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException {
        Bundle extra = new Bundle();
        extra.putInt("app_badge_count", badgeCount);
        context.getContentResolver().call(CONTENT_URI, "setAppBadgeCount", null, extra);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList("com.zui.launcher");
    }

}
