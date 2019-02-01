package com.krt.basemodule.utils;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class EmojiUtils {

    public static void init(){
        EmojiManager.install(new IosEmojiProvider());
    }

    public static void release(){}

}
