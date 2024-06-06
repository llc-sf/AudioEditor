package com.san.audioeditor.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.toast.ToastCompat
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.android.app.AppProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.music.font.Poppins
import com.san.audioeditor.R
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.FAQActivity
import com.san.audioeditor.databinding.FragmentAudioCutBinding
import com.san.audioeditor.view.tips.CutPipsView
import com.san.audioeditor.viewmodel.AudioCutViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.framework.utils.OncePreferencesUtil
import dev.android.player.framework.utils.getLocationOnScreen
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.listener.OnScaleChangeListener
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback
import dev.audio.timeruler.timer.EditExitDialog
import dev.audio.timeruler.timer.RedoConfirmDialog
import dev.audio.timeruler.timer.UndoConfirmDialog
import dev.audio.timeruler.utils.cropMiddleThirdWidth
import dev.audio.timeruler.utils.dp
import dev.audio.timeruler.utils.format2DurationSimple
import dev.audio.timeruler.utils.format2DurationSimpleInt
import dev.audio.timeruler.utils.lastAudioFragmentBean
import dev.audio.timeruler.utils.nextAudioFragmentBean
import dev.audio.timeruler.weight.AudioCutEditorView
import dev.audio.timeruler.weight.AudioEditorConfig
import dev.audio.timeruler.weight.BaseAudioEditorView
import dev.audio.timeruler.weight.CutPieceFragment
import java.util.Calendar


