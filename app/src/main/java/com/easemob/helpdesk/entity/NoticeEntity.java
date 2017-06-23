package com.easemob.helpdesk.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liyuzhao on 16/3/10.
 */
public class NoticeEntity implements Parcelable{
    public String status;
    public String activity_id;
    public String tenant_id;
    public long created_at;
    public String feed_id;
    public String verb;
    public String actorId;
    public String actorObjectType;
    public String contentDetail;
    public String contentSummary;
    public String contentId;

    public NoticeEntity(){}

    protected NoticeEntity(Parcel in) {
        status = in.readString();
        activity_id = in.readString();
        tenant_id = in.readString();
        created_at = in.readLong();
        feed_id = in.readString();
        verb = in.readString();
        actorId = in.readString();
        actorObjectType = in.readString();
        contentDetail = in.readString();
        contentSummary = in.readString();
        contentId = in.readString();
    }

    public static final Creator<NoticeEntity> CREATOR = new Creator<NoticeEntity>() {
        @Override
        public NoticeEntity createFromParcel(Parcel in) {
            return new NoticeEntity(in);
        }

        @Override
        public NoticeEntity[] newArray(int size) {
            return new NoticeEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(activity_id);
        dest.writeString(tenant_id);
        dest.writeLong(created_at);
        dest.writeString(feed_id);
        dest.writeString(verb);
        dest.writeString(actorId);
        dest.writeString(actorObjectType);
        dest.writeString(contentDetail);
        dest.writeString(contentSummary);
        dest.writeString(contentId);
    }
}



