package com.krt.basemodule.error;

import com.krt.basemodule.utils.ToastUtils;

/**
 * @author KRT
 * 2018/11/22
 */
public class ErrorToastHandler implements IErrorHandler{

    @Override
    public int getErrorHandlerType() {
        return ERROR_TOAST_HANDLER;
    }

    @Override
    public void onNetError(CharSequence msg) {
        ToastUtils.show(msg);
    }

    @Override
    public void onServerError(CharSequence msg) {
        ToastUtils.show(msg);
    }

    @Override
    public void onDataError(CharSequence msg) {
        ToastUtils.show(msg);
    }

    @Override
    public void onErrorRemove() {}

    @Override
    public void onDestroy() {}

}
