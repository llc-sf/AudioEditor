package dev.audio.timeruler.multitrack;

import android.content.Context;

import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.MediaClock;

public class MultiMediaCodecAudioRenderer extends MediaCodecAudioRenderer {

    private final int index;

    public MultiMediaCodecAudioRenderer(int index, Context context, MediaCodecSelector mediaCodecSelector)
    {
        super(context, mediaCodecSelector);
        this.index = index;
    }

    @Override
    public MediaClock getMediaClock()
    {
        if (index == 0)
        {
            return super.getMediaClock();
        }
        return null;
    }



}