class AudioCutEditorFragment : BaseMVVMFragment<FragmentAudioCutBinding>(), Player.EventListener {


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
                showWaveLoadingView()
                mViewModel.song = song
                PlayerManager.playByPath(mViewModel.song.path)
                initTimeBar()
                PlayerManager.playWithSeek(0, 0)
                freshSaveActions()
                return
            }

            val audioFragmentBean = intent.getParcelableExtra<AudioFragmentBean>(AudioCutActivity.PARAM_AUDIO)
            if (audioFragmentBean != null && !TextUtils.isEmpty(audioFragmentBean.path)) {
                showWaveLoadingView()
                mViewModel.reInitSongInfo(requireContext(), audioFragmentBean.path!!)
                if (mViewModel.song == null) {
                    mViewModel.editorError()
                    return
                }
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
        registerBack()
        initToolbar()
        PlayerManager.playByPath(mViewModel.song.path)
        viewBinding.toolbar.ImmerseDesign()
        mViewModel.initData(requireContext(), arguments)
        initTimeBar()
        adapterScreenHeight()
        autoPlay()
    }

    private fun autoPlay() {
        if (canCallBack) {
            PlayerManager.play()
        } else {
            PlayerManager.pause()
        }
    }

    private fun showTips() {
        if (!OncePreferencesUtil.get(OncePreferencesUtil.key_cut_tips)) {
            viewBinding.timeLine.post {
                if (activity == null) {
                    return@post
                }
                if (activity?.isFinishing == true) {
                    return@post
                }
                if (!isAdded) {
                    return@post
                }
                showDragTips()
            }
        }
    }

    //裁剪模式提示
    fun showCutModeTips() {
        var x = 0
        var y = 0
        val location = IntArray(2)
        var ancherView = viewBinding.modelLy
        ancherView.getLocationOnScreen(location)
        disableBack()
        activity?.window?.decorView?.let { rootView ->
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            var img = ImageView(requireContext()).apply {
                setImageBitmap(onDraw(ancherView))
            }
            (rootView as? FrameLayout)?.addView(img, layoutParams)
            img.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1]
                marginStart = 20.dp
            }

            var tipsView = CutPipsView(requireContext(), isBottomArrow = true, content = getString(R.string.choose_trim_method), actionMsg = getString(R.string.next))
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            tipsView.measure(widthMeasureSpec, heightMeasureSpec)
            (rootView as? FrameLayout)?.addView(tipsView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            tipsView.updateLayoutParams<FrameLayout.LayoutParams> {
                var margin = 0.dp
                topMargin = location[1] - tipsView.measuredHeight - margin + 10.dp
                marginStart = 20.dp
            }
            tipsView.bottomArrow().updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = viewBinding.keepSelected.measuredWidth / 2 - tipsView.bottomArrow().width / 2
            }
            tipsView.setAction {
                (rootView as? FrameLayout)?.removeView(tipsView)
                (rootView as? FrameLayout)?.removeView(img)
                showConfirmTips()
            }
        }
    }

    //保存提示
    private fun showSaveTips() {
        var x = 0
        var y = 0
        val location = IntArray(2)
        var ancherView = viewBinding.save
        ancherView.getLocationOnScreen(location)
        activity?.window?.decorView?.let { rootView ->
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            var img = ImageView(requireContext()).apply {
                setImageBitmap(onDraw(ancherView))
            }
            (rootView as? FrameLayout)?.addView(img, layoutParams)
            img.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1]
                marginStart = location[0]
            }

            var tipsView = CutPipsView(requireContext(), isTopArrow = true, content = getString(R.string.tap_to_save), actionMsg = getString(R.string.got_it))
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            tipsView.measure(widthMeasureSpec, heightMeasureSpec)
            (rootView as? FrameLayout)?.addView(tipsView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            tipsView.updateLayoutParams<FrameLayout.LayoutParams> {
                var margin = 20.dp
                topMargin = location[1] + ancherView.measuredHeight + margin
                marginStart = ScreenUtil.getScreenWidth(requireContext()) - tipsView.measuredWidth - margin
            }
            tipsView.topArrow().updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = tipsView.measuredWidth - tipsView.topArrow().measuredWidth / 2 - viewBinding.save.width / 2
            }
            tipsView.setAction {
                (rootView as? FrameLayout)?.removeView(tipsView)
                (rootView as? FrameLayout)?.removeView(img)

                (rootView as? FrameLayout)?.findViewById<View>(R.id.tips_bg)?.let {
                    (rootView as? FrameLayout)?.removeView(it)
                }
                OncePreferencesUtil.set(OncePreferencesUtil.key_cut_tips)
                viewBinding.timeLine.needShowTips = false
                PlayerManager.play()
                enableBack()
            }
        }
    }

    private fun showEditTips() {
        if (OncePreferencesUtil.get(OncePreferencesUtil.key_confirm_tips)) {
            return
        }
        disableBack()
        val location = IntArray(2)
        viewBinding.actionEdit.freshRightIconEnable(true, true)
        viewBinding.actionEdit.freshLeftIconEnable(true, true)
        var ancherView = viewBinding.actionEdit
        ancherView.getLocationOnScreen(location)
        activity?.window?.decorView?.let { rootView ->
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            var bg = ImageView(requireContext()).apply {
                setBackgroundColor(requireContext().resources.getColor(R.color.black_alpha_85))
                id = R.id.tips_bg
                setOnClickListener { }
            }
            var img = ImageView(requireContext()).apply {
                setImageBitmap(onDraw(ancherView))
            }
            (rootView as? FrameLayout)?.addView(bg, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            (rootView as? FrameLayout)?.addView(img, layoutParams)
            img.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1]
                marginStart = location[0]
            }

            var tipsView = CutPipsView(requireContext(), isTopArrow = true, content = getString(R.string.redo_undo_tips), actionMsg = getString(R.string.got_it))
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            tipsView.measure(widthMeasureSpec, heightMeasureSpec)
            (rootView as? FrameLayout)?.addView(tipsView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            tipsView.updateLayoutParams<FrameLayout.LayoutParams> {
                var margin = 20.dp
                topMargin = location[1] + ancherView.measuredHeight + margin
                marginStart = location[0]
            }
            tipsView.topArrow().updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = ancherView.width / 2 - tipsView.topArrow().measuredWidth / 2
            }
            tipsView.setAction {
                (rootView as? FrameLayout)?.removeView(tipsView)
                (rootView as? FrameLayout)?.removeView(img)
                (rootView as? FrameLayout)?.removeView(bg)
                viewBinding.actionEdit.reSotore()
                OncePreferencesUtil.set(OncePreferencesUtil.key_confirm_tips)
                enableBack()
                PlayerManager.play()
            }
        }
    }

    private fun showCutLineTips(isPlay: Boolean) {
        if (activity == null) {
            return
        }
        if (activity?.isFinishing == true) {
            return
        }
        if (!isAdded) {
            return
        }
        if (OncePreferencesUtil.get(OncePreferencesUtil.key_switch_mode_tips)) {
            return
        }
        disableBack()
        PlayerManager.pause()
        val location = IntArray(2)
        viewBinding.cutAdd.isEnabled = true
        viewBinding.cutRemove.isEnabled = true
        var ancherView = viewBinding.jumpActionLy
        ancherView.getLocationOnScreen(location)
        activity?.window?.decorView?.let { rootView ->
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            var bg = ImageView(requireContext()).apply {
                setBackgroundColor(requireContext().resources.getColor(R.color.black_alpha_85))
                id = R.id.tips_bg
                setOnClickListener { }
            }
            var img = ImageView(requireContext()).apply {
                setImageBitmap(onDraw(ancherView))
            }
            (rootView as? FrameLayout)?.addView(bg, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            (rootView as? FrameLayout)?.addView(img, layoutParams)
            img.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1]
                marginStart = location[0]
            }

            var tipsView = CutPipsView(requireContext(), isBottomArrow = true, content = getString(R.string.jump_tips), actionMsg = getString(R.string.got_it))
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            tipsView.measure(widthMeasureSpec, heightMeasureSpec)
            (rootView as? FrameLayout)?.addView(tipsView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            tipsView.updateLayoutParams<FrameLayout.LayoutParams> {
                var margin = 0.dp
                topMargin = location[1] - tipsView.measuredHeight - margin
                marginStart = location[0]
            }
            tipsView.bottomArrow().updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = viewBinding.cutAdd.measuredWidth / 2 - tipsView.bottomArrow().width / 2
            }
            tipsView.setAction {
                (rootView as? FrameLayout)?.removeView(tipsView)
                (rootView as? FrameLayout)?.removeView(img)
                (rootView as? FrameLayout)?.removeView(bg)
                OncePreferencesUtil.set(OncePreferencesUtil.key_switch_mode_tips)
                enableBack()
                if (isPlay) {
                    PlayerManager.play()
                }
            }
        }
    }

    //确认提示
    private fun showConfirmTips() {
        val location = IntArray(2)
        var ancherView = viewBinding.confirm
        ancherView.getLocationOnScreen(location)
        activity?.window?.decorView?.let { rootView ->
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            var img = ImageView(requireContext()).apply {
                setImageBitmap(onDraw(ancherView))
            }
            (rootView as? FrameLayout)?.addView(img, layoutParams)
            img.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1]
                marginStart = location[0]
            }

            var tipsView = CutPipsView(requireContext(), isBottomArrow = true, content = getString(R.string.confirm_this_trimming), actionMsg = getString(R.string.next))
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            tipsView.measure(widthMeasureSpec, heightMeasureSpec)
            (rootView as? FrameLayout)?.addView(tipsView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            tipsView.updateLayoutParams<FrameLayout.LayoutParams> {
                var margin = 0.dp
                topMargin = location[1] - tipsView.measuredHeight - margin + 10.dp
                marginStart = ScreenUtil.getScreenWidth(requireContext()) / 2 - tipsView.measuredWidth / 2
            }
            tipsView.bottomArrow().updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = tipsView.measuredWidth / 2 - tipsView.bottomArrow().width / 2
            }
            tipsView.setAction {
                (rootView as? FrameLayout)?.removeView(tipsView)
                (rootView as? FrameLayout)?.removeView(img)
                showSaveTips()
            }
        }
    }


    private var canCallBack = true
    private var callback = object : OnBackPressedCallback(true /* enabled by default */) {
        override fun handleOnBackPressed() { // 在这里处理返回逻辑
            if (canCallBack) {
                backDeal()
            }
        }
    } // 将回调添加到OnBackPressedDispatcher

    private fun freshDragTips() {
        activity?.window?.decorView?.let { rootView ->
            var container = rootView.findViewById<FrameLayout>(R.id.tips_wave_container)
            if (container != null) {
                rootView.findViewById<View>(R.id.tips_wave_loading_root)?.let {
                    container.removeView(it)
                }
                rootView.findViewById<ImageView>(R.id.tips_wave_wave)?.let {
                    it.setImageBitmap(onDraw(viewBinding.timeLine).cropMiddleThirdWidth())
                }
            }
        }
    }

    private fun showDragTips() {
        disableBack()
        val location = IntArray(2)
        var ancherview = viewBinding.timeLine
        ancherview.getLocationOnScreen(location)
        activity?.window?.decorView?.let { rootView ->
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            val layoutParamsContainer = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            var bg = ImageView(requireContext()).apply {
                setBackgroundColor(requireContext().resources.getColor(R.color.black_alpha_85))
                id = R.id.tips_bg
                setOnClickListener { }
            }
            val container = FrameLayout(requireContext()).apply {
                id = R.id.tips_wave_container
            }
            val layoutParamsLoadingRoot = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                .apply {
                    gravity = Gravity.CENTER
                }
            var loadingRootLinearLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                id = R.id.tips_wave_loading_root
            }
            var loading = LottieAnimationView(requireContext()).apply {
                setAnimation("trimloading.json") // 设置循环播放
                repeatCount = -1
                playAnimation()
            }
            var loadingText = TextView(requireContext()).apply {
                text = getString(R.string.loading)
                textSize = 11f
                setTextColor(requireContext().resources.getColor(R.color.white_alpha_40))
                typeface = Poppins.getTypefaceFromCache(Poppins.Regular)
            }
            loadingRootLinearLayout.addView(loading, LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            loadingRootLinearLayout.addView(loadingText, LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            var img = ImageView(requireContext()).apply {
                setImageBitmap(onDraw(ancherview).cropMiddleThirdWidth())
                id = R.id.tips_wave_wave
            }
            (rootView as? FrameLayout)?.addView(bg, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            container.addView(img, layoutParams)
            container.addView(loadingRootLinearLayout, layoutParamsLoadingRoot)
            (rootView as? FrameLayout)?.addView(container, layoutParamsContainer)
            container.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1]
                marginStart = ScreenUtil.getScreenWidth(requireContext()) / 3 - 30
            }
            var tipsView = CutPipsView(requireContext(), isTopArrow = true, content = getString(R.string.drag_to_trim_or_cut), actionMsg = getString(R.string.next))
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            tipsView.measure(widthMeasureSpec, heightMeasureSpec)
            (rootView as? FrameLayout)?.addView(tipsView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            tipsView.updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = location[1] + ancherview.measuredHeight - 20.dp
                marginStart = ScreenUtil.getScreenWidth(requireContext()) / 2 - tipsView.measuredWidth / 2
            }
            tipsView.topArrow().updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = tipsView.measuredWidth / 2 - tipsView.topArrow().width / 2
            }
            tipsView.setAction {
                (rootView as? FrameLayout)?.removeView(tipsView)
                (rootView as? FrameLayout)?.removeView(container)
                showCutModeTips()
            }
        }
    }

    fun onDraw(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.setBitmap(bitmap)
        val drawable = view.background
        drawable?.draw(canvas)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 适配屏幕高度
     */
    private fun adapterScreenHeight() {
        viewBinding.timeLine.post {
            var waveHeightInt = 114
            var cutDescInt = 8
            var playActionsInt = 8
            var cutLyInt = 28
            var totalInt = cutLyInt + waveHeightInt + cutDescInt + playActionsInt
            var bottomMarginConfirm = 0.dp
            var rectConfirm = viewBinding.confirm.getLocationOnScreen()
            var totalBottomMargin = ScreenUtil.getScreenHeight(requireContext()) - rectConfirm.bottom - bottomMarginConfirm
            var cutDescTopMargin = 8.dp
            var cutLyTopMargin = 28.dp
            var playActionsMargin = 8.dp
            var total = viewBinding.timeLine.waveHeight * 2 + totalBottomMargin + cutDescTopMargin + cutLyTopMargin + playActionsMargin
            if (totalBottomMargin <= 0) {
                return@post
            }
            viewBinding.timeLine.updateWaveHeight(total / totalInt * waveHeightInt / 2)
            viewBinding.cutDesc.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = (total / totalInt * cutDescInt).toInt()
            }
            viewBinding.playActions.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = (total / totalInt * playActionsInt).toInt()
            }
            viewBinding.cutLy.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = (total / totalInt * cutLyInt).toInt()
            }

            showTips()
        }
    }

    private fun initToolbar() {
        viewBinding.toolbar.setNavigationOnClickListener {
            backDeal()
        }
    }


    private fun backDeal() {
        if (canCallBack) {
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
        mViewModel.mainModel.observe(viewLifecycleOwner) { uiState ->
            uiState.isSuccess?.let {
                if (it.isShowEditLoading == true) {
                    showAudioDealLoadingView()
                    viewBinding.progressText.text = "(${0}%)"
                }
                if ((it.progress ?: 0) > 0) {
                    viewBinding.progressText.text = "(${it.progress}%)"
                    viewBinding.progress.progress = it.progress ?: 0
                }
                if (it.isShowEditLoading == false) {
                    hideAudioDealLoadingView()
                }
                if (it.waveform != null) {
                    if (it.waveform!!.isEmpty()) {
                        ToastCompat.makeText(AppProvider.context, false, AppProvider.context.getString(R.string.song_unavailable))
                            .show()
                        mActivity?.finish()
                        return@observe
                    }
                    viewBinding.timeLine.setWaveform(Waveform(it.waveform!!.toList()), mViewModel.song.duration.toLong(), mViewModel.song.path, it.cutMode
                        ?: CutPieceFragment.CUT_MODE_SELECT)
                    hideWaveLoadingView()
                    freshZoomView()
                    waveDataLoaded()
                }
                if (it.isShowEditTips == true) {
                    showEditTips()
                }

                if (it.isEnableBack == true) {
                    enableBack()
                }
            }
        }
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

        freshCutModeView(if (isSaveDta) CutPieceFragment.CUT_MODE_SELECT else viewBinding.timeLine.cutMode)
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

            override fun onCutModeChange(mode: Int, isPlay: Boolean) {
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
                        viewBinding.jumpActionLy.postDelayed({ showCutLineTips(isPlay) }, 80)
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

        //裁剪微调
        viewBinding.timeLine.addOnCutLineChangeListener(object :
                                                            AudioCutEditorView.OnCutLineChangeListener {


            override fun onCutLineChange(start: Long, end: Long) {
                viewBinding.cutStart.text = "${start.format2DurationSimple()}"
                viewBinding.cutEnd.text = "${end.format2DurationSimple()}"
                viewBinding.cutLineStart.setText("${start.format2DurationSimple()}")
                viewBinding.cutLineEnd.setText("${end.format2DurationSimple()}")
                viewBinding.timeLine.freshCutLineFineTuningButtonEnable()
                viewBinding.durationSelected.text = "${(viewBinding.timeLine.selectedTime).format2DurationSimple()}"

            }

            override fun onCutLineMove() {
                mViewModel.isCutLineMoved = true
            }

            override fun onCutLineLight(startLight: Boolean, endLight: Boolean) {
                viewBinding.cutLineStart.light(startLight)
                viewBinding.cutLineEnd.light(endLight)
            }
        })

        viewBinding.timeLine.addCutModeChangeButtonEnableListener(object :
                                                                      AudioCutEditorView.CutModeChangeButtonEnableListener {


            override fun onCutModeChange(addEnable: Boolean, removeEnable: Boolean) {
                viewBinding.cutAdd.isEnabled = addEnable
                viewBinding.cutRemove.isEnabled = removeEnable
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
                viewBinding.durationSelected.text = "${(viewBinding.timeLine.selectedTime).format2DurationSimple()}"
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
            if (!checkDealDuration()) {
                ToastCompat.makeText(context, false, requireContext().getString(R.string.error_save))
                    .show()
                return@setOnClickListener
            }
            var realCutPieceFragments = viewBinding.timeLine.cutPieceFragmentsOrder?.filter { !it.isFake }
            mViewModel.audioDeal(requireContext(), viewBinding.timeLine.cutMode, realCutPieceFragments)
        }

        viewBinding.btnCancel.setOnClickListener {
            mViewModel.isCancel = true
            mViewModel.cancelEditor()
        }

        freshSaveActions()
        viewBinding.save.setOnClickListener {
            if (!checkDealDuration()) {
                ToastCompat.makeText(context, false, requireContext().getString(R.string.error_save))
                    .show()
                return@setOnClickListener
            }
            disableBack()
            PlayerManager.pause()
            var realCutPieceFragments = viewBinding.timeLine.cutPieceFragmentsOrder?.filter { !it.isFake }
            mViewModel.save(requireContext(), viewBinding.timeLine.cutMode, realCutPieceFragments)
        }

        viewBinding.question.setOnClickListener {
            FAQActivity.open(requireContext(), FAQActivity.OPEN_FROM_AUDIO_CUT)
        }

        PlayerManager.addEventListener(this)

        viewBinding.timeLine.initConfig(AudioEditorConfig.Builder()
                                            .mode(BaseAudioEditorView.MODE_ARRAY[2])
                                            .startValue(startTime).endValue(endTime)
                                            .maxScreenSpanValue(mViewModel.song.duration.toLong())
                                            .build())

        setAudioData(if (isSaveDta) CutPieceFragment.CUT_MODE_SELECT else viewBinding.timeLine.cutMode)
        if (isSaveDta) {
            addData(viewBinding.timeLine.audioFragmentBean)
        }


    }

    private fun checkDealDuration(): Boolean {
        return viewBinding.timeLine.selectedTime >= 100
    }

    private fun scrollToCenter(scrollView: HorizontalScrollView, view: View) {
        val scrollViewWidth = scrollView.width
        val viewWidth = view.width
        val viewLeft = view.left
        val scrollX = viewLeft + viewWidth / 2 - scrollViewWidth / 2
        scrollView.smoothScrollTo(scrollX, 0)
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

                scrollToCenter(viewBinding.modelLy, viewBinding.keepSelected); //                viewBinding.modelLy.smoothScrollTo(viewBinding.keepSelected.left, 0);
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

                scrollToCenter(viewBinding.modelLy, viewBinding.deleteSelected); //                viewBinding.modelLy.smoothScrollTo(viewBinding.deleteSelected.left, 0);
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

                scrollToCenter(viewBinding.modelLy, viewBinding.jumpSelected); //                viewBinding.modelLy.smoothScrollTo(viewBinding.jumpSelected.left, 0);

            }
        }
    }

    private fun editPre() {
        if (mViewModel.tempConfirmAudios.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) { //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
        } else {
            var last = mViewModel.tempConfirmAudios.lastAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!)
            if (last == null) { //                    Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
            } else {
                AudioCutActivity.open(requireContext(), last)
            }
        }
    }

    private fun editNext() {
        if (mViewModel.tempConfirmAudios.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) { //                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT).show()
        } else {
            var next = mViewModel.tempConfirmAudios.nextAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!)
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
        } else {
            viewBinding.play.setImageResource(R.drawable.ic_play)
        }
    }


    private fun setAudioData(cutMode: Int) { //        var bg = ImageView(requireContext()).apply {
        viewBinding.timeLine.switchCutMode(cutMode)
        viewBinding.timeLine.setLoadingView(mViewModel.song.duration.toLong(), mViewModel.song.path, cutMode)
        mViewModel.getAudioData(requireContext(), cutMode)
        play(requireContext())
    }

    private fun showWaveLoadingView() {
        viewBinding.waveLoading.playAnimation()
        viewBinding.waveLoadingRoot.isVisible = true
    }

    private fun hideWaveLoadingView() {
        viewBinding.waveLoading.pauseAnimation()
        viewBinding.waveLoadingRoot.isVisible = false
    }

    private fun waveDataLoaded() {
        if (!OncePreferencesUtil.get(OncePreferencesUtil.key_cut_tips)) { //            showTips()
            freshDragTips()
        }
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
        if (mViewModel.tempConfirmAudios.isNullOrEmpty() || viewBinding.timeLine.audioFragmentBean == null) {
            viewBinding.pre.alpha = 0.5f
            viewBinding.next.alpha = 0.5f
            viewBinding.pre.isEnabled = false
            viewBinding.next.isEnabled = false
            viewBinding.actionEdit.freshLeftIconEnable(false)
            viewBinding.actionEdit.freshRightIconEnable(false)
            return
        }
        if (mViewModel.tempConfirmAudios.lastAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!) == null) {
            viewBinding.pre.alpha = 0.5f
            viewBinding.pre.isEnabled = false
            viewBinding.actionEdit.freshLeftIconEnable(false)
        } else {
            viewBinding.pre.alpha = 1f
            viewBinding.pre.isEnabled = true
            viewBinding.actionEdit.freshLeftIconEnable(true)
        }
        if (mViewModel.tempConfirmAudios.nextAudioFragmentBean(viewBinding.timeLine.audioFragmentBean!!) == null) {
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

    private fun disableBack() {
        canCallBack = false
    }

    private fun registerBack() {
        requireActivity().onBackPressedDispatcher.addCallback(this@AudioCutEditorFragment, callback!!)
    }

    private fun enableBack() {
        canCallBack = true
    }

    private fun showAudioDealLoadingView() {
        viewBinding.cover.isVisible = true
        viewBinding.progressLy.isVisible = true //
    }

    private fun hideAudioDealLoadingView() {
        viewBinding.cover.isVisible = false
        viewBinding.progressLy.isVisible = false //
    }


    //注意调用时机 todo
    private fun addData(audioFragmentBean: AudioFragmentBean?) {
        audioFragmentBean?.let {
            mViewModel.tempConfirmAudios.add(it)
        }
    }


}