package com.easemob.helpdesk.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liyuzhao on 16/6/22.
 */
public class WorkloadAgent implements Parcelable{
    /**
     * sum_am : 15
     * cnt_svc : 8
     * sum_vm : 41
     * sum_sm : 23
     * cnt_sdc : 14
     * cnt_tc : 13
     * realName : Uncle
     * cnt_sc : 14
     * cnt_sac : 6
     * cnt_tic : 0
     * avg_mc : 6
     * max_wt : 66658
     * name : Uncle
     * key : 42b6e936-df96-42e4-bd19-0342d356008d
     * max_mc : 20
     * cnt_oc : 14
     * avg_wt : 6980
     * cnt_toc : 0
     */

    private double sum_am;
    private double cnt_svc;
    private double sum_vm;
    private double sum_sm;
    private double cnt_sdc;
    private double cnt_tc;
    private String realName;
    private double cnt_sc;
    private double cnt_sac;
    private double cnt_tic;
    private double avg_mc;
    private double max_wt;
    private String name;
    private String key;
    private double max_mc;
    private double cnt_oc;
    private double avg_wt;
    private double cnt_toc;

    public WorkloadAgent(){

    }

    protected WorkloadAgent(Parcel in) {
        sum_am = in.readDouble();
        cnt_svc = in.readDouble();
        sum_vm = in.readDouble();
        sum_sm = in.readDouble();
        cnt_sdc = in.readDouble();
        cnt_tc = in.readDouble();
        realName = in.readString();
        cnt_sc = in.readDouble();
        cnt_sac = in.readDouble();
        cnt_tic = in.readDouble();
        avg_mc = in.readDouble();
        max_wt = in.readDouble();
        name = in.readString();
        key = in.readString();
        max_mc = in.readDouble();
        cnt_oc = in.readDouble();
        avg_wt = in.readDouble();
        cnt_toc = in.readDouble();
    }

    public static final Creator<WorkloadAgent> CREATOR = new Creator<WorkloadAgent>() {
        @Override
        public WorkloadAgent createFromParcel(Parcel in) {
            return new WorkloadAgent(in);
        }

        @Override
        public WorkloadAgent[] newArray(int size) {
            return new WorkloadAgent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(sum_am);
        dest.writeDouble(cnt_svc);
        dest.writeDouble(sum_vm);
        dest.writeDouble(sum_sm);
        dest.writeDouble(cnt_sdc);
        dest.writeDouble(cnt_tc);
        dest.writeString(realName);
        dest.writeDouble(cnt_sc);
        dest.writeDouble(cnt_sac);
        dest.writeDouble(cnt_tic);
        dest.writeDouble(avg_mc);
        dest.writeDouble(max_wt);
        dest.writeString(name);
        dest.writeString(key);
        dest.writeDouble(max_mc);
        dest.writeDouble(cnt_oc);
        dest.writeDouble(avg_wt);
        dest.writeDouble(cnt_toc);
    }

    public double getSum_am() {
        return sum_am;
    }

    public void setSum_am(double sum_am) {
        this.sum_am = sum_am;
    }

    public double getCnt_svc() {
        return cnt_svc;
    }

    public void setCnt_svc(double cnt_svc) {
        this.cnt_svc = cnt_svc;
    }

    public double getSum_vm() {
        return sum_vm;
    }

    public void setSum_vm(double sum_vm) {
        this.sum_vm = sum_vm;
    }

    public double getSum_sm() {
        return sum_sm;
    }

    public void setSum_sm(double sum_sm) {
        this.sum_sm = sum_sm;
    }

    public double getCnt_sdc() {
        return cnt_sdc;
    }

    public void setCnt_sdc(double cnt_sdc) {
        this.cnt_sdc = cnt_sdc;
    }

    public double getCnt_tc() {
        return cnt_tc;
    }

    public void setCnt_tc(double cnt_tc) {
        this.cnt_tc = cnt_tc;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public double getCnt_sc() {
        return cnt_sc;
    }

    public void setCnt_sc(double cnt_sc) {
        this.cnt_sc = cnt_sc;
    }

    public double getCnt_sac() {
        return cnt_sac;
    }

    public void setCnt_sac(double cnt_sac) {
        this.cnt_sac = cnt_sac;
    }

    public double getCnt_tic() {
        return cnt_tic;
    }

    public void setCnt_tic(double cnt_tic) {
        this.cnt_tic = cnt_tic;
    }

    public double getAvg_mc() {
        return avg_mc;
    }

    public void setAvg_mc(double avg_mc) {
        this.avg_mc = avg_mc;
    }

    public double getMax_wt() {
        return max_wt;
    }

    public void setMax_wt(double max_wt) {
        this.max_wt = max_wt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getMax_mc() {
        return max_mc;
    }

    public void setMax_mc(double max_mc) {
        this.max_mc = max_mc;
    }

    public double getCnt_oc() {
        return cnt_oc;
    }

    public void setCnt_oc(double cnt_oc) {
        this.cnt_oc = cnt_oc;
    }

    public double getAvg_wt() {
        return avg_wt;
    }

    public void setAvg_wt(double avg_wt) {
        this.avg_wt = avg_wt;
    }

    public double getCnt_toc() {
        return cnt_toc;
    }

    public void setCnt_toc(double cnt_toc) {
        this.cnt_toc = cnt_toc;
    }
}
