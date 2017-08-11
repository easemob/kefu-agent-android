package com.easemob.helpdesk.mvp.view;

/**
 * 登录界面接口
 *
 * @author liyuzhao
 */
public interface IUserLoginView {
    String getUsername();

    String getPassword();

    boolean isHiddenLogin();

    void showLoading();

    void hideLoading();

    void toMainActivity();

    /**
     * 显示登录失败
     */
    void showFailedError(final String errorMsg);

    boolean checkInputVaid();


}
