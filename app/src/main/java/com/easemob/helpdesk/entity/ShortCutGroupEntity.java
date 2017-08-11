package com.easemob.helpdesk.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ShortCutGroupEntity implements Parcelable{

	public long shortcutMessageGroupId;
	public long tenantId;
	public String agentUserId;
	public String shortcutMessageGroupName;
	public String groupType;
	public ArrayList<ShortCutEntity> shortCutEntitys;

	public ShortCutGroupEntity(){

	}


	protected ShortCutGroupEntity(Parcel in) {
		shortcutMessageGroupId = in.readLong();
		tenantId = in.readLong();
		agentUserId = in.readString();
		shortcutMessageGroupName = in.readString();
		groupType = in.readString();
		shortCutEntitys = in.createTypedArrayList(ShortCutEntity.CREATOR);
	}

	public static final Creator<ShortCutGroupEntity> CREATOR = new Creator<ShortCutGroupEntity>() {
		@Override
		public ShortCutGroupEntity createFromParcel(Parcel in) {
			return new ShortCutGroupEntity(in);
		}

		@Override
		public ShortCutGroupEntity[] newArray(int size) {
			return new ShortCutGroupEntity[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(shortcutMessageGroupId);
		dest.writeLong(tenantId);
		dest.writeString(agentUserId);
		dest.writeString(shortcutMessageGroupName);
		dest.writeString(groupType);
		dest.writeTypedList(shortCutEntitys);
	}
}
