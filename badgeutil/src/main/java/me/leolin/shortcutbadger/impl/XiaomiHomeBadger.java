package me.leolin.shortcutbadger.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import me.leolin.shortcutbadger.Badger;
import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.util.BroadcastHelper;

/**
 * @author leolin
 */

public class XiaomiHomeBadger extends Badger {

    public static final String INTENT_ACTION = "android.intent.action.APPLICATION_MESSAGE_UPDATE";
    public static final String EXTRA_UPDATE_APP_COMPONENT_NAME = "android.intent.extra.update_application_component_name";
    public static final String EXTRA_UPDATE_APP_MSG_TEXT = "android.intent.extra.update_application_message_text";

    @Override
    public void executeBadge(Context context, ComponentName componentName, Notification notification, int notificationId, int thisNotificationCount, int count) throws ShortcutBadgeException {
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
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException {
        try {
            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
            Object miuiNotification = miuiNotificationClass.newInstance();
            Field field = miuiNotification.getClass().getDeclaredField("messageCount");
            field.setAccessible(true);
            field.set(miuiNotification, String.valueOf(badgeCount == 0 ? "" : badgeCount));
        } catch (Exception e) {
            Intent localIntent = new Intent(
                    INTENT_ACTION);
            localIntent.putExtra(EXTRA_UPDATE_APP_COMPONENT_NAME, componentName.getPackageName() + "/" + componentName.getClassName());
            localIntent.putExtra(EXTRA_UPDATE_APP_MSG_TEXT, String.valueOf(badgeCount == 0 ? "" : badgeCount));
            if (BroadcastHelper.canResolveBroadcast(context, localIntent)) {
                context.sendBroadcast(localIntent);
            } else {
                throw new ShortcutBadgeException("unable to resolve intent: " + localIntent.toString());
            }
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
