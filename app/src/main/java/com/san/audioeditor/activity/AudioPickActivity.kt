package com.san.audioeditor.activity

import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.AudioPickFragment
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class AudioPickActivity : BaseFragmentActivity() {


    override fun generateFragment(): Fragment {
        return AudioPickFragment()
    }

    override fun getFragmentTag(): String {
        return AudioPickFragment::class.java.simpleName
    }

}