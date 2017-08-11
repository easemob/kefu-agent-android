package com.easemob.helpdesk.mvp.presenter;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.mvp.biz.IMainBiz;
import com.easemob.helpdesk.mvp.biz.MainBiz;
import com.easemob.helpdesk.mvp.view.IMainView;
import com.easemob.helpdesk.utils.HDNotifier;


/**
 * Created by liyuzhao on 16/4/11.
 */
public class MainPresenter {

    private IMainBiz mainBiz;
    private IMainView mainView;



    public MainPresenter(IMainView mainView) {
        this.mainView = mainView;
        this.mainBiz = new MainBiz();
    }


    public void getTechChannel() {
        mainBiz.getTechChannel();
    }

    public void getInitData() {
        mainBiz.getInitData();
    }


    public void onResume() {
        HDNotifier.getInstance().cancelNotification();
        if (HDApplication.getInstance().avatarIsUpdate) {
            HDApplication.getInstance().avatarIsUpdate = false;
            mainView.refreshAllAvatar();
        }
    }


}

