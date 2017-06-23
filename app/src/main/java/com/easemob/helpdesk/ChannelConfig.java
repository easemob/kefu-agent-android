package com.easemob.helpdesk;

/**
 * Created by lyuzhao on 2015/12/21.
 */
public class ChannelConfig {

    private final ChannelEnum currentChannel = ChannelEnum.Default;

    private static ChannelConfig instance = new ChannelConfig();

    public static ChannelConfig getInstance() {
        return instance;
    }

    public int getCopyFromTxt() {
        return com.easemob.helpdesk.R.string.copy_from;
    }

    public int getCopyFromTxtColor() {
        return com.easemob.helpdesk.R.color.color_copy_from;
    }

    public int getWelcomeLogo() {
        return com.easemob.helpdesk.R.drawable.welcome_logo2;
    }

    public int getWelcomeLogoHeight(int iconWidth) {
        return iconWidth * 360 / 440;
    }

    public int getLoginlogo() {
        return com.easemob.helpdesk.R.drawable.logo_300;
    }

    public int getNotificationSmallIcon(){
        return com.easemob.helpdesk.R.mipmap.icon_launcher2_min;
    }





    enum ChannelEnum {
        Default,
        LangYan,
        PeanutPlan
    }

}
