package com.common.lib.camera.record;

import android.graphics.Bitmap;

/**
 * Created by gigabud on 17-3-6.
 */

public interface OnCameraUseListener {
    void takePicture(Bitmap bmp);

    void recordingEnd(String videoPath, int width, int height);
}
