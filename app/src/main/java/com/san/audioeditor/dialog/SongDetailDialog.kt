package com.san.audioeditor.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.san.audioeditor.databinding.DialogSongDetailBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager

/**
 * 歌曲详情
 */
class SongDetailDialog : BaseBottomTranslucentDialog() {

    companion object {

        private const val TAG = "SongDetailDialog"

        private const val EXTRA_SONG = "extra_song"

        @JvmStatic
        fun show(manger: FragmentManager, song: Song?) {
            val dialog = SongDetailDialog()
            dialog.arguments = bundleOf(EXTRA_SONG to song)
            BottomDialogManager.show(manger, dialog)
        }
    }

    private val mSong: Song? by lazy {
        arguments?.getParcelable(EXTRA_SONG)
    }


    private var _binding: DialogSongDetailBinding? = null
    private val binding get() = _binding!!


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSongDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.songTitleValue.text = mSong?.title ?: ""
        binding.songAlbumValue.text = mSong?.albumName ?: ""
        binding.songArtistValue.text = mSong?.artistName ?: ""
        binding.songGenreValue.text = mSong?.genre ?: ""
        binding.songDurationValue.text = DateUtil.formatTime(mSong?.duration?.toLong() ?: 0L)
        binding.songSizeValue.text = FileUtils.getFileSize(mSong?.size ?: 0L)
        binding.songLocationValue.text = mSong?.path ?: ""


        binding.btnConfirmOk.setOnClickListener {
            TrackerMultiple.onEvent("More_Songs","Details_OK")
            dismiss()
        }

        binding.btnEdit.setOnClickListener {
            TrackerMultiple.onEvent("More_Songs","Details_Edit")
            mSong?.let {

            }
            dismiss()
        }
    }


}