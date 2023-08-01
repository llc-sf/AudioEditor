package com.san.audioeditor;

import android.app.Application;

public class FFmpegApplication extends Application {

    public static FFmpegApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static FFmpegApplication getInstance() {
        return context;
    }

}
