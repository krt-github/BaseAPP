package com.krt.http_retrofit;

import io.reactivex.Observer;

/**
 * @author KRT
 * 2018/11/22
 */
public interface IObserverCallback<T> extends Observer<T> {
    void onFinally();
}
