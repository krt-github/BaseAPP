package com.krt.http_retrofit;

import io.reactivex.functions.Predicate;

public interface IRetryPredicate<P> extends Predicate<P> {
    int getRetryCount();
}
