package com.san.audioeditor.activity

import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.AudioMutiManagerFragment
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class MutiManagerActivity : BaseFragmentActivity() {


    override fun generateFragment(): Fragment {
        return AudioMutiManagerFragment()
    }

    override fun getFragmentTag(): String {
        return AudioMutiManagerFragment::class.java.simpleName
    }

}