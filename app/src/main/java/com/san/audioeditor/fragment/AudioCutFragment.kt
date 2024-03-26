package com.san.audioeditor.fragment

import android.view.LayoutInflater
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.databinding.FragmentAudioCutBinding
import com.san.audioeditor.viewmodel.AudioCutViewModel
import dev.android.player.framework.base.BaseMVVMFragment
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log

class AudioCutFragment : BaseMVVMFragment<FragmentAudioCutBinding>() {


    companion object {
        const val TAG = "AudioCutFragment"
    }


    override fun initViewBinding(inflater: LayoutInflater): FragmentAudioCutBinding {
        return FragmentAudioCutBinding.inflate(inflater)
    }

    private lateinit var mViewModel: AudioCutViewModel
    override fun initViewModel() {
        var song = arguments?.getSerializable(AudioCutActivity.PARAM_SONG)
        Log.i(TAG, "song: $song")
        if (song is Song) {
            mViewModel =
                AudioCutViewModel(arguments?.getSerializable(AudioCutActivity.PARAM_SONG) as Song)
        } else {
            activity?.finish()
        }
    }

    override fun initData() {
        super.initData()
    }


    override fun initView() {
        super.initView()
    }

    override fun startObserve() {
        mViewModel.audioCutState.observe(viewLifecycleOwner) {
        }
    }

}