package com.san.audioeditor.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.toast.ToastCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.R
import com.san.audioeditor.activity.MultiManagerActivity
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.cell.CellOutputEmptyView
import com.san.audioeditor.databinding.FragmentOutputBinding
import com.san.audioeditor.sort.AudioSortAdapter
import com.san.audioeditor.sort.SortSelectDialogFragment
import com.san.audioeditor.view.AudioItemView
import com.san.audioeditor.view.SongMoreBottomDialog
import com.san.audioeditor.viewmodel.AudioOutputViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter
import dev.android.player.widget.utils.disableItemChangeAnimation
import dev.audio.timeruler.player.PlayerManager

class OutputFragment : BaseMVVMRefreshFragment<FragmentOutputBinding>(),
    AudioItemView.OnItemListener {


    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }

    private lateinit var mViewModel: AudioOutputViewModel

    var cell = CellAudioItemView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onRefresh() {
        super.onRefresh()
        mViewModel.onRefresh(requireContext())
    }

    override fun initViewBinding(inflater: LayoutInflater): FragmentOutputBinding {
        return FragmentOutputBinding.inflate(inflater)
    }

    override fun initViewModel() {
        mViewModel = ViewModelProvider(this, AudioOutputViewModel.AudioOutputViewFactory())[AudioOutputViewModel::class.java]
    }


    override fun initView() {
        super.initView()
        mActivity?.let {
            mViewModel.initData(it, arguments)
        }
        showRefresh()
        viewBinding.recycleview.disableItemChangeAnimation()
        mAdapter.register(cell)
        mAdapter.register(CellOutputEmptyView())
        viewBinding.recycleview.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        viewBinding.multiply.setOnClickListener {
            mActivity?.let {
                it.startActivity(Intent(it, MultiManagerActivity::class.java))
            }
        }
        viewBinding.sort.setOnClickListener {
            SortSelectDialogFragment.show(childFragmentManager, mViewModel.mCurrentSort, AudioSortAdapter()) {
                mViewModel.mCurrentSort = it
                mViewModel.onRefresh(requireContext())
            }
        }
        viewBinding.playIcon.setOnClickListener {
            if (PlayerManager.isPlaying) {
                PlayerManager.pause()
            } else {
                PlayerManager.play()
            }
            mAdapter.notifyDataSetChanged()
            refreshPlayState()
        }

        viewBinding.recycleview.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { // 下滑
                    onScrollUp();
                } else if (dy < 0) { // 上滑
                    onScrollDown();
                }
            }

            private fun onScrollUp() {
                viewBinding.playContainer.isVisible = false
            }

            private fun onScrollDown() {
                if (mViewModel.isExpend) {
                    viewBinding.playContainer.isVisible = true
                }
            }
        })
    }

    override fun startObserve() {
        mViewModel.mainModel.observe(viewLifecycleOwner) { uiState ->
            uiState.isSuccess?.let {
                if (it.songs != null) {
                    if (it.songs.isNullOrEmpty()) {
                        ToastCompat.makeText(requireContext(), false, "没有找到音频文件").show()
                        mAdapter.items = mutableListOf<Any>().apply {
                            add(CellOutputEmptyView.OutputEmptyBean())
                        }
                        mAdapter.notifyDataSetChanged()
                    } else {
                        mAdapter.items = it.songs!!
                        mAdapter.notifyDataSetChanged()
                        viewBinding.audioCount.text = "${it.songs?.size} audio"
                    }
                    stopRefresh()
                }

                if (it.notifyItemChangedState != null) {
                    if (it.notifyItemChangedState!!.position == -1) {
                        mAdapter.notifyDataSetChanged()
                    } else if (it.notifyItemChangedState!!.position < mAdapter.itemCount && it.notifyItemChangedState!!.position >= 0) {
                        mAdapter.notifyItemChanged(it.notifyItemChangedState!!.position)
                    }
                    refreshPlayState()
                }
            }
        }
    }

    private fun refreshPlayState() {
        viewBinding.playIcon.setImageResource(if (PlayerManager.isPlaying) R.drawable.ic_item_pause else R.drawable.ic_item_play)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.onResume()
    }

    override fun getPlayingSong(): Song? {
        return mViewModel.playingSong
    }

    override fun onItemClick(position: Int, song: Song) {
        mViewModel.onItemClick(requireContext(), position, song)
    }

    override fun onMoreClick(position: Int, song: Song) {
        SongMoreBottomDialog.show(requireContext(), song)
    }

    override fun onCoverClick(position: Int, song: Song) {
        viewBinding.playContainer.isVisible = true
        mViewModel.onCoverClick(position, song)
    }

    override fun getKeyWord(): String? {
        return null
    }

    override fun getSource(): AudioItemView.Source {
        return AudioItemView.Source.SOURCE_OUTPUT
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cell.onDestroy()
        mViewModel.onDestroy()
    }

}