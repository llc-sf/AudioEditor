package dev.audio.timeruler.demo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.Util
import com.masoudss.lib.utils.WaveformOptions
import dev.audio.timeruler.BaseMultiTrackAudioEditorView
import dev.audio.timeruler.R
import dev.audio.timeruler.bean.TimeBean
import dev.audio.timeruler.bean.VideoBean
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.databinding.ActivityTimeRulerBinding
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class TimeRulerActivity : AppCompatActivity() {
    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss")
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
        calendar[Calendar.MINUTE] = 5
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
            nowTime++
            binding.timeBar.cursorValue = System.currentTimeMillis() + 1000 * nowTime * 60
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

        //小米
//        WaveformOptions.getSampleFrom(this, "/storage/emulated/0/Music/QQ音乐/G.E.M. 邓紫棋,潘玮柏-死了都要爱 (Live).mp3") {
////            binding.waveformSeekBar.sample = it
//            binding.timeBar.setWaveform(Waveform(it.toList()))
//
//        }
        //mate9 /storage/emulated/0/Music/暗杠 - 童话镇.mp3
        //mate9 /storage/emulated/0/Music/暗杠,寅子 - 说书人.mp3

        //华为
        WaveformOptions.getSampleFrom(
            this,
            "/storage/emulated/0/Music/music/MushrooM（蘑菇兄弟） - Katie Sky-Monsters（MushrooM remix）.mp3"
        ) {
            binding.timeBar.setWaveform(Waveform(it.toList()))
        }


        play(this)
    }

    private fun initExoPlayer(mAppContext: Context): SimpleExoPlayer {
        val renderersFactory: RenderersFactory = DefaultRenderersFactory(mAppContext)
        val trackSelector: TrackSelector = DefaultTrackSelector(mAppContext)
        val loadControl: LoadControl = DefaultLoadControl()
        val bandwidthMeter = DefaultBandwidthMeter.Builder(mAppContext).build()
        val analyticsCollector = AnalyticsCollector(Clock.DEFAULT)
//        val player = SimpleExoPlayer.Builder(
//            mAppContext,
//            renderersFactory,
//            trackSelector,
//            loadControl,
//            bandwidthMeter,
//            analyticsCollector
//        ).build()
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
        var dataSourceFactory1 = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )
        var dataSourceFactory2 = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )
        // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource1 = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(
                "content://media/external/audio/media/519"
            )
        )
        val audioSource2 = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(
                "content://media/external/audio/media/520"
            )
        )
//        val audioSource3 = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
//            MediaItem.fromUri(
//                "content://media/external/audio/media/518"
//            )
//        )

        // 使用 ClippingMediaSource 来设置播放的时间段
        val clippedSource1 = ClippingMediaSource(
            audioSource1,
            10*1000000, 35*1000000,
        ) // 播放音频1的10-15秒

        val clippedSource2 = ClippingMediaSource(
            audioSource2,
            0, 35*1000000,
        ) // 播放音频2的5-10秒

        // 使用 MergingMediaSource 合并需要同时播放的音频片段
        val mergedSourceForFirst5Sec = MergingMediaSource(clippedSource1,clippedSource2)
//        player.prepare(mergedSourceForFirst5Sec)
//        var uri = getAudioUriFromPath(context, "/storage/emulated/0/Music/暗杠,寅子 - 说书人.mp3")
//        player.prepare(ExoMediaSourceHelper.getInstance(context).getMediaSource(uri.toString()))
        player.playWhenReady = true
        player.setMediaSource(mergedSourceForFirst5Sec);
        player.prepare()
//        player.play()


//        player.prepare(mergedSourceForFirst5Sec)
    }


    fun getAudioUriFromPath(context: Context, filePath: String): Uri? {
        val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            mediaUri, arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ", arrayOf(filePath),
            null
        )
        var uri: Uri? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                uri = Uri.withAppendedPath(mediaUri, id.toString())
            }
            cursor.close()
        }
        return uri.apply {
            Log.i("llc_uri", this.toString())
        }
    }


}