package com.common.lib.camera;


import com.common.lib.camera.record.OnCameraUseListener;

/**
 * 相机操作的接口
 *
 * @author jerry
 * @date 2015-09-24
 */
public interface ICameraOperation {
    /**
     * 切换前置和后置相机
     */
    void switchCamera();

    /**
     * 切换闪光灯模式
     */
    void switchFlashMode();

    /**
     * 拍照
     */
    boolean takePicture();

    boolean takePicture(OnCameraUseListener onCameraUseListener);

    /**
     * 相机最大缩放级别
     *
     * @return
     */
    int getMaxZoom();

    /**
     * 设置当前缩放级别
     *
     * @param zoom
     */
    void setZoom(int zoom);

    /**
     * 获取当前缩放级别
     *
     * @return
     */
    int getZoom();

    /**
     * 释放相机
     */
    void releaseCamera();
}
