package com.krt.basemodule.bean;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public class ChatInputMediaBean {
    public @StringRes int nameResId = 0;
    public String name;
    public @DrawableRes int iconResId;
    public Runnable clickEvent;

    public ChatInputMediaBean(){}

    public ChatInputMediaBean(@StringRes int nameResId, @DrawableRes int icon, Runnable click){
        this.nameResId = nameResId;
        this.iconResId = icon;
        this.clickEvent = click;
    }

    public ChatInputMediaBean(String name, @DrawableRes int icon, Runnable click){
        this.name = name;
        this.iconResId = icon;
        this.clickEvent = click;
    }
}
