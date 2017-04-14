package com.easemob.helpdesk;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;

import com.easemob.helpdesk.activity.MainActivity;
import com.easemob.helpdesk.utils.HDNotifier;
import com.hyphenate.kefusdk.HDEventListener;
import com.hyphenate.kefusdk.HDNotifierEvent;
import com.hyphenate.kefusdk.chat.HDClient;

/**
 * Created by liyuzhao on 05/04/2017.
 */

public class HDApplication extends Application {
	private static HDApplication instance;
	public Bitmap avatarBitmap = null;
	/**
	 * HDEventListener
	 */
	private HDEventListener eventListener;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		HDClient.getInstance().init(this);
        HDClient.getInstance().setDebugMode(true);
		registerEventListener();
	}

	/**
	 * 当App无界面时，提示来了一条消息
	 */
	protected void registerEventListener() {
		if (eventListener == null){
			eventListener = new HDEventListener() {
				@Override
				public void onEvent(HDNotifierEvent event) {
					switch (event.getEvent()){
						case EventNewMessage:
						case EventNewSession:
							if (MainActivity.instance == null){
								HDNotifier.getInstance().notifiChatMsg(null);
							}
							break;
					}
				}
			};
			HDClient.getInstance().chatManager().addEventListener(eventListener, new HDNotifierEvent.Event[]{HDNotifierEvent.Event.EventNewMessage, HDNotifierEvent.Event.EventNewSession});
		}

	}

	/**
	 * Class Instance Object
	 *
	 * @return HDApplication
	 */
	public static HDApplication getInstance() {
		return instance;
	}


	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
