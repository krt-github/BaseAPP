package com.krt.basemodule.http;

import com.google.gson.Gson;
import com.krt.basemodule.mvp.IBaseModel;
import com.krt.http_retrofit.bean.Request;

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
    private A mAPI;
    protected final List<Disposable> mDisposableList = new ArrayList<>();

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
        RetrofitUtils.request(observable, callback);
    }

    protected RequestBody createJsonRequestBody(Request request){
        String json = new Gson().toJson(request);
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
    }

    protected void cancel(Disposable disposable){
        RetrofitUtils.cancel(disposable);
        mDisposableList.remove(disposable);
    }

    private void recordTask(Disposable disposable){
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
}
