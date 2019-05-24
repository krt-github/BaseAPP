package com.krt.http_retrofit;

import io.reactivex.Observer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * @author KRT
 * 2018/11/22
 */
public interface IObserverCallback<T> extends Observer<T>, Function<T, T>, Consumer<Throwable>, Action {
}
