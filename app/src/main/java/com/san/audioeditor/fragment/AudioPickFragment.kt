package com.san.audioeditor.fragment

import android.view.LayoutInflater
import android.widget.toast.ToastCompat
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.viewmodel.AudioPickViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter

/**
 * 媒体选择页
 */
class AudioPickFragment : BaseMVVMRefreshFragment<FragmentMediaPickBinding>() {


    companion object {
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
        showRefresh() //返回按钮生效
        viewBinding.toolbar.setNavigationOnClickListener {
            mActivity?.finish()
        }
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
                stopRefresh()
            }else{
                ToastCompat.makeText(requireContext(),false, "没有找到音频文件").show()
                stopRefresh()
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        mViewModel.onRefresh(requireContext())
    }

}