package com.krt.http_retrofit;

import io.reactivex.disposables.Disposable;

/**
 * @author KRT
 * 2018/11/22
 */
public abstract class DefaultObserverCallback<T> implements IObserverCallback<T> {
    private Disposable disposable;
    private T convertedResponse;

    @Override
    public final void onSubscribe(Disposable d) {
        disposable = d;
        onStart(d);
    }

    public void onStart(Disposable disposable){}

    /**
     * Function<T, T>, map data, work on background thread
     * @param t
     * @return
     * @throws Exception
     */
    @Override
    public final T apply(T t) throws Exception {
        convertedResponse = doResponseInBackground(t);
        return t;
    }

    public T doResponseInBackground(T response){
        return response;
    }

    /**
     * Work on main thread
     * @param t
     */
    @Override
    public final void onNext(T t) {
        onResponse(t, convertedResponse);
    }

    public abstract void onResponse(T response, T convertedResponse);

    /**
     * Consumer<Throwable> doOnError, work on background thread
     * @param throwable
     * @throws Exception
     */
    @Override
    public final void accept(Throwable throwable) throws Exception {
        doErrorInBackground(throwable);
    }

    /**
     * NOTICE: This method maybe called more than once when retry strategy run.
     * @param throwable
     */
    public void doErrorInBackground(Throwable throwable){}

    @Override
    public abstract void onError(Throwable e);

    @Override
    public void onComplete(){}

    /**
     * Action doFinally, work on main thread
     * @throws Exception
     */
    @Override
    public final void run() throws Exception {
        onFinally();
    }

    public final void onFinally(){
        boolean isCancelByUser = null != disposable && disposable.isDisposed();
        onFinally(isCancelByUser);
        disposable = null;
        convertedResponse = null;
    }

    public void onFinally(boolean isCancelByUser){}

}
