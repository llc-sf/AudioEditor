package com.san.audioeditor.fragment

import android.view.LayoutInflater
import com.san.audioeditor.cell.CellMediaItemView
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.viewmodel.MediaPickViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter

class MediaPickFragment : BaseMVVMFragment<FragmentMediaPickBinding>() {


    companion object{
        const val PARAM_SONG = "param_song"
    }

    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentMediaPickBinding {
        return FragmentMediaPickBinding.inflate(inflater)
    }

    private lateinit var mViewModel: MediaPickViewModel
    override fun initViewModel() {
        mViewModel = MediaPickViewModel()
    }


    override fun initView() {
        super.initView()
        mAdapter.register(CellMediaItemView())
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