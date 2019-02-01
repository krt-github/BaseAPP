package com.krt.basemodule.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.krt.base.R;
import com.krt.basemodule.utils.GlideConfig;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;

/**
 * @author KRT
 * 2018/11/20
 */
public class ImageUtils {

    private ImageUtils() {
    }

    public static void init(Context context){}

    public static void release(){}

    public static List<File> compressImageSync(Context context, String file) throws IOException {
        File thumb = new Compressor(context)
                .setMaxWidth(300)
                .setMaxHeight(320)
                .setQuality(30)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(context.getCacheDir().getAbsolutePath())
                .compressToFile(new File(file), System.nanoTime() + ".jpg");

        Compressor compress = new Compressor(context);
        compress.setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(context.getCacheDir().getAbsolutePath());
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file, options);
            int dW = options.outWidth;
            int dH = options.outHeight;
            float ratio = 1;
            int maxWidth = 1080;
            int maxHeight = 1920;
            if(dW > dH){
                if(dW > maxWidth){
                    ratio = (float)maxWidth / dW;
                    dW = maxWidth;
                    dH = (int)(dH * ratio);
                }
            }else{
                if(dH > maxHeight){
                    ratio = (float)maxHeight / dH;
                    dH = maxHeight;
                    dW = (int)(dW * ratio);
                }
            }
            compress.setMaxWidth(dW)
                    .setMaxHeight(dH);
        }catch(Exception e){
            e.printStackTrace();
        }
        ArrayList<File> res = new ArrayList<>();
        res.add(thumb);
        res.add(compress.compressToFile(new File(file)));
        return res;
    }

//    public static void loadGif(Context context, SketchView sketchImageView, String url){
//        Sketch.with(context).display(url, sketchImageView)
////                .decodeGifImage()
//                .commit();
//    }

    public static File downloadImage(Context context, String url) {
        try {
            return Glide.with(context)
                    .load(url)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadImage(ImageView imageView, @DrawableRes int resId) {
        Glide.with(imageView.getContext())
                .load(resId)
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, String url) {
        loadImage(imageView, url, R.mipmap.default_icon);
    }

    public static void loadImage(ImageView imageView, String url, @DrawableRes int defaultRes) {
        if (null == imageView) {
            return;
        }

        try {
            Glide.with(imageView.getContext())
                    .load(url)
                    .placeholder(defaultRes)
                    .centerCrop()
                    .error(defaultRes)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImage(ImageView imageView, int resId, @DrawableRes int defaultRes) {
        Glide.with(imageView.getContext())
                .load(resId)
                .placeholder(defaultRes)
                .centerCrop()
                .error(defaultRes)
                .into(imageView);
    }

    public static void loadCircleImage(ImageView imageView, String url) {
        if (null == imageView) {
            return;
        }

        try {
            Glide.with(imageView.getContext())
                    .load(url)
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(imageView) {
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getView().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            getView().setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadRadiusImage(ImageView imageView, String url, @DrawableRes int defaultRes, int radius) {
        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(defaultRes)
                .centerCrop()
                .error(defaultRes)
                .transform(new GlideRoundTransform(imageView.getContext(), radius))
                .into(imageView);
    }

    public static void loadImageAutoSize(ImageView imageView, String url,
                                         int maxWidth, int maxHeight, @DrawableRes int defaultRes) {
        if (null == imageView) {
            return;
        }

        try {
            Glide.with(imageView.getContext())
                    .load(url)
                    .asBitmap()
                    .placeholder(defaultRes)
                    .error(defaultRes)
                    .override(maxWidth, maxHeight)
                    .transform(new GlideRoundTransform(imageView.getContext(), 5))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class GlideRoundTransform extends BitmapTransformation {
        private float radius;

        public GlideRoundTransform(Context context, int dp) {
            super(context);
            this.radius = context.getResources().getDisplayMetrics().density * dp;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return roundCrop(pool, toTransform);
        }

        private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
            if (source == null)
                return null;

            Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
            canvas.drawRoundRect(rectF, radius, radius, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName() + Math.round(radius);
        }
    }

    /**
     * Just work with 3.7
     * @param context
     * @param url
     * @return
     */
    public static File getCacheFile(Context context, String url) {
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(originalKey);
        try {
            GlideConfig glideConfig = new GlideConfig();
            DiskLruCache diskLruCache = DiskLruCache.open(new File(glideConfig.getCachePath(context), glideConfig.getCacheDirName()),
                    1, 1, glideConfig.getCacheSize());
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class SafeKeyGenerator {
        private final LruCache<Key, String> loadIdToSafeHash = new LruCache<>(1000);

        String getSafeKey(Key key) {
            String safeKey;
            synchronized (loadIdToSafeHash) {
                safeKey = loadIdToSafeHash.get(key);
            }
            if (safeKey == null) {
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    key.updateDiskCacheKey(messageDigest);
                    safeKey = Util.sha256BytesToHex(messageDigest.digest());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (loadIdToSafeHash) {
                    loadIdToSafeHash.put(key, safeKey);
                }
            }
            return safeKey;
        }
    }

    private static class OriginalKey implements Key {

        private final String id;
        private final Key signature;

        public OriginalKey(String id, Key signature) {
            this.id = id;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            OriginalKey that = (OriginalKey) o;

            if (!id.equals(that.id)) {
                return false;
            }
            if (!signature.equals(that.signature)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {
            messageDigest.update(id.getBytes(STRING_CHARSET_NAME));
            signature.updateDiskCacheKey(messageDigest);
        }
    }
}
