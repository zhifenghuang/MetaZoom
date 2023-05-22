package com.common.lib.utils;

import android.util.Log;

public class LogUtil {

    public static final boolean DEBUG_MODE = true;

    public static final String TAG = "MetaZoom_";

    public static void LogE(Object object) {
        if (DEBUG_MODE) {
            Log.e(TAG, object == null ? "null" : String.valueOf(object));
        }
    }
}
