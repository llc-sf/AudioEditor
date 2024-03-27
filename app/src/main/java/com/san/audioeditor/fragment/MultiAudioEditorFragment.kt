package com.san.audioeditor.fragment

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.masoudss.lib.utils.WaveformOptions
import com.san.audioeditor.R
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.databinding.FragmentMutilAudioEditorBinding
import com.san.audioeditor.viewmodel.AudioCutViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.ImmerseDesign
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.bean.TimeBean
import dev.audio.timeruler.bean.VideoBean
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.listener.OnScaleChangeListener
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector
import dev.audio.timeruler.weight.BaseAudioEditorView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MultiAudioEditorFragment : BaseMVVMFragment<FragmentMutilAudioEditorBinding>() {


    companion object {
        const val TAG = "AudioCutFragment"
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentMutilAudioEditorBinding {
        return FragmentMutilAudioEditorBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioCutViewModel
    override fun initViewModel() {
        var song = arguments?.getSerializable(AudioCutActivity.PARAM_SONG)
        Log.i(TAG, "song: $song")
        if (song is Song) {
            mViewModel =
                AudioCutViewModel(arguments?.getSerializable(AudioCutActivity.PARAM_SONG) as Song)
        } else {
            activity?.finish()
        }
    }

    override fun initData() {
        super.initData()
    }


    override fun initView() {
        super.initView()
        viewBinding.toolbar.ImmerseDesign()
        initTimeBar()
    }

    override fun startObserve() {
        mViewModel.audioCutState.observe(viewLifecycleOwner) {
        }
    }


    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss:SSS")
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
//        viewBinding.timeBar.setScreenSpanValue(TimeRulerBar.VALUE_1000_MS * 8)
        //
        viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[2])
        viewBinding.timeBar.setRange(startTime, endTime)

        viewBinding.timeBar.setOnCursorListener(object :
            BaseAudioEditorView.OnCursorListener {
            override fun onStartTrackingTouch(cursorValue: Long) {

            }

            override fun onProgressChanged(cursorValue: Long, fromeUser: Boolean) {
                viewBinding.tvData.text = cursorDateFormat.format(Date(cursorValue))
            }

            override fun onStopTrackingTouch(cursorValue: Long) {

            }
        })

        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewBinding.timeBar.setScale(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        viewBinding.seekAreaOffset.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        viewBinding.btnDir.setOnClickListener {
            viewBinding.btnDir.isSelected = !viewBinding.btnDir.isSelected
            viewBinding.timeBar.setTickDirection(viewBinding.btnDir.isSelected)
        }

        viewBinding.btnShowCursor.setOnClickListener {
            viewBinding.btnShowCursor.isSelected = !viewBinding.btnShowCursor.isSelected
            viewBinding.timeBar.setShowCursor(viewBinding.btnShowCursor.isSelected);
        }

        viewBinding.btnPlay.setOnClickListener {
            viewBinding.timeBar.setCursorTimeValue(viewBinding.timeBar.getCursorTimeValue() + 1000)
        }
        viewBinding.rgScale.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.scale1 -> {
                    viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[0])
                }

                R.id.scale2 -> {
                    viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[1])
                }

                R.id.scale3 -> {
                    viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[2])
                }

                R.id.scale4 -> {
                    viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[3])
                }

                R.id.scale5 -> {
                    viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[4])
                }

                R.id.scale6 -> {
                    viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[5])
                }
            }

            viewBinding.rgModel.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.model1 -> {
                        R.id.radioGroup
                    }

                    R.id.model2 -> {

                    }

                    R.id.model3 -> {

                    }
                }
            }
        }
        setData()

    }

    //todo requireContext()
    private fun setData() {
        var videos = mutableListOf<VideoBean>()
        var testTime = System.currentTimeMillis()
        for (i in 1..5) {
            val video = VideoBean(testTime, testTime + i * 10 * 60 * 1000, i % 2 == 0)
            videos.add(video)
            testTime += i * 15 * 60 * 1000
        }
        val timeBean = TimeBean(videos)
        viewBinding.timeBar.setColorScale(timeBean)

        WaveformOptions.getSampleFrom(
            requireContext(),
            mViewModel.song.path
        ) {
            viewBinding.timeBar.setWaveform(Waveform(it.toList()))
        }

        viewBinding.timeBar.setOnScaleChangeListener(object : OnScaleChangeListener {
            override fun onScaleChange(mode: String) {
                when (mode) {
                    BaseAudioEditorView.MODE_UINT_100_MS -> {
                        viewBinding.rgScale.check(R.id.scale1)
                    }

                    BaseAudioEditorView.MODE_UINT_500_MS -> {
                        viewBinding.rgScale.check(R.id.scale2)
                    }

                    BaseAudioEditorView.MODE_UINT_1000_MS -> {
                        viewBinding.rgScale.check(R.id.scale3)
                    }

                    BaseAudioEditorView.MODE_UINT_2000_MS -> {
                        viewBinding.rgScale.check(R.id.scale4)
                    }

                    BaseAudioEditorView.MODE_UINT_3000_MS -> {
                        viewBinding.rgScale.check(R.id.scale5)
                    }

                    BaseAudioEditorView.MODE_UINT_6000_MS -> {
                        viewBinding.rgScale.check(R.id.scale6)
                    }

                }
            }
        })



        play(requireContext())
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
        player.playWhenReady = true
        player.setMediaSource(

            MergingMediaSource(
                audioSource1
            )
        )
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
}