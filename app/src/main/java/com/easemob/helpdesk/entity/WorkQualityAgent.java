package com.easemob.helpdesk.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liyuzhao on 16/6/23.
 */
public class WorkQualityAgent implements Parcelable{

    public int max_ar;
    public int cnt_ea;
    public int max_fr;
    public double avg_ar;
    public double avg_fr;
    public double avg_vm;
    public int cnt_ua;
    public String name;
    public String pct_vm;
    public String key;
    public String markList;

    public WorkQualityAgent(){}

    protected WorkQualityAgent(Parcel in) {
        max_ar = in.readInt();
        cnt_ea = in.readInt();
        max_fr = in.readInt();
        avg_ar = in.readDouble();
        avg_fr = in.readDouble();
        avg_vm = in.readDouble();
        cnt_ua = in.readInt();
        name = in.readString();
        pct_vm = in.readString();
        key = in.readString();
        markList = in.readString();
    }

    public static final Creator<WorkQualityAgent> CREATOR = new Creator<WorkQualityAgent>() {
        @Override
        public WorkQualityAgent createFromParcel(Parcel in) {
            return new WorkQualityAgent(in);
        }

        @Override
        public WorkQualityAgent[] newArray(int size) {
            return new WorkQualityAgent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(max_ar);
        dest.writeInt(cnt_ea);
        dest.writeInt(max_fr);
        dest.writeDouble(avg_ar);
        dest.writeDouble(avg_fr);
        dest.writeDouble(avg_vm);
        dest.writeInt(cnt_ua);
        dest.writeString(name);
        dest.writeString(pct_vm);
        dest.writeString(key);
        dest.writeString(markList);
    }
}
