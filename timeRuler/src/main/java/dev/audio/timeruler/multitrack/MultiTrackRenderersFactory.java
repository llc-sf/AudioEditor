package dev.audio.timeruler.multitrack;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor;
import com.google.android.exoplayer2.audio.SonicAudioProcessor;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.util.ArrayList;
import java.util.List;

public class MultiTrackRenderersFactory extends DefaultRenderersFactory {
    private int audioTrackCnt;
    private Context context;

    private List<MultiMediaCodecAudioRenderer>audioSinkList= new ArrayList<>();
    private List<AudioSink> audioSinks= new ArrayList<>();

    public List<AudioSink> getAudioSinks() {
        return audioSinks;
    }

    public void setAudioSinks(List<AudioSink> audioSinks) {
        this.audioSinks = audioSinks;
    }

    public List<MultiMediaCodecAudioRenderer> getAudioSinkList() {
        return audioSinkList;
    }

    public void setAudioSinkList(List<MultiMediaCodecAudioRenderer> audioSinkList) {
        this.audioSinkList = audioSinkList;
    }

    public MultiTrackRenderersFactory(int audioTrackCnt, Context context) {
        super(context);
        this.audioTrackCnt = audioTrackCnt;
        this.context = context;
    }

    @Override
    protected void buildAudioRenderers(Context context, int extensionRendererMode, MediaCodecSelector mediaCodecSelector,
                                       boolean enableDecoderFallback, AudioSink audioSink, Handler eventHandler,
                                       AudioRendererEventListener eventListener, ArrayList<Renderer> out) {

        audioSinkList.clear();
        SonicAudioProcessor sonicAudioProcessor = new SonicAudioProcessor();
        DefaultAudioSink.DefaultAudioProcessorChain defaultAudioProcessorChain=  new DefaultAudioSink.DefaultAudioProcessorChain(
                new AudioProcessor[] {},
                new SilenceSkippingAudioProcessor(),
                sonicAudioProcessor);

        AudioSink myAudioSink =new DefaultAudioSink(
                AudioCapabilities.getCapabilities(context),
                defaultAudioProcessorChain,
                true,
                true,
                true);
        for (int i = 0; i < audioTrackCnt; i++) {

            MultiMediaCodecAudioRenderer multiMediaCodecAudioRenderer =
                    new MultiMediaCodecAudioRenderer(i, context,MediaCodecSelector.DEFAULT,eventHandler,eventListener,audioSink);
            out.add(multiMediaCodecAudioRenderer);
            audioSinkList.add(multiMediaCodecAudioRenderer);
            audioSinks.add(audioSink);

        }
    }



}
