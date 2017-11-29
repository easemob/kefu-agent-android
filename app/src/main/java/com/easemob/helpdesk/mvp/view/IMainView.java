package com.easemob.helpdesk.mvp.view;

/**
 * Created by liyuzhao on 16/4/11.
 */
public interface IMainView {

    void refreshAllAvatar();

    void refreshHomeCount();

    void refreshMenuNickAndStatus();

    void refreshMaxAccessCount();

    void tipCurrentUserDeleted();

    void tipAgentRoleChange();

    void tipSessionChangeTimeout();

    void tipSessionTransferDeny();

    void tipSessionTransferSuccess();

    void tipNoticeCenterRefresh();

    void tipTransferSchedule();
}
