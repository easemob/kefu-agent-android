package com.easemob.helpdesk;

import com.easemob.helpdesk.utils.HDNotifier;
import com.hyphenate.kefusdk.HDEventListener;
import com.hyphenate.kefusdk.HDNotifierEvent;
import com.hyphenate.kefusdk.chat.HDClient;

/**
 * Created by lyuzhao on 2016/1/11.
 */
public class IMHelper {

    private static final String TAG = IMHelper.class.getSimpleName();
    /**
     * global IM Helper instance
     */
    private static IMHelper instance = null;
    /**
     * HDEventListener
     */
    private HDEventListener eventListener;

    private IMHelper(){
    }

    public synchronized static IMHelper getInstance(){
        if(instance == null){
            instance = new IMHelper();
        }
        return instance;
    }



    public void setGlobalListener(){

        //register message event listener
        registerEventListener();

    }

    protected void registerEventListener() {
        if (eventListener == null){
            eventListener = new HDEventListener() {
                @Override
                public void onEvent(HDNotifierEvent event) {
                    switch (event.getEvent()){
                        case EventNewMessage:
                        case EventNewSession:
                            if (HDApplication.getInstance().isNoActivity()){
                                HDNotifier.getInstance().notifiChatMsg(null);
                            }
                            break;
                    }


                }
            };
            HDClient.getInstance().chatManager().addEventListener(eventListener, new HDNotifierEvent.Event[]{HDNotifierEvent.Event.EventNewMessage, HDNotifierEvent.Event.EventNewSession});
        }

    }


}
