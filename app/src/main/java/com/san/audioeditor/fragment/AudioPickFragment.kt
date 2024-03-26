package com.san.audioeditor.fragment

import android.view.LayoutInflater
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.viewmodel.AudioPickViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter

/**
 * 媒体选择页
 */
class AudioPickFragment : BaseMVVMFragment<FragmentMediaPickBinding>() {


    companion object{
        const val PARAM_SONG = "param_song"
    }

    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentMediaPickBinding {
        return FragmentMediaPickBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioPickViewModel
    override fun initViewModel() {
        mViewModel = AudioPickViewModel()
    }


    override fun initView() {
        super.initView()
        viewBinding.toolbar.ImmerseDesign()
        mAdapter.register(CellAudioItemView())
        viewBinding.recycleview.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        mActivity?.let {
            mViewModel.initData(it, arguments)
        }
    }

    override fun startObserve() {
        mViewModel.mediaPickState.observe(viewLifecycleOwner) {
            if (it.songs?.isNotEmpty() == true) {
                mAdapter.items = it.songs!!
                mAdapter.notifyDataSetChanged()
            }
        }
    }

}