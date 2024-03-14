package dev.audio.timeruler.demo

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SilenceMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.masoudss.lib.utils.WaveformOptions
import dev.audio.timeruler.BaseMultiTrackAudioEditorView
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.MODE_UINT_1000_MS
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.MODE_UINT_100_MS
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.MODE_UINT_2000_MS
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.MODE_UINT_3000_MS
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.MODE_UINT_500_MS
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.MODE_UINT_6000_MS
import dev.audio.timeruler.R
import dev.audio.timeruler.bean.TimeBean
import dev.audio.timeruler.bean.VideoBean
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.databinding.ActivityTimeRulerBinding
import dev.audio.timeruler.listener.OnScaleChangeListener
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class TimeRulerActivity : AppCompatActivity() {
    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss:SSS")
    var nowTime = 1
    private lateinit var binding: ActivityTimeRulerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeRulerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initTimeBar()
    }

    private fun initTimeBar() {
        val calendar = Calendar.getInstance()

        // 00:00:00 000
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startTime = calendar.timeInMillis

        // 23:59:59 999
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 10
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var endTime = calendar.timeInMillis

        //一个手机宽度显示多长时间
//        binding.timeBar.setScreenSpanValue(TimeRulerBar.VALUE_1000_MS * 8)
        //
        binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[2])
        binding.timeBar.setRange(startTime, endTime)
        binding.timeBar.cursorValue = System.currentTimeMillis()

        binding.timeBar.setOnCursorListener(object :
            BaseMultiTrackAudioEditorView.OnCursorListener {
            override fun onStartTrackingTouch(cursorValue: Long) {

            }

            override fun onProgressChanged(cursorValue: Long, fromeUser: Boolean) {
                binding.tvData.text = cursorDateFormat.format(Date(cursorValue))
            }

            override fun onStopTrackingTouch(cursorValue: Long) {

            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.timeBar.setScale(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.seekAreaOffset.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.timeBar.setVideoAreaOffset(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.btnDir.setOnClickListener {
            binding.btnDir.isSelected = !binding.btnDir.isSelected
            binding.timeBar.setTickDirection(binding.btnDir.isSelected)
        }

        binding.btnShowCursor.setOnClickListener {
            binding.btnShowCursor.isSelected = !binding.btnShowCursor.isSelected
            binding.timeBar.setShowCursor(binding.btnShowCursor.isSelected);
        }

        binding.btnPlay.setOnClickListener {
            binding.timeBar.cursorValue = binding.timeBar.cursorValue + 1000
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb1 -> {
                    binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[0])
                }

                R.id.rb2 -> {
                    binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[1])
                }

                R.id.rb3 -> {
                    binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[2])
                }

                R.id.rb4 -> {
                    binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[3])
                }

                R.id.rb5 -> {
                    binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[4])
                }

                R.id.rb6 -> {
                    binding.timeBar.setMode(BaseMultiTrackAudioEditorView.MODE_ARRAY[5])
                }
            }
        }
        setData()

    }

    private fun setData() {
        var videos = mutableListOf<VideoBean>()
        var testTime = System.currentTimeMillis()
        for (i in 1..5) {
            val video = VideoBean(testTime, testTime + i * 10 * 60 * 1000, i % 2 == 0)
            videos.add(video)
            testTime += i * 15 * 60 * 1000
        }
        val timeBean = TimeBean(videos)
        binding.timeBar.setColorScale(timeBean)

        WaveformOptions.getSampleFrom(
            this,
            "/storage/emulated/0/Music/QQ音乐/Blow Fever,Vinida万妮达-No Day Off (Live).mp3"
        ) {
            binding.timeBar.setWaveform(Waveform(it.toList()))
        }

        binding.timeBar.setOnScaleChangeListener(object : OnScaleChangeListener {
            override fun onScaleChange(mode: String) {
                when (mode) {
                    MODE_UINT_100_MS -> {
                        binding.radioGroup.check(R.id.rb1)
                    }

                    MODE_UINT_500_MS -> {
                        binding.radioGroup.check(R.id.rb2)
                    }

                    MODE_UINT_1000_MS -> {
                        binding.radioGroup.check(R.id.rb3)
                    }

                    MODE_UINT_2000_MS -> {
                        binding.radioGroup.check(R.id.rb4)
                    }

                    MODE_UINT_3000_MS -> {
                        binding.radioGroup.check(R.id.rb5)
                    }

                    MODE_UINT_6000_MS -> {
                        binding.radioGroup.check(R.id.rb6)
                    }

                }
            }
        })


        play(this)
    }

    private fun initExoPlayer(mAppContext: Context): SimpleExoPlayer {
        val player = SimpleExoPlayer.Builder(
            mAppContext,
            MultiTrackRenderersFactory(
                2,
                mAppContext
            )
        ).setTrackSelector(MultiTrackSelector()).build()
        player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
        player.playWhenReady = true
        player.setPlaybackParameters(PlaybackParameters(1f))
        return player
    }


    private fun play(context: Context) {

        // 创建 ExoPlayer 实例
        val player: SimpleExoPlayer = initExoPlayer(context)

        var dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )
        // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource1 = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(
                "content://media/external/audio/media/5184"
            )
        )
        val audioSource2 = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(
                "content://media/external/audio/media/3123"
            )
        )
        player.playWhenReady = true
        player.setMediaSource(

            MergingMediaSource(
                true,
//                SilenceMediaSource(30 * 1000000),
                ClippingMediaSource(
                    audioSource1,
                    20 * 1000000, 30 * 1000000,
                ),
                ClippingMediaSource(
                    audioSource2,
                    0 * 1000000, 10 * 1000000,
                )
            )
        )
//        player.prepare()

    }


}

//k30 content://media/external/audio/media/5184    /storage/emulated/0/Music/网易云音乐/8先生 - 汪苏泷-万有引力（抖音枪声版）（8先生 remix）.mp3
//k30 /storage/emulated/0/Music/网易云音乐/王力宏 - Can You Feel My World.mp3 content://media/external/audio/media/5142
//k30 content://media/external/audio/media/3123 /storage/emulated/0/Music/QQ音乐/Blow Fever,Vinida万妮达-No Day Off (Live).mp3
//mac pro content://media/external/audio/media/1000000035
//mac pro content://media/external/audio/media/1000000031
