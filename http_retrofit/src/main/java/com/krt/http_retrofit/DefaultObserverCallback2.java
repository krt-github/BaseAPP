package com.krt.http_retrofit;

import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableObserver;

/**
 * @author KRT
 * 2018/11/22
 */
public abstract class DefaultObserverCallback2<T, E> extends DisposableObserver<T>
                                                    implements IObserverCallback<T, E> {
    private E convertedResponse;
    private boolean disposedByUser = false;

    @Override
    public int getStartDelayMS() {
        return 0;
    }

    @Override
    public IRetryPredicate<Throwable> getRetryPredicate() {
        return null;
    }

//    @Override
//    public final void onSubscribe(Disposable d) {
//        onStart(d);
//    }
//
//    public void onStart(Disposable disposable){}

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

    public E doResponseInBackground(T response){
        return null;
    }

    /**
     * Work on main thread
     * @param t
     */
    @Override
    public final void onNext(T t) {
        try {
            onResponse(t, convertedResponse);
        }catch(Exception e){
            e.printStackTrace();
            onError(e);
        }
    }

    public abstract void onResponse(T response, E convertedResponse);

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

    @Override
    public final Action getDisposeAction() {
        return new Action() {
            public void run() throws Exception {
                disposedByUser = true;
            }
        };
    }

    /**
     * Action doFinally, work on main thread
     * @throws Exception
     */
    @Override
    public final void run() throws Exception {
        onFinally();
    }

    public final void onFinally(){
        onFinally(disposedByUser);
        convertedResponse = null;
    }

    public void onFinally(boolean isCancelByUser){}

}
