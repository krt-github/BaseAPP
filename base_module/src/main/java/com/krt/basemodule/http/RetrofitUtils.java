package com.krt.basemodule.http;

import com.krt.http_retrofit.IObserverCallback;
import com.krt.http_retrofit.RetrofitWrapper;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class RetrofitUtils {

    public static <A> A getAPI(Class<A> service, String baseUrl){
        return RetrofitWrapper.getRetrofit(baseUrl).create(service);
    }

    public static <T> void request(Observable<T> observable, Observer<T> callback){
        if(callback instanceof IObserverCallback){
            request(observable, (IObserverCallback<T, T>)callback);
        }else {
            RetrofitWrapper.request(observable, callback);
        }
    }

    public static <T, E> void request(Observable<T> observable, IObserverCallback<T, E> callback){
        RetrofitWrapper.request(observable, callback);
    }

    public static void cancel(Disposable disposable){
        if(null != disposable && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    public static void cancel(List<Disposable> disposableList){
        if(null != disposableList){
            for(Disposable d : disposableList){
                cancel(d);
            }
        }
    }

}
