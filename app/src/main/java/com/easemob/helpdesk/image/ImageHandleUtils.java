package com.easemob.helpdesk.image;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import me.iwf.photopicker.utils.PhotoPickerIntent;

/**
 * 包装PhotoPicker library的工具类
 * Created by lyuzhao on 2015/12/17.
 */
public final class ImageHandleUtils {

    /**
     * 选择一张图片
     *
     * @param context
     * @param showTaskPhotoItem 是否有拍照选项
     * @return
     */
    public static Intent pickSingleImage(Context context, boolean showTaskPhotoItem) {
        PhotoPickerIntent intent = new PhotoPickerIntent(context);
        intent.setPhotoCount(1);
        intent.setShowCamera(showTaskPhotoItem);
        return intent;
    }

    /**
     * 选择多张图片
     *
     * @param context
     * @param ImageCount        选择照片数目
     * @param showTaskPhotoItem 是否有拍照选项
     * @return
     */
    public static Intent pickMultiImage(Context context, int ImageCount, boolean showTaskPhotoItem) {
        PhotoPickerIntent intent = new PhotoPickerIntent(context);
        intent.setPhotoCount(ImageCount);
        intent.setShowCamera(showTaskPhotoItem);
        return intent;
    }

    /**
     * 预览一组图片
     *
     * @param context
     * @param photoPaths  图片的绝对地址
     * @param currentItem 当前展示的图片order
     * @return
     */
    public static Intent previewImage(Context context, ArrayList<String> photoPaths, int currentItem) {
        PreviewIntent intent = new PreviewIntent(context);
        intent.setCurrentItem(currentItem);
        intent.setPhotoPaths(photoPaths);
        return intent;
    }


}
