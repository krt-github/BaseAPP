package com.krt.basemodule.http;

import com.krt.basemodule.mvp.IBaseView;
import com.krt.http_retrofit.DefaultObserverCallback;

/**
 * @author KRT
 * 2018/11/29
 */
public abstract class MVPObserverCallback<T> extends DefaultObserverCallback<T> {
    private IBaseView mBaseView;

    public MVPObserverCallback(IBaseView view){
        mBaseView = view;
    }

    @Override
    public void onComplete() {
        if(null != mBaseView){
            mBaseView.clearErrorState();
        }
    }

    @Override
    public void onFinally(boolean isCancelByUser) {
        if(null != mBaseView){
            mBaseView.onUpdateViewFinally();
        }
    }
}
