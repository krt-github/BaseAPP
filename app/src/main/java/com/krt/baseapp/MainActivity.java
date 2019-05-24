package com.krt.baseapp;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.krt.basemodule.base.BaseActivity;
import com.krt.basemodule.http.NetModel;
import com.krt.basemodule.http.RetrofitUtils;
import com.krt.http_retrofit.DefaultObserverCallback;
import com.krt.http_retrofit.DefaultObserverCallback2;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import retrofit2.http.GET;

public class MainActivity extends BaseActivity {
    private static final String TAG = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.hello).setOnClickListener(v -> {
            init2();
        });
    }

    public class Info{
        @SerializedName("time_cost")
        public int timeCost;
        @SerializedName("file_name")
        public String fileName;
        @SerializedName("target_url")
        public String targetUrl;

        @Override
        public String toString() {
            return "Info{" +
                    "timeCost=" + timeCost +
                    ", fileName='" + fileName + '\'' +
                    ", targetUrl='" + targetUrl + '\'' +
                    '}';
        }
    }

    public static interface getInfo{
        @GET("file/222.json")
        Observable<Info> get();
    }

    private void init2(){
        NetModel netModel = new NetModel(getInfo.class){

            @Override
            protected String getBaseUrl() {
                return "http://192.168.1.222/";
            }
        };
        getInfo api = (getInfo) netModel.getAPI();
        netModel.request(api.get(),
                new DefaultObserverCallback<Info>() {
                    @Override
                    public void onStart(Disposable disposable) {
                        print("------onStart------");
                    }

                    @Override
                    public Info doResponseInBackground(Info response) {
                        print("------doResponseInBackground------");
                        return response;
                    }

                    @Override
                    public void doErrorInBackground(Throwable throwable) {
                        print("------doErrorInBackground------");
                    }

                    @Override
                    public void onComplete() {
                        print("------onComplete------");
                    }

                    @Override
                    public void onFinally(boolean isCancelByUser) {
                        print("------onFinally------" + isCancelByUser);
                    }

                    @Override
                    public void onResponse(Info response, Info convertedResponse) {
                        print("------onResponse------" + response);
                        print("------onResponse------" + convertedResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        print("------onError------");
                    }
                });
    }

    private void print(String s){
        Log.e(TAG, "[" + Thread.currentThread().getName() + "] -> " + s);
    }

}
