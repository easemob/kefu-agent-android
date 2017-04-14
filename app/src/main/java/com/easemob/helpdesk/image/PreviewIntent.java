package com.easemob.helpdesk.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPagerActivity;

/**
 * Created by lyuzhao on 2015/12/17.
 */
@SuppressLint("ParcelCreator")
public final class PreviewIntent extends Intent {

    public PreviewIntent(Context packgeContext) {
        super(packgeContext, PhotoPagerActivity.class);
    }

    public void setCurrentItem(int currentItem) {
        this.putExtra(PhotoPagerActivity.EXTRA_CURRENT_ITEM, currentItem);
    }

    public void setPhotoPaths(ArrayList<String> photoPaths) {
        this.putStringArrayListExtra(PhotoPagerActivity.EXTRA_PHOTOS, photoPaths);
    }

}
