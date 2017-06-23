package me.leolin.shortcutbadger;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

public abstract class Badger {

    /**
     *
     * @param context
     * @param notification  更新角标一般都是和发送notification并行的.如果不想发notification只是更新角标,这里传null
     * @param notificationId
     * @param thisNotificationCount
     * @param badgeCount
     */
    public abstract void executeBadge(Context context, ComponentName componentName, Notification notification, int notificationId, int thisNotificationCount, int badgeCount) throws ShortcutBadgeException;


    /**
     * Called when user attempts to update notification count
     * @param context Caller context
     * @param componentName Component containing package and class name of calling application's
     *                      launcher activity
     * @param badgeCount Desired notification count
     * @throws ShortcutBadgeException
     */
    public abstract void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortcutBadgeException;

    /**
     * Called to let {@link ShortcutBadger} knows which launchers are supported by this badger. It should return a
     * @return List containing supported launchers package names
     */
    public abstract List<String> getSupportLaunchers();



    protected void setNotification(Notification notification, int notificationId, Context context){
        if (notification != null){
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification);
        }
    }
}
