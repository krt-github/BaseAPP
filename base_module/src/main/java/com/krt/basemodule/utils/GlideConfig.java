package com.krt.basemodule.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

import java.io.File;

public class GlideConfig implements GlideModule {
    private final int memorySize = (int) (Runtime.getRuntime().maxMemory()) / 8;  // 取1/8最大内存作为最大缓存

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
        builder.setMemoryCache(new LruResourceCache(memorySize));

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        //修改bitmappool的大小为默认的1.2倍
        int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));

        String cacheDir = getCachePath(context);//指定的是数据的缓存地址
        int diskCacheSize = getCacheSize();//最多可以缓存多少字节的数据 400M
        //设置磁盘缓存大小
        builder.setDiskCache(new DiskLruCacheFactory(cacheDir, getCacheDirName(), diskCacheSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

    public String getCachePath(Context context){
        File externalCacheDir = context.getExternalCacheDir();
        if(null != externalCacheDir){
            return externalCacheDir.getAbsolutePath();
        }else{
            return context.getCacheDir().getAbsolutePath();
        }
    }

    public String getCacheDirName(){
        return "glide";
    }

    public int getCacheSize(){
        return 1024 * 1024 * 400;
    }
}
