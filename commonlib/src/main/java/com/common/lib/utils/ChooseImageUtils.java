package com.common.lib.utils;

import android.app.Activity;
import android.content.Intent;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ui.ImageGridActivity;

/**
 * @author wodx521
 * @date on 2018/9/6
 */
public class ChooseImageUtils {

    public static void chooseImage(Activity activity, int requestCode) {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        //多选
        imagePicker.setMultiMode(false);
        //显示拍照按钮
        imagePicker.setShowCamera(false);
        //最多选择9张
        imagePicker.setSelectLimit(1);
        //不进行裁剪
        imagePicker.setCrop(false);

        Intent intent = new Intent(activity, ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void openCamera(Activity activity, int requestCode) {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        //多选
        imagePicker.setMultiMode(false);
        //显示拍照按钮
        imagePicker.setShowCamera(false);
        //最多选择9张
        imagePicker.setSelectLimit(1);
        //不进行裁剪
        imagePicker.setCrop(false);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true); // 是否是直接打开相机
        activity.startActivityForResult(intent, requestCode);
    }
}
