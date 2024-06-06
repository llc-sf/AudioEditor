package com.san.audioeditor.fragment

import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.san.audioeditor.R
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentSongMutiManagerBinding
import com.san.audioeditor.view.AudioItemView
import com.san.audioeditor.view.EmptyViewBusiness
import com.san.audioeditor.viewmodel.AudioMultiManagerViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.framework.utils.KeyboardUtil
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter
import dev.android.player.widget.utils.disableItemChangeAnimation
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback

/**
 * 媒体选择页
 */
class AudioMultiManagerFragment : BaseMVVMRefreshFragment<FragmentSongMutiManagerBinding>(),
    PlayerProgressCallback, AudioItemView.OnItemListener {


    companion object {
        const val PARAM_SONG = "param_song"
    }

    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }

    var cell = CellAudioItemView(this)
    private var currentKeyword: String? = null


    override fun initViewBinding(inflater: LayoutInflater): FragmentSongMutiManagerBinding {
        return FragmentSongMutiManagerBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioMultiManagerViewModel

    override fun initViewModel() {
        mViewModel = ViewModelProvider(this, AudioMultiManagerViewModel.AudioMultiManagerViewFactory())[AudioMultiManagerViewModel::class.java]
    }


    override fun initView() {
        super.initView()
        viewBinding.toolbar.ImmerseDesign()
        showRefresh() //返回按钮生效
        viewBinding.toolbar.setNavigationOnClickListener {
            mActivity?.finish()
        }
        viewBinding.recycleview.disableItemChangeAnimation()
        PlayerManager.addProgressListener(this)
        mAdapter.register(CellAudioItemView(this))
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
            mActivity?.let {
                mViewModel.deleteSongs(it)
            }

        }
        viewBinding.query.run {
            postDelayed({ KeyboardUtil.showKeyBoard(this) }, 500)
            doAfterTextChanged {
                doEditTextChange(it?.trim()?.toString() ?: "")
            }
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtil.hideKeyboardFrom(v)
                    false
                } else {
                    true
                }
            }
        }
        viewBinding.ivClear.setOnClickListener {
            KeyboardUtil.hideKeyboardFrom(viewBinding.query)
            viewBinding.query.setText("")
        }
        updateEditBar("")
    }

    private fun doEditTextChange(text: String) {
        updateEditBar(text)
        currentKeyword = text
        mViewModel.search(text)
    }

    private fun updateEditBar(keyword: String) {
        viewBinding.ivSearch.isVisible = keyword.isEmpty()
        viewBinding.tvSearchHint.isVisible = keyword.isEmpty()
        viewBinding.ivClear.isVisible = keyword.isNotEmpty()
    }


    override fun startObserve() {
        mViewModel.mainModel.observe(viewLifecycleOwner) { uiState ->
            uiState.isSuccess?.let {
                if (it.songs != null) {
                    mAdapter.items = it.songs!!
                    mAdapter.notifyDataSetChanged()
                    updateEmptyView(mAdapter.items.isEmpty())
                    stopRefresh()
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
        mViewModel.selectedSongs.observe(viewLifecycleOwner) {
            viewBinding.toolbar.title = if (it.isEmpty()) {
                getString(R.string.select_audio)
            } else {
                getString(R.string.x_selected, it.size.toString())
            }

        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        viewBinding.groupEmpty.isVisible = isEmpty
        viewBinding.recycleview.isVisible = !isEmpty
        viewBinding.folderSelected.isVisible = !isEmpty
        viewBinding.selectAllIcon.isVisible = !isEmpty
        viewBinding.bottomActionBg.isVisible = !isEmpty
        viewBinding.shareContainer.isVisible = !isEmpty
        viewBinding.deleteContainer.isVisible = !isEmpty
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
        cell.onDestroy()
    }

    override fun onItemClick(position: Int, song: Song) {
        onMoreClick(position, song)
    }

    override fun onCoverClick(position: Int, song: Song) {
        onItemClick(position, song)
    }

    override fun getPlayingSong(): Song? {
        return null
    }

    override fun onMoreClick(position: Int, song: Song) {
        song.isSelected = !song.isSelected
        mViewModel.onAllSelectRefresh()
        mAdapter.notifyItemChanged(position)
    }


    override fun getKeyWord(): String? {
        return currentKeyword
    }


    override fun getSource(): AudioItemView.Source {
        return AudioItemView.Source.SOURCE_MUTIMANAGER
    }

}