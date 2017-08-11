package com.easemob.helpdesk.mvp.biz;

import com.easemob.helpdesk.utils.PreferenceUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;

/**
 * Created by liyuzhao on 16/4/11.
 */
public class MainBiz implements IMainBiz {

    private static final String TAG = MainBiz.class.getSimpleName();

    @Override
    public void getTechChannel() {
        HDClient.getInstance().getTechChannel(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                PreferenceUtils.getInstance().setTechChannel(value);
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });
    }


    @Override
    public void getInitData() {
        HDClient.getInstance().getInitData(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                PreferenceUtils.getInstance().setInitData(value);
            }

            @Override
            public void onError(int error, String errorMsg) {
            }

            @Override
            public void onAuthenticationException() {

            }
        });
    }

}
