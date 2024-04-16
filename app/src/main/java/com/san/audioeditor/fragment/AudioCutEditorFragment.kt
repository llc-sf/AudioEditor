package com.san.audioeditor.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
import com.san.audioeditor.databinding.FragmentAudioCutBinding
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback
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
import dev.audio.timeruler.timer.DialogTimerSetting
import dev.audio.timeruler.utils.format2Duration
import dev.audio.timeruler.weight.AudioCutEditorView
import dev.audio.timeruler.weight.AudioEditorConfig
import dev.audio.timeruler.weight.BaseAudioEditorView
import dev.audio.timeruler.weight.CutPieceFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AudioCutEditorFragment : BaseMVVMFragment<FragmentAudioCutBinding>() {


    companion object {
        const val TAG = "AudioCutFragment"
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentAudioCutBinding {
        return FragmentAudioCutBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioCutViewModel
    override fun initViewModel() {
        var song = arguments?.getSerializable(AudioCutActivity.PARAM_SONG)
        Log.i(TAG, "song: $song")
        if (song is Song) {
            mViewModel = AudioCutViewModel(arguments?.getSerializable(AudioCutActivity.PARAM_SONG) as Song)
        } else {
            activity?.finish()
        }
    }

    override fun initData() {
    }


    override fun initView() {
        super.initView()
        PlayerManager.playByPath(mViewModel.song.path)
        viewBinding.toolbar.ImmerseDesign()
        initTimeBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerManager.releasePlayer()
    }

    override fun startObserve() {
        mViewModel.audioCutState.observe(viewLifecycleOwner) {}
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


        val hours = (mViewModel.song.duration / (1000 * 60 * 60) % 24)
        val minutes = (mViewModel.song.duration / (1000 * 60) % 60)
        val seconds = (mViewModel.song.duration / 1000 % 60)
        val milliseconds = (mViewModel.song.duration % 1000)

        // 23:59:59 999
        calendar[Calendar.HOUR_OF_DAY] = hours
        calendar[Calendar.MINUTE] = minutes
        calendar[Calendar.SECOND] = seconds
        calendar[Calendar.MILLISECOND] = milliseconds
        var endTime = calendar.timeInMillis

        //一个手机宽度显示多长时间
        //        viewBinding.timeBar.setScreenSpanValue(TimeRulerBar.VALUE_1000_MS * 8)
        //        viewBinding.timeBar.setMode(BaseAudioEditorView.MODE_ARRAY[2])
        //        viewBinding.timeBar.setRange(startTime, endTime)

        viewBinding.timeBar.initConfig(AudioEditorConfig.Builder()
                                           .mode(BaseAudioEditorView.MODE_ARRAY[2])
                                           .startValue(startTime).endValue(endTime)
                                           .maxScreenSpanValue(mViewModel.song.duration.toLong())
                                           .build())
        viewBinding.durationTime.text = mViewModel.song.duration.toLong().format2Duration()
        viewBinding.durationTime1.text = (mViewModel.song.duration / 1000).toString()
        viewBinding.scale.text = viewBinding.timeBar.mMode.toString()

        viewBinding.timeBar.setOnCursorListener(object : BaseAudioEditorView.OnCursorListener {
            override fun onStartTrackingTouch(cursorValue: Long) {
                viewBinding.tvData.text = cursorDateFormat.format(Date(cursorValue))
            }

            override fun onProgressChanged(cursorValue: Long, fromeUser: Boolean) {
                viewBinding.tvData.text = cursorDateFormat.format(Date(cursorValue))
            }

            override fun onStopTrackingTouch(cursorValue: Long) {
                viewBinding.tvData.text = cursorDateFormat.format(Date(cursorValue))
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
            viewBinding.timeBar.setShowCursor(viewBinding.btnShowCursor.isSelected)
        }

        viewBinding.btnPlay.setOnClickListener {
            viewBinding.timeBar.setCursorTimeValue(viewBinding.timeBar.getCursorTimeValue() + 1000)
        }
        viewBinding.zoomIn.setOnClickListener {
            viewBinding.timeBar.zoomIn()
        }
        viewBinding.zoomOut.setOnClickListener {
            viewBinding.timeBar.zoomOut()
        }


        viewBinding.rgModel.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.model1 -> {
                    viewBinding.cutAdd.visibility = View.INVISIBLE
                    viewBinding.cutRemove.visibility = View.INVISIBLE
                    viewBinding.timeBar.switchCutMode(CutPieceFragment.CUT_MODE_SELECT)
                    viewBinding.trimAnchorLy.visibility = View.VISIBLE
                }

                R.id.model2 -> {
                    viewBinding.cutAdd.visibility = View.INVISIBLE
                    viewBinding.cutRemove.visibility = View.INVISIBLE
                    viewBinding.timeBar.switchCutMode(CutPieceFragment.CUT_MODE_DELETE)
                    viewBinding.trimAnchorLy.visibility = View.VISIBLE
                }

                R.id.model3 -> {
                    viewBinding.cutAdd.visibility = View.VISIBLE
                    viewBinding.cutRemove.visibility = View.VISIBLE
                    viewBinding.timeBar.switchCutMode(CutPieceFragment.CUT_MODE_JUMP)
                    viewBinding.trimAnchorLy.visibility = View.INVISIBLE
                }
            }
        }

        viewBinding.cutAdd.setOnClickListener {
            viewBinding.timeBar.cutAdd()
        }

        viewBinding.cutRemove.setOnClickListener {
            viewBinding.timeBar.cutRemove()
        }

        viewBinding.play.setOnClickListener {
            if (viewBinding.timeBar.getCurrentPosition() > 0) {
                PlayerManager.playWithSeek(viewBinding.timeBar.getCurrentPosition())
            } else {
                PlayerManager.play()
            }

        }
        viewBinding.pause.setOnClickListener {
            PlayerManager.pause()
        }

        viewBinding.trimStart.setOnClickListener {
            viewBinding.timeBar.trimStart()
        }

        viewBinding.trimEnd.setOnClickListener {
            viewBinding.timeBar.trimEnd()
        }

        PlayerManager.addProgressListener(object : PlayerProgressCallback {
            override fun onProgressChanged(position: Long, duration: Long) {
                if (isAdded) {
                    viewBinding.timeBar.setPlayerProgress(position, duration)
                }
            }
        })

        viewBinding.timeBar.addOnCutLineAnchorChangeListener(object :
                                                                 AudioCutEditorView.OnCutLineAnchorChangeListener {

            override fun onCutLineChange(start: Boolean, end: Boolean) {
                viewBinding.clpLeft.visibility = if (start) View.VISIBLE else View.INVISIBLE
                viewBinding.clpRight.visibility = if (end) View.VISIBLE else View.INVISIBLE
            }
        })

        viewBinding.timeBar.addOnTrimAnchorChangeListener(object :
                                                              AudioCutEditorView.OnTrimAnchorChangeListener {

            override fun onTrimChange(start: Boolean, end: Boolean) {
                viewBinding.trimStart.visibility = if (start) View.VISIBLE else View.INVISIBLE
                viewBinding.trimEnd.visibility = if (end) View.VISIBLE else View.INVISIBLE
            }
        })

        viewBinding.timeBar.addOnCutLineChangeListener(object :
                                                           AudioCutEditorView.OnCutLineChangeListener {

            override fun onCutLineChange(start: Long, end: Long) {
                viewBinding.cutStart.text = "${start.format2Duration()}"
                viewBinding.cutEnd.text = "${end.format2Duration()}"
                viewBinding.durationSelected.text = "${(end - start).format2Duration()}"
            }
        })

        viewBinding.timeBar.addCutModeChangeButtonEnableListener(object :
                                                                     AudioCutEditorView.CutModeChangeButtonEnableListener {


            override fun onCutModeChange(addEnable: Boolean, removeEnable: Boolean) {
                viewBinding.cutAdd.visibility = if (addEnable) View.VISIBLE else View.INVISIBLE
                viewBinding.cutRemove.visibility = if (removeEnable) View.VISIBLE else View.INVISIBLE
            }
        })

        viewBinding.cutStartMinus.setOnClickListener {
            viewBinding.timeBar.startCutMinus()
        }
        viewBinding.cutStartPlus.setOnClickListener {
            viewBinding.timeBar.startCutPlus()
        }
        viewBinding.cutEndMinus.setOnClickListener {
            viewBinding.timeBar.startEndMinus()
        }
        viewBinding.cutEndPlus.setOnClickListener {
            viewBinding.timeBar.startEndPlus()
        }
        viewBinding.cutStart.setOnClickListener { //end todo 片段不能交织
            viewBinding.timeBar.editTrimStart(parentFragmentManager)
        }
        viewBinding.cutEnd.setOnClickListener { //end todo 片段不能交织
            viewBinding.timeBar.editTrimEnd(parentFragmentManager)
        }


        viewBinding.timeBar.addOnPlayingLineChangeListener(object :
                                                               AudioCutEditorView.OnPlayingLineChangeListener {

            override fun onPlayingLineChange(value: Long) {
                viewBinding.currentPlayTime.text = value.format2Duration()
            }
        })

        viewBinding.clpLeft.setOnClickListener {
            viewBinding.timeBar.anchor2CutStartLine()
        }

        viewBinding.clpRight.setOnClickListener {
            viewBinding.timeBar.anchor2CutEndLine()
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

        WaveformOptions.getSampleFrom(requireContext(), mViewModel.song.path) {
            viewBinding.timeBar.setWaveform(Waveform(it.toList()), mViewModel.song.duration.toLong())
        }

        viewBinding.timeBar.setOnScaleChangeListener(object : OnScaleChangeListener {
            override fun onScaleChange(mode: Int) {
                when (mode) {
                    BaseAudioEditorView.MODE_UINT_100_MS -> {
                        viewBinding.scale.text = "0"
                    }

                    BaseAudioEditorView.MODE_UINT_500_MS -> {
                        viewBinding.scale.text = "1"
                    }

                    BaseAudioEditorView.MODE_UINT_1000_MS -> {
                        viewBinding.scale.text = "2"
                    }

                    BaseAudioEditorView.MODE_UINT_2000_MS -> {
                        viewBinding.scale.text = "3"
                    }

                    BaseAudioEditorView.MODE_UINT_3000_MS -> {
                        viewBinding.scale.text = "4"
                    }

                    BaseAudioEditorView.MODE_UINT_6000_MS -> {
                        viewBinding.scale.text = "5"
                    }

                }
            }
        })



        play(requireContext())
    }


    private fun play(context: Context) { // 创建 ExoPlayer 实例
        val player: SimpleExoPlayer = initExoPlayer(context)

        var dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.packageName)) // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource1 = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri("content://media/external/audio/media/5184"))
        player.playWhenReady = true
        player.setMediaSource(

            MergingMediaSource(audioSource1))
    }


    private fun initExoPlayer(mAppContext: Context): SimpleExoPlayer {
        val player = SimpleExoPlayer.Builder(mAppContext, MultiTrackRenderersFactory(2, mAppContext))
            .setTrackSelector(MultiTrackSelector()).build()
        player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
        player.playWhenReady = true
        player.setPlaybackParameters(PlaybackParameters(1f))
        return player
    }
}