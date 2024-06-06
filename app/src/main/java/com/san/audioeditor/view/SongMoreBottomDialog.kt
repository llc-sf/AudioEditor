package com.san.audioeditor.view

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.children
import com.android.app.AppProvider
import com.san.audioeditor.R
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.business.ShareBusiness
import com.san.audioeditor.databinding.DialogSongMoreBinding
import com.san.audioeditor.delete.DeleteAction
import com.san.audioeditor.delete.DeleteSongPresenterCompat
import com.san.audioeditor.dialog.RenameAudioDialog
import com.san.audioeditor.dialog.SongDetailDialog
import com.san.audioeditor.storage.AudioSyncService
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.rx.ioMain
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager
import dev.audio.timeruler.utils.AudioFileUtils
import io.reactivex.rxjava3.core.Flowable
import musicplayer.playmusic.audioplayer.base.loader.load
import java.io.Serializable

/**
 * 歌曲更多选项
 */
class SongMoreBottomDialog : BaseBottomTranslucentDialog(), View.OnClickListener {

    companion object {

        private const val TAG = "SongMoreDialogBottomDialog"

        private const val EXTRA_ARG_SONG = "extra_arg_song"
        private const val EXTRA_ARG_SOURCE = "extra_arg_source"

        @JvmStatic
        fun show(context: Context?, song: Song?, source: Serializable? = null) {
            if (context == null || context !is AppCompatActivity || song == null) {
                return
            }
            val dialog = SongMoreBottomDialog()
            dialog.arguments = bundleOf(EXTRA_ARG_SONG to song, EXTRA_ARG_SOURCE to source)
            BottomDialogManager.show(context, dialog)
        }
    }

    private var _binding: DialogSongMoreBinding? = null
    private val binding get() = _binding!!


    private val mSong: Song? by lazy {
        arguments?.getParcelable(EXTRA_ARG_SONG)
    }

    private val mSource: Serializable? by lazy {
        arguments?.getSerializable(EXTRA_ARG_SOURCE)
    }


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSongMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (mSource) { //            is Album -> {
            //                binding.container.removeView(binding.container.findViewById(R.id.action_more_go_to_album))
            //            }
        } //为每个item设置点击事件
        binding.container.children.forEach {
            if (it is MoreActionItem) {
                it.setOnClickListener(this)
            }
        }
        binding.share.setOnClickListener(this)
        binding.info.setOnClickListener(this)

        mSong?.apply {
            binding.title.text = title
            binding.description.text = "${DateUtil.formatTime((this.duration).toLong())} | ${FileUtils.getFileSize(this.size)} | ${
                AudioFileUtils.getExtension(this.path).uppercase()
            }"
        }
        binding.cover.load(mSong)
        binding.close.setOnClickListener {
            dismiss()
        }
        TrackerMultiple.onEvent("More_Songs", "PV")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.share -> {
                if (!TextUtils.isEmpty(mSong?.path)) {
                    ShareBusiness.shareTrack(v.context, mSong?.path)
                }
            }

            R.id.info -> {
                val manager = (activity as AppCompatActivity).supportFragmentManager ?: return
                SongDetailDialog.show(manager, mSong!!)
            }

            R.id.action_rename -> {
                mSong?.let {
                    RenameAudioDialog.show(activity?.supportFragmentManager, it)
                }
            }

            R.id.action_trim -> {
                mSong?.let {
                    AudioCutActivity.open(v.context, it)
                }
            }

            R.id.action_delete -> {
                Flowable.fromCallable {
                    DeleteSongPresenterCompat.onDelete(DeleteAction(activity, mutableListOf(mSong)))
                }.flatMap { it ->
                    DeleteSongPresenterCompat.getDeleteActionSuccess()
                }.ioMain().subscribe {
                    AudioSyncService.sync(AppProvider.context)
                }
            }
        }
        dismiss()
    }
}