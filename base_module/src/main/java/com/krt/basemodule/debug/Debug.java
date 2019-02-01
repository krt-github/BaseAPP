package com.krt.basemodule.debug;

import android.util.Log;

/**
 * @author KRT
 * 2018/11/20
 */
public class Debug {
    private static final boolean ENABLE_DEBUG = true;

    private Debug() {}

    public static boolean isEnableDebug() {
        return ENABLE_DEBUG;
    }

    public static void d(String tag, String msg) {
        if (isEnableDebug()) {
            Log.d(tag, checkNull(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (isEnableDebug()) {
            Log.i(tag, checkNull(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (isEnableDebug()) {
            Log.e(tag, checkNull(msg));
        }
    }

    private static String checkNull(String msg) {
        return null == msg ? "[NULL]" : msg;
    }

}
