package com.liyuzhao.badger.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.liyuzhao.badger.Badger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liyuzhao on 16/5/5.
 */
public class XiaomiHomeBadger extends Badger {
    @Override
    public void executeBadge(Context context, Notification notification, int notificationId, int thisNotificationCount, int count) {
        try {
            if (notification == null){
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setOnlyAlertOnce(true).build();
                notification = builder.build();
            }
            if (notification != null){
                Class notificationClass = notification.getClass();
                Field field = notificationClass.getDeclaredField("extraNotification");
                Object extraNotification = field.get(notification);
                Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
                method.invoke(extraNotification, thisNotificationCount);//小米这里只要这个notificationId对应的count,而不是所有notification count
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setNotification(notification, notificationId, context);
        }

    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "com.miui.miuilite",
                "com.miui.home",
                "com.miui.miuihome",
                "com.miui.miuihome2",
                "com.miui.mihome",
                "com.miui.mihome2"
        );
    }
}
