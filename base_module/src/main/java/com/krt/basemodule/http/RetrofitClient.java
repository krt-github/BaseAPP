package com.krt.basemodule.http;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author KRT
 * 2018/11/20
 */
public class RetrofitClient {

    public RetrofitClient() {}

    public OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Log信息拦截器
//        if(Debug.isEnableDebug()){
//            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
//
//            builder.addInterceptor(new LoggingInterceptor())
//                    .addInterceptor(loggingInterceptor);
//        }

        //cache
//        File httpCacheDir = new File(MyApp.getContext().getCacheDir(), "response");
//        int cacheSize = 10 * 1024 * 1024;// 10 MiB
//        Cache cache = new Cache(httpCacheDir, cacheSize);
//        builder.cache(cache);

        //cookie
//        ClearableCookieJar cookieJar =
//                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApp.getContext()));
//        builder.cookieJar(cookieJar);

//        return builder.addInterceptor(rewriteHeaderControlInterceptor)
//                .addInterceptor(rewriteCacheControlInterceptor)

        return builder.connectTimeout(5, TimeUnit.SECONDS)//设置超时
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//                .addInterceptor(new TimeoutInterceptor())
//                .retryOnConnectionFailure(true)//错误重连
                .build();
    }

    //header配置
    private Interceptor rewriteHeaderControlInterceptor = chain -> {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")
//                .addHeader("Content-Type", "application/json; charset=utf-8")
//                .addHeader("Accept-Encoding", "gzip, deflate")
//                .addHeader("Connection", "keep-alive")
//                .addHeader("Accept", "*/*")
//                .addHeader("Cookie", "add cookies here")
                .build();
        return chain.proceed(request);
    };

    //cache配置
    private Interceptor rewriteCacheControlInterceptor = chain -> {

        //通过 CacheControl 控制缓存数据
        CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        cacheBuilder.maxAge(0, TimeUnit.SECONDS);//这个是控制缓存的最大生命时间
        cacheBuilder.maxStale(365, TimeUnit.DAYS);//这个是控制缓存的过时时间
        CacheControl cacheControl = cacheBuilder.build();

        //设置拦截器
        Request request = chain.request();
//        if (!NetUtils.isConnected(MyApp.getContext())) {
//            request = request.newBuilder()
//                    .cacheControl(cacheControl)
//                    .build();
//        }
//
//        Response originalResponse = chain.proceed(request);
//        if (NetUtils.isConnected(MyApp.getContext())) {
//            int maxAge = 0;//read from cache
//            return originalResponse.newBuilder()
//                    .removeHeader("Pragma")
//                    .header("Cache-Control", "public ,max-age=" + maxAge)
//                    .build();
//        } else {
//            int maxStale = 60 * 60 * 24 * 28;//tolerate 4-weeks stale
//            return originalResponse.newBuilder()
//                    .removeHeader("Prama")
//                    .header("Cache-Control", "poublic, only-if-cached, max-stale=" + maxStale)
//                    .build();
//        }
        return null;
    };

    private class LoggingInterceptor implements Interceptor {
        public Response intercept(Chain chain) throws IOException {
            //这个chain里面包含了request和response，所以你要什么都可以从这里拿
            Request request = chain.request();
            long t1 = System.nanoTime();//请求发起的时间
            Log.e("test", String.format("发送请求 %s on %s%n%s", request.url(), chain.connection(), request.headers()));
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();//收到响应的时间
            //这里不能直接使用response.body().string()的方式输出日志
            //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一
            //个新的response给应用层处理
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            Log.e("test", String.format("接收响应: [%s] %n返回json:【%s】 %.1fms%n%s",
                    response.request().url(),
                    responseBody.string(),
                    (t2 - t1) / 1e6d,
                    response.headers()));
            return response;
        }
    }
}
