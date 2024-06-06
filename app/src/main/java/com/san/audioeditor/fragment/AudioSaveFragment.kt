package com.san.audioeditor.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.toast.ToastCompat
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.R
import com.san.audioeditor.activity.FAQActivity
import com.san.audioeditor.activity.MainActivity
import com.san.audioeditor.business.ShareBusiness
import com.san.audioeditor.databinding.FragmentAudioSaveBinding
import com.san.audioeditor.viewmodel.AudioSaveViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.ImmerseDesign
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.utils.AudioFileUtils
import musicplayer.playmusic.audioplayer.base.loader.load

/**
 * 媒体选择页
 */
class AudioSaveFragment : BaseMVVMFragment<FragmentAudioSaveBinding>() {


    companion object {
        const val PARAM_SONG = "param_song"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentAudioSaveBinding {
        return FragmentAudioSaveBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioSaveViewModel
    override fun initViewModel() {
        mViewModel = AudioSaveViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_audio_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_share -> {
                if (!TextUtils.isEmpty(mViewModel.song?.path)) {
                    ShareBusiness.shareTrack(requireContext(), mViewModel.song?.path)
                }
            }

            R.id.action_question -> FAQActivity.open(requireContext(), FAQActivity.OPEN_FROM_SAVE)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun initView() {
        super.initView()
        (activity as? AppCompatActivity)?.setSupportActionBar(viewBinding.toolbar)
        viewBinding.toolbar.ImmerseDesign()
        viewBinding.toolbar.setNavigationOnClickListener {
            mActivity?.finish()
        }
        mViewModel.initData(requireContext(), arguments)
        viewBinding.play.setOnClickListener {
            if (PlayerManager.isPlaying) {
                PlayerManager.pause()
                viewBinding.play.setImageResource(R.drawable.ic_item_play)
            } else {
                PlayerManager.playByPathWithProgress(mViewModel.song?.path ?: "", true)
                viewBinding.play.setImageResource(R.drawable.ic_item_pause)
            }
        }
        PlayerManager.playByPath(mViewModel.song?.path ?: "", false)
        PlayerManager.seekTo(0)
        viewBinding.progressContainer.addProgressListener()
        viewBinding.output.setOnClickListener { it ->
            it.context.startActivity(Intent(it.context, MainActivity::class.java).apply {
                putExtra(MainActivity.PARAM_INDEX, MainActivity.INDEX_FRAGMENT_OUTPUT)
            })
            activity?.finish()
        }
        viewBinding.rename.setOnClickListener {
            mViewModel.rename(activity?.supportFragmentManager)
        }

    }

    override fun startObserve() {
        mViewModel.mainModel.observe(viewLifecycleOwner) { uiState ->
            uiState.isSuccess?.let {
                if (it.song != null) {
                    viewBinding.audioTitle.text = it.song!!.title
                    viewBinding.audioInfo.text = "${DateUtil.formatTime((it.song!!.duration).toLong())} | ${FileUtils.getFileSize(it.song!!.size)} | ${
                        AudioFileUtils.getExtension(it.song!!.path).uppercase()
                    }"
                    viewBinding.progressContainer.setData(it.song!!)
                    viewBinding.audioImg.load(it.song)
                }
                if (it.renameResult != null) {
                    if (it.renameResult == true) {
                        ToastCompat.makeText(requireContext(), true, "重命名成功").show()
                        viewBinding.audioTitle.text = it.song!!.title
                        var position = PlayerManager.getCurrentPosition()
                        var isPlaying = PlayerManager.isPlaying
                        PlayerManager.playByPath(it.song!!.path, false)
                        PlayerManager.seekTo(position)
                        if (isPlaying) {
                            PlayerManager.play()
                        }
                    } else {
                        ToastCompat.makeText(requireContext(), true, "重命名失败").show()
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerManager.stop()
    }

}