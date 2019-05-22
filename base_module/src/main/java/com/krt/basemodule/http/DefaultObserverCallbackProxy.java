package com.krt.basemodule.http;

import com.krt.http_retrofit.DefaultObserverCallback;
import com.krt.http_retrofit.IObserverCallback;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

class DefaultObserverCallbackProxy<T> extends DefaultObserverCallback<T> {
    private Observer<T> realCallback;

    public DefaultObserverCallbackProxy(Observer<T> realCallback){
        this.realCallback = realCallback;
    }

    @Override
    public void onStart(Disposable d) {
        if(null != realCallback){
            realCallback.onSubscribe(d);
        }
    }

    @Override
    public void onResponse(T response) {
        if(null != realCallback){
            realCallback.onNext(response);
        }
    }

    @Override
    public void onError(Throwable e) {
        if(null != realCallback){
            realCallback.onError(e);
        }
    }

    @Override
    public void onComplete() {
        if(null != realCallback){
            realCallback.onComplete();
        }
    }

    @Override
    public void onFinally(boolean isCancelByUser) {
        if(realCallback instanceof DefaultObserverCallback){
            ((DefaultObserverCallback<T>) realCallback).onFinally(isCancelByUser);
        }else if(realCallback instanceof IObserverCallback) {
            ((IObserverCallback<T>) realCallback).onFinally();
        }
        realCallback = null;
    }
}
