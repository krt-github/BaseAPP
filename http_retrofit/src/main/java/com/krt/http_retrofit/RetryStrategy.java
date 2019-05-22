package com.krt.http_retrofit;

import io.reactivex.functions.Predicate;

/**
 * @author KRT
 * 2018/11/22
 */
public class RetryStrategy implements Predicate<Throwable> {

    protected boolean canRetry(Throwable throwable){
        return true;
    }

    @Override
    public final boolean test(Throwable throwable) throws Exception {
        return canRetry(throwable);
    }
}
