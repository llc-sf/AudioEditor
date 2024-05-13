package com.san.audioeditor.fragment

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.R
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentAudioSaveBinding
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.viewmodel.AudioPickViewModel
import com.san.audioeditor.viewmodel.AudioSaveViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.format2DurationStander
import java.io.File

/**
 * 媒体选择页
 */
class AudioSaveFragment : BaseMVVMFragment<FragmentAudioSaveBinding>() {


    companion object {
        const val PARAM_SONG = "param_song"
    }

    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentAudioSaveBinding {
        return FragmentAudioSaveBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioSaveViewModel
    override fun initViewModel() {
        mViewModel = AudioSaveViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_audio_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun initView() {
        super.initView()
        (activity as? AppCompatActivity)?.setSupportActionBar(viewBinding.toolbar)
        viewBinding.toolbar.ImmerseDesign()
        viewBinding.toolbar.setNavigationOnClickListener {
            mActivity?.finish()
        }
        mViewModel.initData(requireContext(), arguments)
    }

    override fun startObserve() {
        mViewModel.audioSaveState.observe(viewLifecycleOwner) {
            if (it.song != null) {
                viewBinding.audioTitle.text = it.song!!.title
                viewBinding.audioInfo.text = "${DateUtil.formatTime((it.song!!.duration).toLong())} | ${FileUtils.getFileSize(it.song!!.size)} | ${AudioFileUtils.getExtension(it.song!!.path).uppercase()}"
            }
        }
    }

}