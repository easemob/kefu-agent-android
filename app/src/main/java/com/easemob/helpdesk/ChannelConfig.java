package com.easemob.helpdesk;

import com.hyphenate.kefusdk.chat.HDClient;

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
        if (currentChannel == ChannelEnum.LangYan){
            return com.easemob.helpdesk.R.drawable.welcome_logo_langyan;
        }
        if (currentChannel == ChannelEnum.PeanutPlan){
            return com.easemob.helpdesk.R.drawable.welcome_logo_peanutplan;
        }

        return com.easemob.helpdesk.R.drawable.welcome_logo2;
    }

    public int getWelcomeLogoHeight(int iconWidth) {
        if (currentChannel == ChannelEnum.LangYan){
            return iconWidth * 360 / 440;
        }

        if (currentChannel == ChannelEnum.PeanutPlan){
            return iconWidth * 360 / 440;
        }

        return iconWidth * 360 / 440;
    }

    public int getLoginlogo() {
        if (currentChannel == ChannelEnum.LangYan){
            return com.easemob.helpdesk.R.drawable.logo_300_langyan;
        }
        if (currentChannel == ChannelEnum.PeanutPlan){
            return com.easemob.helpdesk.R.drawable.logo_300_peanutplan;
        }
        return com.easemob.helpdesk.R.drawable.logo_300;
    }

    public int getNotificationSmallIcon(){
        if (currentChannel == ChannelEnum.LangYan){
            return com.easemob.helpdesk.R.mipmap.ic_launcher_langyan;
        }

        if (currentChannel == ChannelEnum.PeanutPlan){
            return com.easemob.helpdesk.R.mipmap.ic_launcher_peanutplan;
        }

        return com.easemob.helpdesk.R.mipmap.icon_launcher2_min;
    }


    public String getChannelString(){
        if (currentChannel == ChannelEnum.LangYan){
            return "langyan";
        }
        if (currentChannel == ChannelEnum.PeanutPlan){
            return "peanutplan";
        }

        return "Tools";
    }

    public String getCheckUpdateVersion(){
        // KefuApp_Update.txt
//        return "http://o8ugkv090.bkt.clouddn.com/KefuApp_Update.txt";
//        return "http://kefu.easemob.com/app/andriod/KefuApp_Update.txt";
        if (currentChannel == ChannelEnum.LangYan){
            return "http://oe56kc285.bkt.clouddn.com/KefuApp_Update.txt";
        }
        if (currentChannel == ChannelEnum.PeanutPlan){
            return "http://oe56kc285.bkt.clouddn.com/KefuApp_Update_Peanut.txt";
        }

//        return "http://oev49clxj.bkt.clouddn.com/KefuApp_Update.txt";
//        return "http://oe56kc285.bkt.clouddn.com/KefuApp_Update_test.txt";
        return HDClient.getInstance().getKefuServerAddress() + "/app/andriod/KefuApp_Update.txt";
    }


    enum ChannelEnum {
        Default,
        LangYan,
        PeanutPlan
    }

}
