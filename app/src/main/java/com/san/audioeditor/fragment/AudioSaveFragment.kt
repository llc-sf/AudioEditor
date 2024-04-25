package com.san.audioeditor.fragment

import android.view.LayoutInflater
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentAudioSaveBinding
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.viewmodel.AudioPickViewModel
import com.san.audioeditor.viewmodel.AudioSaveViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter

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


    override fun initViewBinding(inflater: LayoutInflater): FragmentAudioSaveBinding {
        return FragmentAudioSaveBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioSaveViewModel
    override fun initViewModel() {
        mViewModel = AudioSaveViewModel()
    }


    override fun initView() {
        super.initView()
        viewBinding.toolbar.ImmerseDesign()
        mViewModel.initData(requireContext(),arguments)
    }

    override fun startObserve() {
        mViewModel.audioSaveState.observe(viewLifecycleOwner) {}
    }

}