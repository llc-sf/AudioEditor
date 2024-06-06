package com.san.audioeditor.fragment

import android.text.TextUtils
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.SearchActivity
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentSearchBinding
import com.san.audioeditor.view.AudioItemView
import com.san.audioeditor.view.SongMoreBottomDialog
import com.san.audioeditor.viewmodel.AudioPickViewModel
import com.san.audioeditor.viewmodel.AudioSearchViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.widget.utils.disableItemChangeAnimation
import dev.android.player.widget.cell.MultiTypeAdapter
import dev.audio.timeruler.player.PlayerManager

class SearchFragment : BaseMVVMFragment<FragmentSearchBinding>(), AudioItemView.OnItemListener {

    private val searchModel by lazy {
        ViewModelProvider(requireActivity())[AudioSearchViewModel::class.java]
    }
    var cell = CellAudioItemView(this)
    private var from = SearchActivity.FROM_HOME
    private var directoryPath = AudioPickViewModel.ALL_AUDIO

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            register(cell)
        }
    }

    override fun initView() {
        super.initView()
        getParams()
        viewBinding.rvSearchResult.run {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
        viewBinding.rvSearchResult .disableItemChangeAnimation()
        // 初始化列表数据
        searchModel.searchFromTargetPath("", directoryPath)
    }

    private fun getParams() {
        from = arguments?.getString(SearchActivity.KEY_FROM, from) ?: from
        directoryPath =
            arguments?.getString(SearchActivity.KEY_SONG_DIRECTORY_PATH, directoryPath) ?: directoryPath
    }

    override fun initViewModel() {}

    override fun startObserve() {
        searchModel.mainModel.observe(viewLifecycleOwner) { data ->
            adapter.items = data.isSuccess ?: emptyList()
            updateEmptyView(adapter.items.isEmpty())
            adapter.notifyDataSetChanged()
        }
        searchModel.keyword.observe(viewLifecycleOwner) { keyword ->
            searchModel.searchFromTargetPath(keyword, directoryPath)
        }
    }

    override fun initViewBinding(inflater: LayoutInflater) =
        FragmentSearchBinding.inflate(layoutInflater)

    private fun updateEmptyView(isEmpty: Boolean) {
        viewBinding.groupEmpty.isVisible = isEmpty
        viewBinding.rvSearchResult.isVisible = !isEmpty
        if (isEmpty) {
            searchModel.playingSong = null
            stopPlayIfNeeded()
        }
    }

    private fun stopPlayIfNeeded() {
        if (PlayerManager.isPlaying) PlayerManager.stop()
    }

    override fun onItemClick(position: Int, song: Song) {
        when (from) {
            SearchActivity.FROM_HOME -> {
                PlayerManager.playByPathWithProgress(song.path, true)
                searchModel.playingSong = song
                adapter.notifyDataSetChanged()
            }

            SearchActivity.FROM_PICK -> AudioCutActivity.open(requireContext(), song)
        }
    }

    override fun onCoverClick(position: Int, song: Song) {
        when (from) {
            SearchActivity.FROM_HOME -> {
                if (PlayerManager.isPlaying
                    && TextUtils.equals(searchModel.playingSong?.path, song.path)
                ) {
                    PlayerManager.pause()
                    adapter.notifyItemChanged(position)
                } else {
                    onItemClick(position, song)
                }
            }

            SearchActivity.FROM_PICK -> onItemClick(position, song)
        }
    }

    override fun getPlayingSong(): Song? {
        return searchModel.playingSong
    }

    override fun onMoreClick(position: Int, song: Song) {
        SongMoreBottomDialog.show(requireContext(), song)
    }

    override fun getKeyWord(): String? {
        return searchModel.keyword.value
    }

    override fun getSource(): AudioItemView.Source {
        return AudioItemView.Source.SOURCE_SEARCH
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cell.onDestroy()
    }
}