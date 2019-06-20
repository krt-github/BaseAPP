package com.krt.basemodule.debug;

import android.util.Log;

import com.krt.base.BuildConfig;

/**
 * @author KRT
 * 2018/11/20
 */
public class Debug {
    private static final boolean ENABLE_DEBUG = BuildConfig.DEBUG;

    private Debug() {}

    public static boolean isEnableDebug() {
        return ENABLE_DEBUG;
    }

    public static void d(String tag, Object msg) {
        if (isEnableDebug()) {
            Log.d(tag, checkNull(msg));
        }
    }

    public static void i(String tag, Object msg) {
        if (isEnableDebug()) {
            Log.i(tag, checkNull(msg));
        }
    }

    public static void e(String tag, Object msg) {
        if (isEnableDebug()) {
            Log.e(tag, checkNull(msg));
        }
    }

    private static String checkNull(Object msg) {
        return null == msg ? "[NULL]" : msg.toString();
    }

}
