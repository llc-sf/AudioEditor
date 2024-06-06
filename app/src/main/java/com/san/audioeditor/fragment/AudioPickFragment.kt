package com.san.audioeditor.fragment

import android.app.Activity.RESULT_OK
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.toast.ToastCompat
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.san.audioeditor.R
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.SearchActivity
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.sort.AudioSortAdapter
import com.san.audioeditor.sort.SortSelectDialogFragment
import com.san.audioeditor.storage.AudioSyncUtil
import com.san.audioeditor.view.AudioItemView
import com.san.audioeditor.view.FolderSelectedView
import com.san.audioeditor.view.SongMoreBottomDialog
import com.san.audioeditor.viewmodel.AudioPickViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.framework.utils.ViewUtils
import dev.android.player.widget.utils.disableItemChangeAnimation
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.rotate
import dev.audio.timeruler.weight.CustomPopupWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 媒体选择页
 */
class AudioPickFragment : BaseMVVMRefreshFragment<FragmentMediaPickBinding>(),
    AudioItemView.OnItemListener {


    companion object {
        const val PARAM_SONG = "param_song"
    }

    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }

    var cell = CellAudioItemView(this)


    override fun initViewBinding(inflater: LayoutInflater): FragmentMediaPickBinding {
        return FragmentMediaPickBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioPickViewModel
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleSelectedFile(result)
    }

    override fun initViewModel() {
        mViewModel = ViewModelProvider(this, AudioPickViewModel.AudioPickViewFactory())[AudioPickViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        mViewModel.onResume()
    }


    override fun initView() {
        super.initView()
        viewBinding.toolbar.ImmerseDesign()
        showRefresh() //返回按钮生效
        viewBinding.toolbar.setNavigationOnClickListener {
            mActivity?.finish()
        }
        viewBinding.recycleview.disableItemChangeAnimation()
        mAdapter.register(cell)
        viewBinding.recycleview.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        mActivity?.let {
            mViewModel.initData(it, arguments)
        }
        viewBinding.folderSelected.setOnClickListener {
            pop()
        }
        viewBinding.arrow.setOnClickListener {
            pop()
        }
        viewBinding.sort.setOnClickListener {
            SortSelectDialogFragment.show(childFragmentManager, mViewModel.mCurrentSort, AudioSortAdapter()) {
                mViewModel.mCurrentSort = it
                mViewModel.onRefresh(requireContext())
            }
        }
        viewBinding.search.setOnClickListener {
            SearchActivity.open(requireContext(), SearchActivity.FROM_PICK, mViewModel.currentDir?.path
                ?: AudioPickViewModel.ALL_AUDIO)
        }
        viewBinding.recentFolder.setOnClickListener {
            openRecentFiles()
        }


    }

    private fun openRecentFiles() {
        AudioFileUtils.openRecentFiles(openDocumentLauncher)
    }

    private fun handleSelectedFile(result: ActivityResult?) {
        if (result?.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (AudioFileUtils.isAudioFile(uri)) {
                        AudioSyncUtil.getSongFromUri(requireContext(), uri)?.let {
                            withContext(Dispatchers.Main) {
                                AudioCutActivity.open(requireContext(), it)
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                ToastCompat.makeText(context, false, R.string.text_audio_not_supported)
                                    .show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            ToastCompat.makeText(context, false, R.string.text_not_supported_selecting_file)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private var mPopupWindow: PopupWindow? = null

    private fun pop() {
        var dirs = mViewModel.getDirectoriesBySongs()
        if (dirs.isNullOrEmpty()) {
            return
        }
        if (mPopupWindow != null) mPopupWindow!!.dismiss()
        val popup = CustomPopupWindow(requireContext())
        val contentView = FolderSelectedView(requireContext())
        contentView.setData(dirs, mViewModel.currentDir) {
            mViewModel.onFolderSelected(it)
            mPopupWindow?.dismiss()
            viewBinding.folderSelected.text = it.name
        }
        popup.contentView = contentView
        popup.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_222020_radius_all_16_bg))
        popup.isOutsideTouchable = true
        popup.isFocusable = true
        val size = ViewUtils.onMeasure(contentView)
        var xOffset: Int
        xOffset = -(size[0] + AndroidUtil.dip2px(requireContext(), 10f))
        val yOffset = -(size[1] + viewBinding.folderSelected!!.height) //        xOffset += 16.dp
        popup.showAsDropDown(viewBinding.folderSelected, 0, 0, Gravity.LEFT)
        onPopShow()
        popup.setOnDismissListener { onPopDismiss() }
        mPopupWindow = popup
    }

    private fun onPopDismiss() {
        mPopupWindow = null
        viewBinding.arrow.isSelected = !viewBinding.arrow.isSelected
        if (viewBinding.arrow.isSelected) {
            viewBinding.arrow.rotate(0f, 180f, 300)
        } else {
            viewBinding.arrow.rotate(180f, 0f, 300)
        }
    }

    private fun onPopShow() {
        viewBinding.arrow.isSelected = !viewBinding.arrow.isSelected
        if (viewBinding.arrow.isSelected) {
            viewBinding.arrow.rotate(0f, 180f, 300)
        } else {
            viewBinding.arrow.rotate(180f, 0f, 300)
        }
    }

    fun showFolderSelected() {
        folderSelectedClick()
    }

    fun hideFolderSelected() {
        folderSelectedClick()
    }

    fun folderSelectedText(text: String) {
        viewBinding.folderSelected.text = text
    }

    private fun folderSelectedClick() {


    }

    override fun startObserve() {
        mViewModel.mainModel.observe(viewLifecycleOwner) { uiState ->
            uiState.isSuccess?.let {
                if (it.songs != null) {
                    if (it.songs?.isNullOrEmpty() == true) {
                        ToastCompat.makeText(requireContext(), false, "没有找到音频文件").show()
                        stopRefresh()
                    } else {
                        mAdapter.items = it.songs!!
                        mAdapter.notifyDataSetChanged()
                        stopRefresh()
                    }

                }
                if (it.notifyItemChangedState != null) {
                    if (it.notifyItemChangedState!!.position == -1) {
                        mAdapter.notifyDataSetChanged()
                    } else if (it.notifyItemChangedState!!.position < mAdapter.itemCount && it.notifyItemChangedState!!.position >= 0) {
                        mAdapter.notifyItemChanged(it.notifyItemChangedState!!.position)
                    }
                }
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        mViewModel.onRefresh(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.onDestroy()
        cell.onDestroy()
    }

    override fun onItemClick(position: Int, song: Song) {
        mViewModel.onItemClick(requireContext(), position, song)
    }

    override fun onCoverClick(position: Int, song: Song) {
        mViewModel.onCoverClick(position, song)
    }

    override fun getPlayingSong(): Song? {
        return mViewModel.playingSong
    }


    override fun onMoreClick(position: Int, song: Song) {
        SongMoreBottomDialog.show(requireContext(), song)
    }

    override fun getKeyWord(): String? {
        return null
    }

    override fun getSource(): AudioItemView.Source {
        return AudioItemView.Source.SOURCE_PICK
    }


}