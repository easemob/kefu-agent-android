package com.easemob.helpdesk.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ShortCutEntity implements Parcelable{
	/**
	 * 
	 */
	public int tenantId;
	public int groupId;
	public long shortCutMessageId;
	public String message;
	public long createDateTime;
	public long lastUpdateDateTime;
	public boolean isEditable;

	public ShortCutEntity(){

	}

	protected ShortCutEntity(Parcel in) {
		tenantId = in.readInt();
		groupId = in.readInt();
		shortCutMessageId = in.readLong();
		message = in.readString();
		createDateTime = in.readLong();
		lastUpdateDateTime = in.readLong();
	}

	public static final Creator<ShortCutEntity> CREATOR = new Creator<ShortCutEntity>() {
		@Override
		public ShortCutEntity createFromParcel(Parcel in) {
			return new ShortCutEntity(in);
		}

		@Override
		public ShortCutEntity[] newArray(int size) {
			return new ShortCutEntity[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(tenantId);
		dest.writeInt(groupId);
		dest.writeLong(shortCutMessageId);
		dest.writeString(message);
		dest.writeLong(createDateTime);
		dest.writeLong(lastUpdateDateTime);
	}
}
