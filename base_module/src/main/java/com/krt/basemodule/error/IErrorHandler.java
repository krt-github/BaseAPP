package com.krt.basemodule.error;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author KRT
 * 2018/11/22
 */
public interface IErrorHandler {
    @IntDef({
            ERROR_TOAST_HANDLER,
            ERROR_DIALOG_HANDLER,
            ERROR_PAGE_HANDLER
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ErrorHandlerDef{}

    int ERROR_TOAST_HANDLER = 1;
    int ERROR_DIALOG_HANDLER = 2;
    int ERROR_PAGE_HANDLER = 3;

    @ErrorHandlerDef int getErrorHandlerType();
    void onNetError(CharSequence msg);
    void onServerError(CharSequence msg);
    void onDataError(CharSequence msg);
    void onErrorRemove();
    void onDestroy();
}
