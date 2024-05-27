package com.san.audioeditor.fragment

import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.toast.ToastCompat
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.san.audioeditor.R
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentMediaPickBinding
import com.san.audioeditor.view.FolderSelectedView
import com.san.audioeditor.viewmodel.AudioPickViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.framework.utils.ViewUtils
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter
import dev.audio.timeruler.utils.rotate
import dev.audio.timeruler.weight.CustomPopupWindow

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
        viewBinding.folderSelected.setOnClickListener {
            pop()
        }
        viewBinding.arrow.setOnClickListener {
            pop()
        }

    }

    private var mPopupWindow: PopupWindow? = null

    private fun pop() {
        if (mPopupWindow != null) mPopupWindow!!.dismiss()
        val popup = CustomPopupWindow(requireContext())
        val contentView = FolderSelectedView(requireContext())
        contentView.setData(mViewModel.getDirectoriesBySongs())
        popup.contentView = contentView
        popup.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_222020_radius_all_16_bg))
        popup.isOutsideTouchable = true
        popup.isFocusable = true
        val size = ViewUtils.onMeasure(contentView)
        var xOffset: Int
        xOffset =  -(size[0] + AndroidUtil.dip2px(requireContext(), 10f))
        val yOffset = -(size[1] + viewBinding.folderSelected!!.height)
//        xOffset += 16.dp
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
        mViewModel.mediaPickState.observe(viewLifecycleOwner) {
            if (it.songs?.isNotEmpty() == true) {
                mAdapter.items = it.songs!!
                mAdapter.notifyDataSetChanged()
                stopRefresh()
            } else {
                ToastCompat.makeText(requireContext(), false, "没有找到音频文件").show()
                stopRefresh()
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        mViewModel.onRefresh(requireContext())
    }

}