package com.easemob.helpdesk.adapter.manager;

import android.graphics.drawable.Drawable;

/**
 * Created by tiancruyff on 2017/4/19.
 */

public class RealTimeBaseAdapterItem {
	private String value;
	private String name;
	private int index;
	private Drawable img;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Drawable getImg() {
		return img;
	}

	public void setImg(Drawable img) {
		this.img = img;
	}
}
