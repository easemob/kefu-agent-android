package com.easemob.helpdesk.mvp.biz;

/**
 * Created by liyuzhao on 16/4/8.
 */
public interface OnLoginListener {
    void loginSuccess(String  result);
    void loginFailed(String errorMsg);
}
