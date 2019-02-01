package com.krt.basemodule.mvp;

import android.content.Context;
import android.support.annotation.StringRes;

/**
 * @author KRT
 * 2018/11/22
 */
public interface IBaseView {

    /**
     * 处理数据时的 loading 提示
     * @param tip tip
     */
    void onHandle(CharSequence tip);

    /**
     * 当请求被取消时回调，如 获取数据的必要参数错误
     * @param tip tip
     */
    void onCancel(CharSequence tip);

    /**
     * 每次获取数据后(成功、失败、取消)调用，处理数据请求后的清理逻辑，如 dismissLoading
     */
    void onUpdateViewFinally();

    /**
     * 数据获取成功后，清除错误状态 {@link com.krt.basemodule.error.ErrorType}
     */
    void clearErrorState();

    /**
     * 获取上下文对象
     * @return context
     */
    Context getContext();

    /**
     * 获取 Activity
     * @return activity
     */
    Context getActivity();

    void toast(CharSequence s);

    void toast(@StringRes int resId);
}
