package com.krt.basemodule.error;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author KRT
 * 2018/11/22
 */
public class ErrorType {
    @IntDef({
            NET_ERROR,
            SERVER_ERROR,
            DATA_ERROR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorTypeDef{}

    public static final int NET_ERROR = 1;
    public static final int SERVER_ERROR = 2;
    public static final int DATA_ERROR = 3;
}
