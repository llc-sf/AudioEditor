package com.san.audioeditor.delete.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogDeleteSongsResultBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.ResourceUtils
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager

/**
 * 删除歌曲时，部分删除成功，部分删除失败的弹窗
 */
class DeleteResultDialog : BaseBottomTranslucentDialog() {


    companion object {

        private const val EXTRA_SUCCESS_SONGS = "extra_success_songs"

        /**
         * @param success 成功删除的歌曲
         */
        @JvmStatic
        fun show(activity: AppCompatActivity, success: List<Song>, callback: ISAFPermissionRequest?) {
            val dialog = DeleteResultDialog()
            dialog.arguments = bundleOf(EXTRA_SUCCESS_SONGS to success)
            dialog.setCallback(callback)
            BottomDialogManager.show(activity, dialog)
        }
    }

    private var callback: ISAFPermissionRequest? = null

    private fun setCallback(callback: ISAFPermissionRequest?) {
        this.callback = callback
    }


    private var _binding: DialogDeleteSongsResultBinding? = null
    private val binding get() = _binding!!


    private val mSuccessSongs by lazy {
        arguments?.getSerializable(EXTRA_SUCCESS_SONGS) as? List<Song> ?: emptyList()
    }


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogDeleteSongsResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //无权限提示文字
        var colorString = "#EE294D"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.messageDetail.text = Html.fromHtml(getString(R.string.music4_delete_permission_access).replace("#7D92FF", colorString), Html.FROM_HTML_MODE_LEGACY)
        } else {
            binding.messageDetail.text = Html.fromHtml(getString(R.string.music4_delete_permission_access).replace("#7D92FF", colorString))
        }

        //设置删除成功的歌曲数量
        binding.message.text = buildSpannedString {
            var colorString = "#EE294D"
            val msg = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(getString(R.string.music4_delete_some_no_premission).replace("#7D92FF", colorString), Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(getString(R.string.music4_delete_some_no_premission).replace("#7D92FF", colorString))
            }
            append(ResourceUtils.makeLabel(context, R.plurals.NNNtracksdeleted, mSuccessSongs.size))
            append("\n")
            append(msg)
        }

        binding.btnOk.setOnClickListener {
            callback?.onDismissCancel()
            TrackerMultiple.onEvent("Delete", "PartFailed_OK")
            dismiss()
        }
        binding.btnAccess.setOnClickListener {
            TrackerMultiple.onEvent("Delete", "PartFailed_Allow")
            callback?.onAccessPermissionsRequest()
            dismiss()
        }
        TrackerMultiple.onEvent("Delete", "PartFailed_PV")
    }

    override fun onCancel(dialog: DialogInterface) {
        callback?.onDismissCancel()
        super.onCancel(dialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        callback = null
    }
}