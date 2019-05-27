package com.krt.http_retrofit;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author KRT
 * 2018/11/22
 */
public class RetrofitWrapper {
    /**
     * ms 避免网络请求过快，用户无法感知请求过程
     */
    private static final int DEFAULT_DELAY_FOR_REQUEST = 500;

    public static Retrofit getRetrofit(String baseUrl){
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }

    public static Retrofit getRetrofit(OkHttpClient client, String baseUrl){
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(client)
                .build();
    }

    public static <T> Observable<T> config(Observable<T> observable) {
        return config(observable, new RetryStrategy());
    }

    public static <T> Observable<T> config(Observable<T> observable, Predicate<Throwable> predicate){
        return config(observable, predicate, 3);
    }

    public static <T> Observable<T> config(Observable<T> observable, Predicate<Throwable> predicate,
                                           int retryCount){
        return observable.delay(DEFAULT_DELAY_FOR_REQUEST, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(retryCount, predicate);
    }

    public static <T, E> Observable<T> config(Observable<T> observable, IObserverCallback<T, E> callback){
        Observable<T> configObservable = observable;

        int startDelayMS = callback.getStartDelayMS();
        if(startDelayMS > 0){
            configObservable = configObservable.delay(startDelayMS, TimeUnit.MILLISECONDS);
        }

        configObservable = configObservable.subscribeOn(Schedulers.io())
                .map(callback)
                .doOnError(callback)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(callback.getDisposeAction())
                .doFinally(callback);

        IRetryPredicate<Throwable> retryPredicate = callback.getRetryPredicate();
        if(null != retryPredicate && retryPredicate.getRetryCount() > 0){
            return configObservable.retry(retryPredicate.getRetryCount(), retryPredicate);
        }

        return configObservable;
    }

    public static <T> void request(Observable<T> observable, Observer<T> callback){
        config(observable).subscribe(callback);
    }

    public static <T, E> void request(Observable<T> observable, IObserverCallback<T, E> callback){
        config(observable, callback).subscribe(callback);
    }

}
