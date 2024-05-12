package com.san.audioeditor.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
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
import dev.android.player.framework.utils.getLocationOnScreen
import dev.audio.ffmpeglib.FFmpegApplication
import dev.audio.ffmpeglib.tool.FFmpegUtil
import dev.audio.ffmpeglib.tool.FileUtil
import dev.audio.ffmpeglib.tool.ScreenUtil
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
import dev.audio.timeruler.timer.RedoConfirmDialog
import dev.audio.timeruler.timer.UndoConfirmDialog
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.dp
import dev.audio.timeruler.utils.format2DurationSimple
import dev.audio.timeruler.utils.format2DurationSimpleInt
import dev.audio.timeruler.utils.lastAudioFragmentBean
import dev.audio.timeruler.utils.nextAudioFragmentBean
import dev.audio.timeruler.utils.toSegmentsArray
import dev.audio.timeruler.weight.AudioCutEditorView
import dev.audio.timeruler.weight.AudioEditorConfig
import dev.audio.timeruler.weight.BaseAudioEditorView
import dev.audio.timeruler.weight.CutPieceFragment
import java.io.File
import java.util.Calendar

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
                PlayerManager.playByPath(mViewModel.song.path)
                initTimeBar()
                PlayerManager.playWithSeek(0, 0)
                freshSaveActions()
                return
            }

            val audioFragmentBean = intent.getParcelableExtra<AudioFragmentBean>(AudioCutActivity.PARAM_AUDIO)
            if (audioFragmentBean != null && !TextUtils.isEmpty(audioFragmentBean.path)) {
                mViewModel.song = getSongInfo(requireContext(), audioFragmentBean.path!!) ?: return
                PlayerManager.playByPath(mViewModel.song.path)
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
        mViewModel.initData(requireContext(), arguments)
        initTimeBar()
        adapterScreenHeight()
    }

    /**
     * 适配屏幕高度
     */
    private fun adapterScreenHeight() {
        viewBinding.timeLine.post {
            var bottomMargin = 24.dp
            var margin = 24.dp
            var rect = viewBinding.confirm.getLocationOnScreen()
            var totalBottomMargin = ScreenUtil.getScreenHeight(requireContext()) - rect.bottom
            if (totalBottomMargin < bottomMargin + margin * 2) {
                return@post
            }
            var waveHeightOffset = totalBottomMargin - bottomMargin - margin * 2
            viewBinding.timeLine.updateWaveHeight(viewBinding.timeLine.waveHeight + waveHeightOffset.toFloat() / 2f)
            viewBinding.cutDesc.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin += margin
            }
            viewBinding.playActions.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin += margin
            }
        }
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
        mViewModel.clearDatas()
    }


    override fun startObserve() {
        mViewModel.audioCutState.observe(viewLifecycleOwner) {
            if (it.isShowEditLoading == true) {
                viewBinding.progressLy.isVisible = true
                viewBinding.progressText.text = "${0}%"
            } else if ((it.progress ?: 0) > 0) {
                viewBinding.progressText.text = "${it.progress}%"
            } else if (it.isShowEditLoading == false) {
                viewBinding.progressLy.isVisible = false
            }
        }
    }

    private var cutAddEnable = false
    private var cutRemoveEnable = false

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
            .format2DurationSimpleInt() //        viewBinding.scale.text = viewBinding.timeLine.mMode.toString()

        viewBinding.timeLine.setOnCursorListener(object : BaseAudioEditorView.OnCursorListener {
            override fun onStartTrackingTouch(cursorValue: Long) {
            }

            override fun onProgressChanged(cursorValue: Long, fromeUser: Boolean) {
            }

            override fun onStopTrackingTouch(cursorValue: Long) {
            }
        })

        //撤销 重做
        viewBinding.actionEdit.setActionListener({
                                                     UndoConfirmDialog.show(parentFragmentManager)
                                                         ?.setOnConfirmListener(object :
                                                                                    UndoConfirmDialog.OnConfirmListener {
                                                             override fun onConfirm() {
                                                                 editPre()
                                                             }

                                                             override fun onCancel() {
                                                             }
                                                         })
                                                 }, {
                                                     RedoConfirmDialog.show(parentFragmentManager)
                                                         ?.setOnConfirmListener(object :
                                                                                    RedoConfirmDialog.OnConfirmListener {
                                                             override fun onConfirm() {
                                                                 editNext()
                                                             }

                                                             override fun onCancel() {
                                                             }
                                                         })
                                                 }) //放大缩小
        viewBinding.actionScale.setActionListener({
                                                      viewBinding.timeLine.zoomOut()
                                                  }, {
                                                      viewBinding.timeLine.zoomIn()
                                                  })

        freshCutModeView(CutPieceFragment.CUT_MODE_SELECT)
        viewBinding.keepSelected.setOnClickListener {
            viewBinding.timeLine.switchCutMode(CutPieceFragment.CUT_MODE_SELECT)
            freshCutModeView(CutPieceFragment.CUT_MODE_SELECT)
        }
        viewBinding.deleteSelected.setOnClickListener {
            viewBinding.timeLine.switchCutMode(CutPieceFragment.CUT_MODE_DELETE)
            freshCutModeView(CutPieceFragment.CUT_MODE_DELETE)
        }

        viewBinding.jumpSelected.setOnClickListener {
            viewBinding.timeLine.switchCutMode(CutPieceFragment.CUT_MODE_JUMP)
            freshCutModeView(CutPieceFragment.CUT_MODE_JUMP)
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
                freshZoomView()
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
                if (PlayerManager.isPlaying) {
                    return
                }
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
                viewBinding.cutLineStart.setText("${start.format2DurationSimple()}")
                viewBinding.cutLineEnd.setText("${end.format2DurationSimple()}")
                viewBinding.timeLine.freshCutLineFineTuningButtonEnable()

            }

            override fun onCutLineMove() {
                mViewModel.isCutLineMoved = true
            }
        })

        viewBinding.timeLine.addCutModeChangeButtonEnableListener(object :
                                                                      AudioCutEditorView.CutModeChangeButtonEnableListener {


            override fun onCutModeChange(addEnable: Boolean, removeEnable: Boolean) {
                cutAddEnable = addEnable
                cutRemoveEnable = removeEnable
                if(PlayerManager.isPlaying){
                    viewBinding.cutAdd.isEnabled = false
                    viewBinding.cutRemove.isEnabled = false
                }else{
                    viewBinding.cutAdd.isEnabled = addEnable
                    viewBinding.cutRemove.isEnabled = removeEnable
                }
            }
        })

        //裁剪条微调
        viewBinding.cutLineStart.setActionListener({
                                                       viewBinding.timeLine.startCutMinus()
                                                   }, {

                                                       viewBinding.timeLine.editTrimStart(parentFragmentManager)
                                                   }, {
                                                       viewBinding.timeLine.startCutPlus()
                                                   })

        //裁剪条微调
        viewBinding.cutLineEnd.setActionListener({
                                                     viewBinding.timeLine.endCutMinus()
                                                 }, {
                                                     viewBinding.timeLine.editTrimEnd(parentFragmentManager)
                                                 }, {
                                                     viewBinding.timeLine.endCutPlus()
                                                 })
        viewBinding.timeLine.addCutLineFineTuningButtonChangeListener(object :
                                                                          AudioCutEditorView.CutLineFineTuningButtonChangeListener {


            override fun onCutLineFineTuningButtonChange(startMinusEnable: Boolean,
                                                         startPlusEnable: Boolean,
                                                         endMinusEnable: Boolean,
                                                         endPlusEnable: Boolean) {
                viewBinding.cutLineStart.freshButtonEnable(startMinusEnable, startPlusEnable)
                viewBinding.cutLineEnd.freshButtonEnable(endMinusEnable, endPlusEnable)
            }

            override fun onCutLineFineTuningEnable(isEnable: Boolean) {
                viewBinding.cutLineStart.fineTuningEnable(isEnable)
                viewBinding.cutLineEnd.fineTuningEnable(isEnable)
            }
        })

        viewBinding.cutStartMinus.setOnClickListener {
            viewBinding.timeLine.startCutMinus()
        }
        viewBinding.cutStartPlus.setOnClickListener {
            viewBinding.timeLine.startCutPlus()
        }
        viewBinding.cutEndMinus.setOnClickListener {
            viewBinding.timeLine.endCutMinus()
        }
        viewBinding.cutEndPlus.setOnClickListener {
            viewBinding.timeLine.endCutPlus()
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
                viewBinding.currentPlayTime.text = value.format2DurationSimpleInt()
            }
        })

        //开始裁剪线 定位
        viewBinding.clStartAncher.setOnClickListener {
            viewBinding.timeLine.anchor2CutStartLine()
        }

        //结束裁剪线 定位
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

        freshSaveActions()
        viewBinding.save.setOnClickListener {
            var realCutPieceFragments = viewBinding.timeLine.cutPieceFragmentsOrder?.filter { !it.isFake }
            mViewModel.save(requireContext(), realCutPieceFragments, mViewModel.datas)
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
        PlayerManager.play()
    }

    private fun freshCutModeView(mode: Int) {
        when (mode) {
            CutPieceFragment.CUT_MODE_SELECT -> {
                viewBinding.keepSelected.isSelected = true
                viewBinding.keepSelectedTv.isEnabled = true
                viewBinding.deleteSelected.isSelected = false
                viewBinding.deleteSelectedTv.isEnabled = false
                viewBinding.jumpSelected.isSelected = false
                viewBinding.jumpSelectedTv.isEnabled = false
                viewBinding.keepSelectedIcon.isVisible = true
                viewBinding.deleteSelectedIcon.isVisible = false
                viewBinding.jumpSelectedIcon.isVisible = false
            }

            CutPieceFragment.CUT_MODE_DELETE -> {
                viewBinding.keepSelected.isSelected = false
                viewBinding.keepSelectedTv.isEnabled = false
                viewBinding.deleteSelected.isSelected = true
                viewBinding.deleteSelectedTv.isEnabled = true
                viewBinding.jumpSelected.isSelected = false
                viewBinding.jumpSelectedTv.isEnabled = false
                viewBinding.keepSelectedIcon.isVisible = false
                viewBinding.deleteSelectedIcon.isVisible = true
                viewBinding.jumpSelectedIcon.isVisible = false
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                viewBinding.keepSelected.isSelected = false
                viewBinding.keepSelectedTv.isEnabled = false
                viewBinding.deleteSelected.isSelected = false
                viewBinding.deleteSelectedTv.isEnabled = false
                viewBinding.jumpSelected.isSelected = true
                viewBinding.jumpSelectedTv.isEnabled = true
                viewBinding.keepSelectedIcon.isVisible = false
                viewBinding.deleteSelectedIcon.isVisible = false
                viewBinding.jumpSelectedIcon.isVisible = true

            }
        }
    }

    private fun editPre() {
        if (mViewModel.datas.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) { //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
        } else {
            var last = mViewModel.datas.lastAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!)
            if (last == null) { //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                AudioCutActivity.open(requireContext(), last)
            }
        }
    }

    private fun editNext() {
        if (mViewModel.datas.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) { //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
        } else {
            var next = mViewModel.datas.nextAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!)
            if (next == null) { //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                AudioCutActivity.open(requireContext(), next)
            }
        }
    }


    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (!isAdded) {
            return
        }
        if (isPlaying) {
            viewBinding.play.setImageResource(R.drawable.ic_puase)
            viewBinding.clStartAncher.isVisible = false
            viewBinding.clEndAncher.isVisible = false
        } else {
            viewBinding.play.setImageResource(R.drawable.ic_play)
            viewBinding.clStartAncher.isVisible = viewBinding.timeLine.isCutLineStartVisible
            viewBinding.clEndAncher.isVisible = viewBinding.timeLine.isCutLineEndVisible
            viewBinding.cutAdd.isEnabled = cutAddEnable
            viewBinding.cutRemove.isEnabled = cutRemoveEnable

        }
    }


    //todo requireContext()
    private fun setAudioData() {
        viewBinding.timeLine.setLoadingView(mViewModel.song.duration.toLong(), mViewModel.song.path)
        WaveformOptions.getSampleFrom(requireContext(), mViewModel.song.path) {
            viewBinding.timeLine.setWaveform(Waveform(it.toList()), mViewModel.song.duration.toLong(), mViewModel.song.path)
            viewBinding.waveLoading.pauseAnimation()
            viewBinding.waveLoading.isVisible = false
            freshZoomView()
        }
        play(requireContext())
    }


    fun freshZoomView() {
        var zoomIn = viewBinding.timeLine.canZoomIn
        var zoomOut = viewBinding.timeLine.canZoomOut
        viewBinding.zoomIn.isEnabled = zoomIn
        viewBinding.zoomOut.isEnabled = zoomOut
        viewBinding.zoomIn.alpha = if (zoomIn) 1f else 0.5f
        viewBinding.zoomOut.alpha = if (zoomOut) 1f else 0.5f
        viewBinding.actionScale.freshLeftIconEnable(zoomOut)
        viewBinding.actionScale.freshRightIconEnable(zoomIn)
    }

    private fun freshSaveActions() {
        if (mViewModel.datas.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) {
            viewBinding.pre.alpha = 0.5f
            viewBinding.next.alpha = 0.5f
            viewBinding.pre.isEnabled = false
            viewBinding.next.isEnabled = false
            viewBinding.actionEdit.freshLeftIconEnable(false)
            viewBinding.actionEdit.freshRightIconEnable(false)
            return
        }
        if (mViewModel.datas.lastAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!) == null) {
            viewBinding.pre.alpha = 0.5f
            viewBinding.pre.isEnabled = false
            viewBinding.actionEdit.freshLeftIconEnable(false)
        } else {
            viewBinding.pre.alpha = 1f
            viewBinding.pre.isEnabled = true
            viewBinding.actionEdit.freshLeftIconEnable(true)
        }
        if (mViewModel.datas.nextAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!) == null) {
            viewBinding.next.alpha = 0.5f
            viewBinding.next.isEnabled = false
            viewBinding.actionEdit.freshRightIconEnable(false)
        } else {
            viewBinding.next.alpha = 1f
            viewBinding.next.isEnabled = true
            viewBinding.actionEdit.freshRightIconEnable(true)
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

        cutFileName = "cut_" + (System.currentTimeMillis())
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

    //todo  封装
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
                            AudioFileUtils.deleteFile(outputPath)
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


    //注意调用时机 todo
    private fun addData(audioFragmentBean: AudioFragmentBean?) {
        audioFragmentBean?.let {
            mViewModel.datas.add(it)
        }
    }

    override fun onCancel() {
        ffmpegHandler?.cancelExecute(true) //        viewBinding.progressLy.isVisible = false
    }


}