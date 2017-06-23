package com.liyuzhao.badger;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by liyuzhao on 16/5/5.
 */
public class BadgeUtil {

    private static final String TAG = BadgeUtil.class.getSimpleName();
    private static List<Integer> notifyIdList = new ArrayList<Integer>();
    private static Badger badger;

    /**
     *
     * @param notification 更新角标一般都是和发送notification并行的.如果不想发notification只是更新角标,这里传null
     * @param notifyID notificationId
     * @param context
     * @param thisNotifyCount notifyID对应的notification的未读总数(XiaoMi 发送的是这个count,XiaoMi 会自动计算所有notification 未读数量)
     * @param count 整个APP所有的未读数量 (其他的是使用这个count)
     */
    public static void sendBadgeNotification(Notification notification, int notifyID, Context context, int thisNotifyCount, int count){
        if (count <= 0){
            count = 0;
        } else {
            count = Math.max(0, Math.min(count, 99));
        }
//
//        String currentHomePackage = getLauncherName(context);
//
//        Log.d(TAG, "currentHomePackage:" + currentHomePackage);
//        if (badger == null){
//            badger = BadgerType.getBadgerByLauncherName(currentHomePackage);
//        }
//        badger.executeBadge(context, notification, notifyID, thisNotifyCount, count);

        ShortcutBadger.applyCount(context, notification, notifyID, thisNotifyCount, count);
    }


    /**
     * 重置,清除Badge未读显示数<br/>
     * @param context
     */
    public static void resetBadgeCount(Context context){
//        sendBadgeNotification(null, 0, context, 0, 0);

        ShortcutBadger.removeCount(context);
    }

    /**
     * 获取手机的launcher name
     * @param context
     * @return
     */
    public static String getLauncherName(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null)
            return resolveInfo.activityInfo.packageName;
        else
            return "";

    }

}
