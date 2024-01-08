package dev.audio.timeruler.bean;

import android.graphics.Color;


import java.util.List;

import dev.audio.timeruler.TimeRulerBar;

public class TimeBean implements TimeRulerBar.ColorScale {
    List<VideoBean> videoBeans;

    public TimeBean(List<VideoBean> videoBeans) {
        this.videoBeans = videoBeans;
    }

    @Override
    public int getSize() {
        return videoBeans.size();
    }

    @Override
    public long getStart(int index) {
        return videoBeans.get(index).startTime;
    }

    @Override
    public long getEnd(int index) {
        return videoBeans.get(index).endTime;
    }

    @Override
    public int getColor(int index) {
        return videoBeans.get(index).isSos ? Color.RED : Color.GREEN;
    }
}
