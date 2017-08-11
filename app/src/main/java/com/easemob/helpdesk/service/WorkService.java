package com.easemob.helpdesk.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.receiver.WakeUpReceiver;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by liyuzhao
 */

public class WorkService extends Service {

    static final int HASH_CODE = 1;

    //是否 任务完成， 不再需要服务运行
    public static boolean sShouldStopService;

    public static Subscription sSubscription;

    /**
     * 1.防止重复启动，可以任意调用startService(Intent i);
     * 2.利用漏洞启动前台服务而不显示通知;
     * 3.在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题;
     * 4.启动守护服务;
     * 5.守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用.
     */
    int onStart(Intent intent, int flags, int startId) {
        //启动前台服务而不显示通知的漏洞已在 API Level 25 修复，大快人心！
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
            startForeground(HASH_CODE, new Notification());
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                startService(new Intent(HDApplication.getInstance(), WorkNotificationService.class));
        }

        // 启动守护服务， 运行在:watch子进程中
        startService(new Intent(HDApplication.getInstance(), WatchDogService.class));

        //----------业务逻辑----------
        //实际使用时，根据需求，将这里更改为自定义的条件，判定服务应当启动还是停止 (任务是否需要运行)
        if (sShouldStopService) stopService();
        else startService();
        //----------业务逻辑----------
        getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), WatchDogService.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        return START_STICKY;
    }

    static void startService() {
        //检查服务是否不需要运行
        if (sShouldStopService) return;
        // 若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接return
        if (sSubscription != null && !sSubscription.isUnsubscribed()) return;

        //----------业务逻辑----------

        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                .interval(30, TimeUnit.SECONDS)
                //取消任务时需要定时唤醒
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("保存数据到磁盘。");
                        HDApplication.getInstance().sendBroadcast(new Intent(WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB));
                    }
                }).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long count) {
                        System.out.println("每 40 秒采集一次数据... count = " + count);
                        if (count > 0 && count % 18 == 0)
                            System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));
                    }
                });
        //----------业务逻辑----------
    }

    /**
     * 停止服务并取消定时唤醒
     * <p>
     * 停止服务使用取消订阅的方式实现，而不是调用 Context.stopService(Intent name)。因为：
     * 1.stopService 会调用 Service.onDestroy()，而 WorkService 做了保活处理，会把 Service 再拉起来；
     * 2.我们希望 WorkService 起到一个类似于控制台的角色，即 WorkService 始终运行 (无论任务是否需要运行)，
     * 而是通过 onStart() 里自定义的条件，来决定服务是否应当启动或停止。
     */
    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sSubscription != null) sSubscription.unsubscribe();
        //取消 Job / Alarm / Subscription
        HDApplication.getInstance().sendBroadcast(new Intent(WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    void onEnd(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
        startService(new Intent(HDApplication.getInstance(), WorkService.class));
        startService(new Intent(HDApplication.getInstance(), WatchDogService.class));
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    @Override
    public void onDestroy() {
        onEnd(null);
    }

    public static class WorkNotificationService extends Service {
        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WorkService.HASH_CODE, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


}

