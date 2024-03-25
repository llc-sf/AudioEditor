/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package dev.android.player.framework.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.beautycamera.stackblur.StackBlurManager;


/**
 * 图片工具
 */
public class ImageUtils {

    private static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static Drawable createBlurredScaleImageWithRadius(Bitmap original, Context context, int scale, int radius) {
//        MPUtils.triggerGC();
        float minScale = Math.max(original.getWidth(), original.getHeight()) / 128f;
        int w = (int) (original.getWidth() / minScale);
        int h = (int) (original.getHeight() / minScale);
        if (w == 0) {
            w = 1;
        }
        if (h == 0) {
            h = 1;
        }
        Bitmap out = Bitmap.createScaledBitmap(original, w, h, true);

        Bitmap blur = new StackBlurManager(out).process(radius);//FastBlur.blur(out, radius, false);
        recycleBitmap(out);
        return new BitmapDrawable(context.getResources(), blur);
    }

    public static Drawable createBlurredScaleImageWithRadius(Bitmap original, Context context, int scale, int radius, int coverColor) {
//        MPUtils.triggerGC();
        float minScale = Math.max(original.getWidth(), original.getHeight()) / 128f;
        int w = (int) (original.getWidth() / minScale);
        int h = (int) (original.getHeight() / minScale);
        if (w == 0) {
            w = 1;
        }
        if (h == 0) {
            h = 1;
        }
        Bitmap out = Bitmap.createScaledBitmap(original, w, h, true);
        Bitmap blur = new StackBlurManager(out).process(radius);//FastBlur.blur(out, radius, false);
        if (blur != null) {
            recycleBitmap(out);
        } else {
            blur = out;
        }

        Bitmap cover = addCover(blur, coverColor);
        recycleBitmap(blur);
        return new BitmapDrawable(context.getResources(), cover);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
//        MPUtils.triggerGC();
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * 得到圆角图片
     */
    public static Bitmap roundRectangleBitmap(Bitmap source, int rectWidth, int rectHeight, float cornerRadius) {
        int width = source.getWidth();
        int height = source.getHeight();
        Log.e("BitmapUtil", "RoundRectangleBitmap:(" + width + "," + height + ")");

        Bitmap out = Bitmap.createBitmap(rectWidth, rectHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(out);

        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0f, 0f, rectWidth, rectHeight);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        return out;
    }

    public static Bitmap addCover(Bitmap source, int coverColor) {
        int width = source.getWidth();
        int height = source.getHeight();
        Log.e("BitmapUtil", "RoundRectangleBitmap:(" + width + "," + height + ")");
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawBitmap(source, 0, 0, paint);

        paint.setColor(coverColor);
        canvas.drawRect(0, 0, width, height, paint);

        return out;
    }

    /**
     * 剪切图片
     */
    public static Bitmap cropCenter(Bitmap source, int width, int height) {
        float ratio = Math.max(width * 1.0f / source.getWidth(), height * 1.0f / source.getHeight());
        Bitmap src = source;
        if (ratio > 1.0f) {
            src = Bitmap.createScaledBitmap(source, (int) (source.getWidth() * ratio), (int) (source.getHeight() * ratio), false);
        }

        Bitmap out = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int left = (width - src.getWidth()) / 2;
        int top = (height - src.getHeight()) / 2;
        canvas.drawBitmap(src, left, top, paint);
        if (src != source) {
            recycleBitmap(src);
        }
        return out;
    }

    public static Bitmap cropBitmap(Bitmap source, Rect rect) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int left = -rect.left;
        int top = -rect.top;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(source, left, top, paint);
        return bitmap;
    }

    public static boolean bitmapValid(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return false;
        }
        return true;
    }

    public static void recycleBitmap(Bitmap artwork) {
        if (artwork == null || artwork.isRecycled()) {
            return;
        }
        artwork.recycle();
    }

    public static Bitmap mergeImage(Bitmap src, Bitmap src2, int translateLeft, int translateTop) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(src, 0, 0, paint);
        canvas.drawBitmap(src2, translateLeft, translateTop, paint);
        return bitmap;
    }

    public static Drawable createBlurredImageWithRadius(Bitmap bitmap, Activity activity, int radius) {
        return new BitmapDrawable(activity.getResources(), new StackBlurManager(bitmap).process(radius));
    }

    public static Bitmap createBlurredBitmapWithRadius(Bitmap src, int radius) {
        return new StackBlurManager(src).process(radius);
    }


    public static Bitmap createBitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 将Biamap 缩放到指定大小
     *
     * @param source
     * @param width
     * @param height
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap source, int width, int height) {
        int sw = source.getWidth();//原始图片宽度
        int sh = source.getHeight();//原始图片高度
        float scaleWidth = (float) width / sw;//宽度放缩比例
        float scaleHeight = (float) height / sh;//高度放缩比例


        //保持宽高比缩放，以长边为主
        float scaleRatio = Math.max(scaleHeight, scaleWidth);


        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatio, scaleRatio);
        return Bitmap.createBitmap(source, 0, 0, sw, sh, matrix, true);
    }
}
