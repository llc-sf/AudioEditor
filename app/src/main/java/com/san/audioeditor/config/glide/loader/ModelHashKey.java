package com.san.audioeditor.config.glide.loader;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;
import java.util.Locale;

/**
 * 以Model Hashcode + View的宽 + View的高 值作为缓存键
 *
 * @param <T>
 */
public class ModelHashKey implements Key {

    private final Object mObj;

    //师傅区分不同View大小的缓存
    private boolean isDiffSize;

    private int mTargetWidth;

    private int mTargetHeight;

    public ModelHashKey(Object obj) {
        this.mObj = obj;
    }

    public ModelHashKey(Object obj, int width, int height) {
        this(obj, true, width, height);
    }

    private ModelHashKey(Object obj, boolean isDiffSize, int width, int height) {
        this.mObj = obj;
        this.isDiffSize = isDiffSize;
        this.mTargetWidth = width;
        this.mTargetHeight = height;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(getCacheKeyBytes());
    }


    private volatile byte[] cacheKeyBytes;

    private String getCacheKey() {
        if (isDiffSize) {
            return String.format(Locale.getDefault(), "Width:%d Height:%d Code:%d", mTargetWidth, mTargetHeight, mObj.hashCode());
        } else {
            return String.format(Locale.getDefault(), "Code:%d", mObj.hashCode());
        }

    }

    private byte[] getCacheKeyBytes() {
        if (cacheKeyBytes == null) {
            cacheKeyBytes = getCacheKey().getBytes(CHARSET);
        }
        return cacheKeyBytes;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof ModelHashKey) {
            ModelHashKey other = (ModelHashKey) o;
            return getCacheKey().equals(other.getCacheKey());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getCacheKey().hashCode();
    }
}