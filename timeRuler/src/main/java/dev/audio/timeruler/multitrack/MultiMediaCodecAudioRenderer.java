package dev.audio.timeruler.multitrack;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.MediaClock;

public class MultiMediaCodecAudioRenderer extends MediaCodecAudioRenderer {

    private int index;

    public MultiMediaCodecAudioRenderer(int index, Context context, MediaCodecSelector mediaCodecSelector, Handler eventHandler, AudioRendererEventListener eventListener, AudioSink audioSink) {
//        super(context,mediaCodecSelector,eventHandler,eventListener,audioSink);
        super(context,mediaCodecSelector);
        this.index = index;
    }

    @Override
    public MediaClock getMediaClock() {
        if (index == 0) {
            return super.getMediaClock();
        }
        return null;
    }



}
