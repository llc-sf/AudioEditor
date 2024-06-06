package com.san.audioeditor.delete.dialog

import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogDeleteSongBinding
import dev.android.player.framework.data.model.Song
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager

/**
 * 删除歌曲弹窗
 */
class DeleteSongDialog : BaseBottomTranslucentDialog() {

    companion object {


        private const val TAG = "DeleteSongDialog"

        private const val EXTRA_ARG_SONGS = "EXTRA_ARG_SONGS"

        @JvmStatic
        fun show(activity: AppCompatActivity, songs: List<Song>, callback: Callback? = null) {
            val dialog = DeleteSongDialog()
            dialog.arguments = Bundle().apply {
                putParcelableArrayList(EXTRA_ARG_SONGS, songs as ArrayList<out Parcelable>)
            }
            dialog.setCallback(callback)
            BottomDialogManager.show(activity, dialog)
        }
    }

    private fun setCallback(callback: Callback? = null) {
        this.callback = callback
    }

    //点击删除按钮的回调
    private var callback: Callback? = null


    private val songs: MutableList<Song> by lazy {
        arguments?.getParcelableArrayList<Song>(EXTRA_ARG_SONGS) ?: mutableListOf()
    }


    private var _binding: DialogDeleteSongBinding? = null
    private val binding get() = _binding!!


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogDeleteSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (songs.isEmpty()) {
            dismiss()
            return
        } else {
            if (songs.size == 1) {
                val arg = "'${songs[0].title}'"
                val description = getString(R.string.music4_delete_song_message, arg)
                binding.title.text = SpannableString(description).apply {
                    setSpan(
                            ForegroundColorSpan(resources.getColor(R.color.colorAccent)),
                            description.indexOf(arg),
                            description.indexOf(arg) + arg.length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } else {
//                val arg = ResourceUtils.makeLabel(view.context, R.plurals.songs, songs.size)
                val arg = String.format("%d", songs.size)
                binding.title.text = getString(R.string.music4_delete_songs_message, arg)
            }
        }
        binding.btnCancel.setOnClickListener {
            callback?.onCancel()
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            callback?.onDelete()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    interface Callback {

        fun onDelete()

        fun onCancel()
    }
}