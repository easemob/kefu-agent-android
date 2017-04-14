package com.easemob.helpdesk.emoticon.data;

public class AppBean {
    private int id;
    private int icon;
    private String funcName;

    public int getIcon() {
        return icon;
    }

    public String getFuncName() {
        return funcName;
    }

    public int getId() {
        return id;
    }

    public AppBean(int icon, String funcName){
        this.icon = icon;
        this.funcName = funcName;
    }
    public AppBean(int id, int icon, String funcName){
        this.id = id;
        this.icon = icon;
        this.funcName = funcName;
    }
}
