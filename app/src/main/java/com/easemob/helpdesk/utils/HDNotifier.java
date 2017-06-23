package com.easemob.helpdesk.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.activity.chat.ChatActivity;
import com.easemob.helpdesk.activity.main.MainActivity;
import com.hyphenate.kefusdk.bean.HDSession;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.manager.CurrentSessionManager;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HDNotifier {
	
	private final static String TAG = HDNotifier.class.getSimpleName();
	
	static Ringtone ringtone = null;
	
	private final static String msg_eng = "receive message!";
	
	private final static String msg_ch = "您收到了消息!";
	
	public static int notifyID = 1314;//start notification id
	
	private NotificationManager notificationManager = null;
	
	private int notificationNum = 0;
	
	private Context appContext;
	
	private String appName;
	private String packageName;
	private String msgs;
	private long lastNotifyTime;
	
	private static HDNotifier instance;
	
	private AudioManager audioManager;
	private Vibrator vibrator;
	
	private ExecutorService notifierThread = Executors.newSingleThreadExecutor();
	
	private HDNotifier(Context context){
		appContext = context;
		if(notificationManager == null){
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		
		if(appContext.getApplicationInfo().labelRes != 0){
			appName = appContext.getString(appContext.getApplicationInfo().labelRes);
		}else{
			appName = "";
		}
		
		packageName = appContext.getApplicationInfo().packageName;
//		if(Locale.getDefault().getLanguage().equals("zh")){
//			
//		}else{
//			
//		}
		msgs = msg_ch;
		
		audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
		
	}
	
	public static HDNotifier getInstance(){
		if(instance == null){
			synchronized (HDNotifier.class){
				if(instance == null){
					instance = new HDNotifier(HDApplication.getInstance());
				}
			}
		}
		return instance;
	}
	
	
	public void stop(){
		if(ringtone != null){
			ringtone.stop();
			ringtone = null;
		}
	}
	

	private void sendNotifaction(HDMessage message){
		PackageManager packageManager = appContext.getPackageManager();

		//notification title

		//create and send notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext);
		mBuilder.setSmallIcon(appContext.getApplicationInfo().icon);
//		mBuilder.setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.icon_launcher2));
		mBuilder.setWhen(System.currentTimeMillis());
		mBuilder.setAutoCancel(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			mBuilder.setColor(Color.GRAY);
		} else {
			mBuilder.setColor(Color.TRANSPARENT);
		}



//		Notification.Builder mBuilder = new Notification.Builder(appContext);
//		mBuilder.setSmallIcon(ChannelConfig.getInstance().getNotificationSmallIcon());
//		mBuilder.setWhen(System.currentTimeMillis());
//		mBuilder.setAutoCancel(true);

		Intent msgIntent = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
		
		if(message!=null){
			msgIntent.putExtra("hasUnReadMessage", true);
			msgIntent.putExtra("visitorid", message.getSessionServiceId());
			HDSession session = CurrentSessionManager.getInstance()
					.getSessionEntity(message.getSessionServiceId());
			if (session != null && MainActivity.instance != null) {
				msgIntent.setClass(appContext, ChatActivity.class);
				System.out.println("HDSession is " + session.getUser().getNicename());
				msgIntent.putExtra("user", session.getUser());
				msgIntent.putExtra("originType",session.getOriginType());
				msgIntent.putExtra("techChannelName",session.getTechChannelName());
			} else {
				System.out.println("HDSession is null");
				msgIntent.putExtra("user", message.getFromUser());
			}
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(appContext, notifyID, msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentTitle(packageManager.getApplicationLabel(appContext.getApplicationInfo()));
		mBuilder.setTicker(msgs);
		mBuilder.setContentText(msgs);
		mBuilder.setContentIntent(pendingIntent);
		Notification notification = mBuilder.build();

//		try{
//			Field field = notification.getClass().getDeclaredField("extraNotification");
//			Object extraNotification = field.get(notification);
//			Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
//			int unreadcount = HDApplication.getInstance().getUnReadMsgCount();
//			method.invoke(extraNotification, unreadcount);
//		}catch (Exception e){
//		}

		try {
			notificationManager.cancel(notifyID);
		} catch (Exception ignored) {
		}
	}
	
	public synchronized void notifiChatMsg(HDMessage message){
		if(!CommonUtils.isAppRunningForeground(appContext)){
			sendNotifaction(message);
		}
		notifyOnNewMsg();
	}
	
	
	public void notifyOnNewMsg() {
		try {
			lastNotifyTime = System.currentTimeMillis();
			// 判断是否处于静音模式
			if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				HDLog.e(TAG, "in slient mode now");
				return;
			}
			long[] pattern = new long[] { 0, 180, 80, 120 };
			vibrator.vibrate(pattern, -1);

			String vendor = Build.MANUFACTURER;
			if (ringtone == null) {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				ringtone = RingtoneManager.getRingtone(appContext, notification);
				if (ringtone == null) {
					HDLog.e(TAG, "cant find ringtone at:" + notification.getPath());
					return;
				}
			}
			if (!ringtone.isPlaying()) {
				ringtone.play();
				if (vendor != null && vendor.toLowerCase(Locale.US).contains("samsung")) {
					Thread ctlThread = new Thread() {

						@Override
						public void run() {
							super.run();
							try {
								Thread.sleep(3000);
								if (ringtone.isPlaying()) {
									ringtone.stop();
								}
							} catch (Exception ignored) {
							}

						}

					};
					ctlThread.start();

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * cancel notification
	 */
	public void cancelNotification(){
		if(notificationManager!=null){
			try {
				notificationManager.cancel(notifyID);
			} catch (Exception ignored) {
			}
		}
	}
	
	
	
	
	
	
	
}
