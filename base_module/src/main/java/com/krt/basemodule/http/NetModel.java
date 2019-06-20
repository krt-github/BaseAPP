package com.krt.basemodule.http;

import com.google.gson.Gson;
import com.krt.basemodule.debug.Debug;
import com.krt.basemodule.mvp.IBaseModel;
import com.krt.http_retrofit.DefaultObserverCallback2;
import com.krt.http_retrofit.bean.Request;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author KRT
 * 2018/11/22
 */
public abstract class NetModel<A> implements IBaseModel {
    private final String TAG = getClass().getSimpleName();

    private A mAPI;
    private final List<Disposable> mDisposableList = new ArrayList<>();

    public NetModel(Class<A> service){
        mAPI = RetrofitUtils.getAPI(service, getBaseUrl());
    }

    protected abstract String getBaseUrl();

    public A getAPI(){
        return mAPI;
    }

    public <T> void request(Observable<T> observable, Observer<T> callback){
        if(null == observable || null == callback){
            return;
        }
        callback = wrapper(callback);
        RetrofitUtils.request(observable, callback);
    }

    /**
     * Proxy callback for record disposable and remove disposable.
     * <br>
     * Cancel request need disposable, so we need record request disposable.
     * <br>
     * If request was executed completed(not cancel by disposable),
     * that means {@link #mDisposableList} need remove disposable by manually,
     * but we didn't known what time request was completed,
     * so we listen this call by proxy.
     * @param oriCallback original callback
     * @param <T> callback response bean type
     * @return proxy instance if oriCallback is instanceof {@link DefaultObserverCallback2}
     */
    private <T> Observer<T> wrapper(final Observer<T> oriCallback){
        if(oriCallback instanceof DefaultObserverCallback2){
            final DefaultObserverCallback2 tempCallback = (DefaultObserverCallback2) oriCallback;
            recordDisposable(tempCallback);
            Object proxyInstance = Proxy.newProxyInstance(
                    oriCallback.getClass().getClassLoader(),
                    DefaultObserverCallback2.class.getInterfaces(),
                    (proxy, method, args) -> {
                        if ("run".equals(method.getName())) { // "run" method means onFinally
                            removeDisposable(tempCallback);
                        }
                        return method.invoke(oriCallback, args);
                    });
            if(proxyInstance instanceof Observer){
                return (Observer<T>) proxyInstance;
            }
        }
        return oriCallback;
    }

    protected RequestBody createJsonRequestBody(Request request){
        String json = new Gson().toJson(request);
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
    }

    protected void cancel(Disposable disposable){
        RetrofitUtils.cancel(disposable);
        removeDisposable(disposable);
    }

    private void removeDisposable(Disposable disposable){
        mDisposableList.remove(disposable);
    }

    private void recordDisposable(Disposable disposable){
        mDisposableList.add(disposable);
    }

    private void cancelAll(){
        RetrofitUtils.cancel(mDisposableList);
        mDisposableList.clear();
    }

    @Override
    public void onDestroy() {
        cancelAll();
    }

    protected void printi(Object s){
        Debug.i(TAG, s);
    }

    protected void printe(Object s){
        Debug.e(TAG, s);
    }
}
