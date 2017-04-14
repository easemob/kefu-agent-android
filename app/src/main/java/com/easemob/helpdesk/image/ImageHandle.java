package com.easemob.helpdesk.image;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by lyuzhao on 2015/12/17.
 */
public interface ImageHandle {

    String IMAGE_PATHS = "image_paths";

    /**
     * 选择一张图片
     * @param context
     * @param showTaskPhotoItem 是否有拍照选项
     * @return
     */
    Intent pickSingleImage(Context context, boolean showTaskPhotoItem);

    /**
     *  选择多张图片
     * @param context
     * @param ImageCount 选择照片数目
     * @param showTaskPhotoItem 是否有拍照选项
     * @return
     */
    Intent pickMultiImage(Context context, int ImageCount, boolean showTaskPhotoItem);

    /**
     * 预览一组图片
     * @param context
     * @param photoPaths 图片的绝对地址
     * @return
     */
    Intent previewImage(Context context, ArrayList<String> photoPaths);


}
