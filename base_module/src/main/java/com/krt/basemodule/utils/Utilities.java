package com.krt.basemodule.utils;

import android.content.Context;

/**
 * @author KRT
 * 2018/11/22
 */
public class Utilities {

    public static void init(Context context){
        ToastUtils.init(context);
        NetUtils.init(context);
        TimeUtils.init(context);
        ImageUtils.init(context);
        EmojiUtils.init();
    }

    public static void release(){
        ToastUtils.release();
        NetUtils.release();
        TimeUtils.release();
        ImageUtils.release();
        EmojiUtils.release();
    }

}
