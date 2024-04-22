package com.san.audioeditor.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.isVisible
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
import com.san.audioeditor.handler.FFmpegHandler
import com.san.audioeditor.storage.convertSong
import com.san.audioeditor.viewmodel.AudioCutViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.ImmerseDesign
import dev.audio.ffmpeglib.FFmpegApplication
import dev.audio.ffmpeglib.tool.FFmpegUtil
import dev.audio.ffmpeglib.tool.FileUtil
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.listener.OnScaleChangeListener
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback
import dev.audio.timeruler.timer.ExitDialog
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.format2Duration
import dev.audio.timeruler.utils.lastAudioFragmentBean
import dev.audio.timeruler.utils.nextAudioFragmentBean
import dev.audio.timeruler.utils.toSegmentsArray
import dev.audio.timeruler.weight.AudioCutEditorView
import dev.audio.timeruler.weight.AudioEditorConfig
import dev.audio.timeruler.weight.BaseAudioEditorView
import dev.audio.timeruler.weight.CutPieceFragment
import java.io.File
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

    fun onNewIntent(intent: Intent?) {
        if (intent != null) {
            val song = intent.getParcelableExtra<Song>(AudioCutActivity.PARAM_SONG)
            if (song != null) {
                mViewModel.song = song
                initTimeBar()
                PlayerManager.playWithSeek(0, 0)
                return
            }

            val audioFragmentBean = intent.getParcelableExtra<AudioFragmentBean>(AudioCutActivity.PARAM_AUDIO)
            if (audioFragmentBean != null && !TextUtils.isEmpty(audioFragmentBean.path)) {
                mViewModel.song = getSongInfo(requireContext(), audioFragmentBean.path!!) ?: return
                initTimeBar(false)
                PlayerManager.playWithSeek(0, 0)
                return
            }

        }

    }

    override fun initData() {
    }


    override fun initView() {
        super.initView()
        initToolbar()
        PlayerManager.playByPath(mViewModel.song.path)
        viewBinding.toolbar.ImmerseDesign()
        initTimeBar()
    }

    private var isConformed = false
    private var isCutLineMoved = false
    private fun initToolbar() {
        viewBinding.toolbar.setNavigationOnClickListener {
            if (!isCutLineMoved&&!isConformed) {
                activity?.finish()
            }else{
                showExitDialog()
            }
        }
    }

    private fun showExitDialog() {
        ExitDialog.show(parentFragmentManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerManager.releasePlayer()
    }

    override fun startObserve() {
        mViewModel.audioCutState.observe(viewLifecycleOwner) {}
    }


    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss:SSS")
    private fun initTimeBar(isSaveDta: Boolean = true) {
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
            viewBinding.timeBar.play()
        }
        viewBinding.pause.setOnClickListener {
            viewBinding.timeBar.pause()
        }

        viewBinding.trimStart.setOnClickListener {
            viewBinding.timeBar.trimStart()
        }

        viewBinding.trimEnd.setOnClickListener {
            viewBinding.timeBar.trimEnd()
        }

        PlayerManager.addProgressListener(object : PlayerProgressCallback {
            override fun onProgressChanged(currentWindowIndex: Int,
                                           position: Long,
                                           duration: Long) {
                if (isAdded) {
                    viewBinding.timeBar.onProgressChange(currentWindowIndex, position, duration)
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
                isCutLineMoved = true
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
        viewBinding.cutStart.setOnClickListener {
            viewBinding.timeBar.editTrimStart(parentFragmentManager)
        }
        viewBinding.cutEnd.setOnClickListener {
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

        viewBinding.confirm.setOnClickListener {
            isConformed = true
            audioDeal(mViewModel.song.path)
        }

        viewBinding.pre.setOnClickListener {
            if (datas.isNullOrEmpty() || viewBinding.timeBar.audioFragmentBean == null) {
                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                var last = datas.lastAudioFragmentBean(viewBinding.timeBar.audioFragmentBean!!)
                if (last == null) {
                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
                } else {
                    AudioCutActivity.open(requireContext(), last)
                }
            } //            Log.i("llc_action", "current = ${viewBinding.timeBar.audioFragmentBean}")
            //            if (datas.isNullOrEmpty() || viewBinding.timeBar.audioFragmentBean == null) {
            //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            //                Log.i("llc_action", "pre = null}")
            //            } else {
            //                if (viewBinding.timeBar.audioFragmentBean!!.index - 1 < 0) {
            //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            //                    Log.i("llc_action", "pre = null}")
            //                } else {
            //                    AudioCutActivity.open(requireContext(), datas[viewBinding.timeBar.audioFragmentBean!!.index - 1])
            //                    Log.i("llc_action", "pre =${datas[viewBinding.timeBar.audioFragmentBean!!.index - 1]}")
            //                }
            //            }
        }
        viewBinding.next.setOnClickListener {
            if (datas.isNullOrEmpty() || viewBinding.timeBar.audioFragmentBean == null) {
                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                var next = datas.nextAudioFragmentBean(viewBinding.timeBar.audioFragmentBean!!)
                if (next == null) {
                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
                } else {
                    AudioCutActivity.open(requireContext(), next)
                }
            } //            if (datas.isNullOrEmpty() || viewBinding.timeBar.audioFragmentBean == null) {
            //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            //            } else {
            //                if (datas.size >= viewBinding.timeBar.audioFragmentBean!!.index + 1) {
            //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            //                } else {
            //                    AudioCutActivity.open(requireContext(), datas[viewBinding.timeBar.audioFragmentBean!!.index + 1])
            //                }
            //            }
        }
        setAudioData()
        if (isSaveDta) {
            addData(viewBinding.timeBar.audioFragmentBean)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == 10000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 已获得权限，可以访问外部存储中的文件
            } else { // 用户拒绝了权限请求，处理相应逻辑
            }
        }
    }


    //todo requireContext()
    private fun setAudioData() {

        WaveformOptions.getSampleFrom(requireContext(), mViewModel.song.path) {
            viewBinding.timeBar.setWaveform(Waveform(it.toList()), mViewModel.song.duration.toLong(), mViewModel.song.path)
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


    var outputPath: String = ""
    private var cutFileName = "cut"
    private var suffix: String? = null
    private val PATH = FFmpegApplication.instance?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
        ?: ""

    private fun audioDeal(srcFile: String) {
        var realCutPieceFragments = viewBinding.timeBar.cutPieceFragmentsOrder?.filter { !it.isFake }
        if (realCutPieceFragments.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "请先选择片段", Toast.LENGTH_SHORT).show()
            return
        }
        var commandLine: Array<String>? = null
        if (!FileUtil.checkFileExist(srcFile)) {
            return
        }
        if (!FileUtil.isAudio(srcFile)) {
            return
        }
        suffix = FileUtil.getFileSuffix(srcFile)
        if (suffix.isNullOrEmpty()) {
            return
        }

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(srcFile)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val totalDuration = (durationStr?.toFloat() ?: 0F) / 1000 // 转换为秒
        cutFileName = "cut" + (System.currentTimeMillis())
        outputPath = PATH + File.separator + cutFileName + suffix

        commandLine = FFmpegUtil.cutMultipleAudioSegments(srcFile, realCutPieceFragments.toSegmentsArray(), outputPath)

        android.util.Log.i(BaseAudioEditorView.jni_tag, "outputPath=${outputPath}") //打印 commandLine
        var sb = StringBuilder()
        commandLine?.forEachIndexed { index, s ->
            sb.append("$s ")
            android.util.Log.i(BaseAudioEditorView.jni_tag, "s=$s")
        }
        android.util.Log.i(BaseAudioEditorView.jni_tag, "sb=$sb")



        if (ffmpegHandler != null && commandLine != null) {
            ffmpegHandler!!.executeFFmpegCmd(commandLine)
        }
    }

    private var ffmpegHandler: FFmpegHandler? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ffmpegHandler = FFmpegHandler(mHandler)
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                FFmpegHandler.MSG_BEGIN -> {
                    Log.i(BaseAudioEditorView.jni_tag, "begin")
                    viewBinding.progressLy.isVisible = true
                    viewBinding.progressText.text = "0%"
                }

                FFmpegHandler.MSG_FINISH -> {
                    Log.i(BaseAudioEditorView.jni_tag, "finish resultCode=${msg.obj}")
                    viewBinding.progressLy.isVisible = false
                    if (msg.obj == 0) {
                        var file = AudioFileUtils.copyAudioToFileStore(File(outputPath), requireContext(), cutFileName + suffix)
                        if (file != null) {
                            AudioFileUtils.notifyMediaScanner(requireContext(), file.absolutePath) { path: String, uri: Uri ->
                                val song = getSongInfo(requireContext(), path)
                                if (song != null) {
                                    AudioCutActivity.open(requireContext(), song)
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "裁剪失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                FFmpegHandler.MSG_PROGRESS -> {
                    val progress = msg.arg1
                    Log.i(BaseAudioEditorView.jni_tag, "progress=$progress")
                    viewBinding.progressText.text = "$progress%"
                }

                FFmpegHandler.MSG_INFO -> {
                    Log.i(BaseAudioEditorView.jni_tag, "${msg.obj}")
                }

                else -> {
                }
            }
        }
    }


    fun getSongInfo(context: Context, songPath: String): Song? {
        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "=?", arrayOf(songPath), null)
        if (cursor?.moveToNext() == true) {
            return cursor.convertSong()
        }
        return null
    }

    private var datas: MutableList<AudioFragmentBean> = mutableListOf()

    //注意调用时机 todo
    fun addData(audioFragmentBean: AudioFragmentBean?) {
        audioFragmentBean?.let {
            Log.i("llc_action", "addData = ${it}")
            datas.add(it)
        }
    }


}