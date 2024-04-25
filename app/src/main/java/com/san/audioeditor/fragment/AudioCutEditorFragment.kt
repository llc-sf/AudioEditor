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
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
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
import dev.audio.timeruler.timer.EditExitDialog
import dev.audio.timeruler.timer.EditLoadingDialog
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.format2DurationSimple
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
import kotlin.math.max

class AudioCutEditorFragment : BaseMVVMFragment<FragmentAudioCutBinding>(),
    EditLoadingDialog.OnCancelListener, Player.EventListener {


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
                freshSaveActions()
                return
            }

            val audioFragmentBean = intent.getParcelableExtra<AudioFragmentBean>(AudioCutActivity.PARAM_AUDIO)
            if (audioFragmentBean != null && !TextUtils.isEmpty(audioFragmentBean.path)) {
                mViewModel.song = getSongInfo(requireContext(), audioFragmentBean.path!!) ?: return
                initTimeBar(false)
                PlayerManager.playWithSeek(0, 0)
                freshSaveActions()
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

    private fun initToolbar() {
        viewBinding.toolbar.setNavigationOnClickListener {
            if (!mViewModel.isCutLineMoved && !mViewModel.isConformed) {
                activity?.finish()
            } else {
                showExitDialog()
            }
        }
    }

    private fun showExitDialog() {
        EditExitDialog.show(parentFragmentManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    override fun startObserve() {
        mViewModel.audioCutState.observe(viewLifecycleOwner) {}
    }


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
        //        viewBinding.timeLine.setScreenSpanValue(TimeRulerBar.VALUE_1000_MS * 8)
        //        viewBinding.timeLine.setMode(BaseAudioEditorView.MODE_ARRAY[2])
        //        viewBinding.timeLine.setRange(startTime, endTime)

        viewBinding.durationTime.text = mViewModel.song.duration.toLong()
            .format2DurationSimple() //        viewBinding.scale.text = viewBinding.timeLine.mMode.toString()

        viewBinding.timeLine.setOnCursorListener(object : BaseAudioEditorView.OnCursorListener {
            override fun onStartTrackingTouch(cursorValue: Long) {
            }

            override fun onProgressChanged(cursorValue: Long, fromeUser: Boolean) {
            }

            override fun onStopTrackingTouch(cursorValue: Long) {
            }
        })

        viewBinding.zoomIn.setOnClickListener {
            viewBinding.timeLine.zoomIn()
        }
        viewBinding.zoomOut.setOnClickListener {
            viewBinding.timeLine.zoomOut()
        }

        viewBinding.keepSelected.isSelected = true
        viewBinding.keepSelected.setOnClickListener {
            viewBinding.timeLine.switchCutMode(CutPieceFragment.CUT_MODE_SELECT)
            viewBinding.keepSelected.isSelected = true
            viewBinding.deleteSelected.isSelected = false
            viewBinding.jumpSelected.isSelected = false
        }
        viewBinding.deleteSelected.setOnClickListener {
            viewBinding.timeLine.switchCutMode(CutPieceFragment.CUT_MODE_DELETE)
            viewBinding.keepSelected.isSelected = false
            viewBinding.deleteSelected.isSelected = true
            viewBinding.jumpSelected.isSelected = false
        }

        viewBinding.jumpSelected.setOnClickListener {
            viewBinding.timeLine.switchCutMode(CutPieceFragment.CUT_MODE_JUMP)
            viewBinding.keepSelected.isSelected = false
            viewBinding.deleteSelected.isSelected = false
            viewBinding.jumpSelected.isSelected = true
        }

        viewBinding.cutAdd.setOnClickListener {
            viewBinding.timeLine.cutAdd()
        }

        viewBinding.cutRemove.setOnClickListener {
            viewBinding.timeLine.cutRemove()
        }

        viewBinding.playActions.setOnClickListener {
            viewBinding.timeLine.playOrPause()
        }

        viewBinding.trimStart.setOnClickListener {
            viewBinding.timeLine.trimStart()
        }

        viewBinding.trimEnd.setOnClickListener {
            viewBinding.timeLine.trimEnd()
        }

        PlayerManager.addProgressListener(object : PlayerProgressCallback {
            override fun onProgressChanged(currentWindowIndex: Int,
                                           position: Long,
                                           duration: Long) {
                if (isAdded) {
                    viewBinding.timeLine.onProgressChange(currentWindowIndex, position, duration)
                }
            }
        })

        viewBinding.timeLine.addOnCutModeChangeListener(object :
                                                            AudioCutEditorView.CutModeChangeListener {

            override fun onCutModeChange(mode: Int) {
                when (mode) {
                    CutPieceFragment.CUT_MODE_SELECT -> {
                        viewBinding.cutDesc.visibility = View.VISIBLE
                        viewBinding.trimAnchorLy.isVisible = true
                        viewBinding.jumpActionLy.isVisible = false
                    }

                    CutPieceFragment.CUT_MODE_DELETE -> {
                        viewBinding.cutDesc.visibility = View.VISIBLE
                        viewBinding.trimAnchorLy.isVisible = true
                        viewBinding.jumpActionLy.isVisible = false
                    }

                    CutPieceFragment.CUT_MODE_JUMP -> {
                        viewBinding.cutDesc.visibility = View.INVISIBLE
                        viewBinding.trimAnchorLy.isVisible = false
                        viewBinding.jumpActionLy.isVisible = true
                    }
                }
            }
        })

        viewBinding.timeLine.setOnScaleChangeListener(object : OnScaleChangeListener {

            override fun onScaleChange(mode: Int, min: Int, max: Int) {
                freshZoomView(mode, min, max)
                when (mode) {
                    BaseAudioEditorView.MODE_UINT_100_MS -> {
                    }

                    BaseAudioEditorView.MODE_UINT_500_MS -> {
                    }

                    BaseAudioEditorView.MODE_UINT_1000_MS -> {
                    }

                    BaseAudioEditorView.MODE_UINT_2000_MS -> {
                    }

                    BaseAudioEditorView.MODE_UINT_3000_MS -> {
                    }

                    BaseAudioEditorView.MODE_UINT_6000_MS -> {
                    }

                }
            }

        })

        viewBinding.timeLine.addOnCutLineAnchorChangeListener(object :
                                                                  AudioCutEditorView.OnCutLineAnchorChangeListener {

            override fun onCutLineChange(start: Boolean, end: Boolean) {
                viewBinding.clpLeft.visibility = if (start) View.VISIBLE else View.INVISIBLE
                viewBinding.clpRight.visibility = if (end) View.VISIBLE else View.INVISIBLE
                viewBinding.clStartAncher.visibility = if (start) View.VISIBLE else View.INVISIBLE
                viewBinding.clEndAncher.visibility = if (end) View.VISIBLE else View.INVISIBLE

            }
        })

        viewBinding.timeLine.addOnTrimAnchorChangeListener(object :
                                                               AudioCutEditorView.OnTrimAnchorChangeListener {

            override fun onTrimChange(start: Boolean, end: Boolean) {
                viewBinding.trimStart.isEnabled = start
                viewBinding.trimEnd.isEnabled = end
            }
        })

        viewBinding.timeLine.addOnCutLineChangeListener(object :
                                                            AudioCutEditorView.OnCutLineChangeListener {

            override fun onCutLineChange(start: Long, end: Long) {

                viewBinding.cutStart.text = "${start.format2DurationSimple()}"
                viewBinding.cutEnd.text = "${end.format2DurationSimple()}"
                viewBinding.durationSelected.text = "${(end - start).format2DurationSimple()}"
            }

            override fun onCutLineMove() {
                mViewModel.isCutLineMoved = true
            }
        })

        viewBinding.timeLine.addCutModeChangeButtonEnableListener(object :
                                                                      AudioCutEditorView.CutModeChangeButtonEnableListener {


            override fun onCutModeChange(addEnable: Boolean, removeEnable: Boolean) {
                viewBinding.cutAdd.isEnabled = addEnable
                viewBinding.cutRemove.isEnabled = removeEnable
            }
        })

        viewBinding.cutStartMinus.setOnClickListener {
            viewBinding.timeLine.startCutMinus()
        }
        viewBinding.cutStartPlus.setOnClickListener {
            viewBinding.timeLine.startCutPlus()
        }
        viewBinding.cutEndMinus.setOnClickListener {
            viewBinding.timeLine.startEndMinus()
        }
        viewBinding.cutEndPlus.setOnClickListener {
            viewBinding.timeLine.startEndPlus()
        }
        viewBinding.cutStart.setOnClickListener {
            viewBinding.timeLine.editTrimStart(parentFragmentManager)
        }
        viewBinding.cutEnd.setOnClickListener {
            viewBinding.timeLine.editTrimEnd(parentFragmentManager)
        }


        viewBinding.timeLine.addOnPlayingLineChangeListener(object :
                                                                AudioCutEditorView.OnPlayingLineChangeListener {

            override fun onPlayingLineChange(value: Long) {
                viewBinding.currentPlayTime.text = value.format2DurationSimple()
            }
        })

        viewBinding.clStartAncher.setOnClickListener {
            viewBinding.timeLine.anchor2CutStartLine()
        }

        viewBinding.clEndAncher.setOnClickListener {
            viewBinding.timeLine.anchor2CutEndLine()
        }

        viewBinding.confirm.setOnClickListener {
            mViewModel.isConformed = true
            mViewModel.isCancel = false
            audioDeal(mViewModel.song.path)
        }

        viewBinding.btnCancel.setOnClickListener {
            mViewModel.isCancel = true
            ffmpegHandler?.cancelExecute(true) //        viewBinding.progressLy.isVisible = false
        }

        viewBinding.pre.setOnClickListener {
            if (datas.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) { //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                var last = datas.lastAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!)
                if (last == null) { //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
                } else {
                    AudioCutActivity.open(requireContext(), last)
                }
            }
        }

        freshSaveActions()
        viewBinding.next.setOnClickListener {
            if (datas.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) { //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                var next = datas.nextAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!)
                if (next == null) { //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
                } else {
                    AudioCutActivity.open(requireContext(), next)
                }
            }
        }
        PlayerManager.addListener(this)

        viewBinding.timeLine.initConfig(AudioEditorConfig.Builder()
                                            .mode(BaseAudioEditorView.MODE_ARRAY[2])
                                            .startValue(startTime).endValue(endTime)
                                            .maxScreenSpanValue(mViewModel.song.duration.toLong())
                                            .build())

        setAudioData()
        if (isSaveDta) {
            addData(viewBinding.timeLine.audioFragmentBean)
        }
    }


    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (!isAdded) {
            return
        }
        if (isPlaying) {
            viewBinding.play.setImageResource(R.drawable.ic_puase)
        } else {
            viewBinding.play.setImageResource(R.drawable.ic_play)
        }
    }


    //todo requireContext()
    private fun setAudioData() {
        viewBinding.timeLine.setLoadingView(mViewModel.song.duration.toLong(), mViewModel.song.path)
        WaveformOptions.getSampleFrom(requireContext(), mViewModel.song.path) {
            viewBinding.timeLine.setWaveform(Waveform(it.toList()), mViewModel.song.duration.toLong(), mViewModel.song.path)
            viewBinding.waveLoading.pauseAnimation()
            viewBinding.waveLoading.isVisible = false
        }
        play(requireContext())
    }


    fun freshZoomView(currentMode: Int, minMode: Int, maxMode: Int) {
        var zoomIn = currentMode > minMode
        var zoomOut = currentMode < maxMode
        viewBinding.zoomIn.isEnabled = zoomIn
        viewBinding.zoomOut.isEnabled = zoomOut
        viewBinding.zoomIn.alpha = if (zoomIn) 1f else 0.5f
        viewBinding.zoomOut.alpha = if (zoomOut) 1f else 0.5f
    }

    private fun freshSaveActions() {
        if (datas.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) {
            viewBinding.pre.alpha = 0.5f
            viewBinding.next.alpha = 0.5f
            viewBinding.pre.isEnabled = false
            viewBinding.next.isEnabled = false
            return
        }
        if (datas.lastAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!) == null) {
            viewBinding.pre.alpha = 0.5f
            viewBinding.pre.isEnabled = false
        } else {
            viewBinding.pre.alpha = 1f
            viewBinding.pre.isEnabled = true
        }
        if (datas.nextAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!) == null) {
            viewBinding.next.alpha = 0.5f
            viewBinding.next.isEnabled = false
        } else {
            viewBinding.next.alpha = 1f
            viewBinding.next.isEnabled = true
        }
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
        var realCutPieceFragments = viewBinding.timeLine.cutPieceFragmentsOrder?.filter { !it.isFake }
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

    var editLoadingDialog: EditLoadingDialog? = null

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                FFmpegHandler.MSG_BEGIN -> {
                    Log.i(BaseAudioEditorView.jni_tag, "begin")
                    viewBinding.progressLy.isVisible = true //                    editLoadingDialog = EditLoadingDialog.show(parentFragmentManager)
                    viewBinding.progressText.text = "0%"
                    editLoadingDialog?.setOnCancelListener(this@AudioCutEditorFragment)
                }

                FFmpegHandler.MSG_FINISH -> {
                    Log.i(BaseAudioEditorView.jni_tag, "finish resultCode=${msg.obj}")
                    viewBinding.progressLy.isVisible = false
                    editLoadingDialog?.dismiss()
                    if (mViewModel.isCancel) {
                        return
                    }
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
                    editLoadingDialog?.freshProgress(progress)
                    viewBinding.progress.progress = progress
                    viewBinding.progressText.text = "$progress%"
                }

                FFmpegHandler.MSG_INFO -> {
                    Log.i(BaseAudioEditorView.jni_tag, "${msg.obj}")
                }

                FFmpegHandler.MSG_CONTINUE -> {
                    Log.i(BaseAudioEditorView.jni_tag, "continue")
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

    override fun onCancel() {
        ffmpegHandler?.cancelExecute(true) //        viewBinding.progressLy.isVisible = false
    }


}