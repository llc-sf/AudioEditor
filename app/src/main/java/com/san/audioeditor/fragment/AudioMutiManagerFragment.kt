package com.san.audioeditor.fragment

import android.view.LayoutInflater
import android.widget.toast.ToastCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentSongMutiManagerBinding
import com.san.audioeditor.view.AudioItemView
import com.san.audioeditor.viewmodel.AudioMutiManagerViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback

/**
 * 媒体选择页
 */
class AudioMutiManagerFragment : BaseMVVMRefreshFragment<FragmentSongMutiManagerBinding>(),
    PlayerProgressCallback {


    companion object {
        const val PARAM_SONG = "param_song"
    }

    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentSongMutiManagerBinding {
        return FragmentSongMutiManagerBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioMutiManagerViewModel

    override fun initViewModel() {
        mViewModel = ViewModelProvider(this, AudioMutiManagerViewModel.AudioMutiManagerViewFactory())[AudioMutiManagerViewModel::class.java]
    }


    override fun initView() {
        super.initView()
        viewBinding.toolbar.ImmerseDesign()
        showRefresh() //返回按钮生效
        viewBinding.toolbar.setNavigationOnClickListener {
            mActivity?.finish()
        }
        PlayerManager.addProgressListener(this)
        mAdapter.register(CellAudioItemView(
            {
                mAdapter.notifyDataSetChanged()
            },
            {
                -1
            },
            { song, isSelected ->
                mViewModel.selectSong(song, isSelected)
            },
            source = AudioItemView.Source.SOURCE_MUTIMANAGER,
        ))
        viewBinding.recycleview.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        mActivity?.let {
            mViewModel.initData(it, arguments)
        }
        viewBinding.selectAllIcon.setOnClickListener {
            viewBinding.selectAllIcon.isSelected = !viewBinding.selectAllIcon.isSelected
            mViewModel.selectAll(viewBinding.selectAllIcon.isSelected)
            mAdapter.notifyDataSetChanged()
        }


        viewBinding.shareContainer.setOnClickListener {
            mViewModel.shareSongs(requireContext())
        }
        viewBinding.deleteContainer.setOnClickListener {
            mViewModel.deleteSongs(requireContext())
        }
    }


    override fun startObserve() {
        mViewModel.audioMutiManagerState.observe(viewLifecycleOwner) {
            if (it.songs != null) {
                if (it.songs?.isNotEmpty() == true) {
                    mAdapter.items = it.songs!!
                    mAdapter.notifyDataSetChanged()
                    stopRefresh()
                } else {
                    ToastCompat.makeText(requireContext(), false, "没有找到音频文件").show()
                    stopRefresh()
                }
            }

            if (it.isAllSelected != null) {
                viewBinding.selectAllIcon.isSelected = it.isAllSelected!!
            }
            if (it.isShowBottomActionView != null) {
                viewBinding.bottomActionBg.isVisible = it.isShowBottomActionView!!
                viewBinding.shareContainer.isVisible = it.isShowBottomActionView!!
                viewBinding.deleteContainer.isVisible = it.isShowBottomActionView!!
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        mViewModel.onRefresh(requireContext())
    }

    override fun onProgressChanged(currentWindowIndex: Int, position: Long, duration: Long) {
        try { //            (viewBinding.recycleview.getChildAt(this.position) as? AudioItemView)?.onProgressChanged1(currentWindowIndex, position, duration, this.position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerManager.removeProgressListener()
    }

}