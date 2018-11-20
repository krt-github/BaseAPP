package com.krt.basemodule.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * @author KRT
 * 2018/11/20
 */
public class ToastUtils {
    private static Toast mToast;

    private ToastUtils() {
        throw new AssertionError();
    }

    @SuppressLint("ShowToast")
    public static void init(Context context){
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static void show(@StringRes int resId){
        if(null != mToast){
            mToast.setText(resId);
            mToast.show();
        }
    }

    public static void show(CharSequence msg){
        if(null != mToast && null != msg && msg.length() > 0){
            mToast.setText(msg);
            mToast.show();
        }
    }
}
