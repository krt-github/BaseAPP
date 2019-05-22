package com.krt.http_retrofit;

import io.reactivex.disposables.Disposable;

/**
 * @author KRT
 * 2018/11/22
 */
public abstract class DefaultObserverCallback<T> implements IObserverCallback<T> {
    private Disposable disposable;

    @Override
    public final void onSubscribe(Disposable d) {
        disposable = d;
        onStart(d);
    }

    @Override
    public final void onNext(T t) {
        onResponse(t);
    }

    public abstract void onStart(Disposable d);

    public abstract void onResponse(T response);

    @Override
    public abstract void onError(Throwable e);

    @Override
    public abstract void onComplete();

    @Override
    public final void onFinally(){
        boolean isCancelByUser = null != disposable && disposable.isDisposed();
        onFinally(isCancelByUser);
        disposable = null;
    }

    public abstract void onFinally(boolean isCancelByUser);

}
