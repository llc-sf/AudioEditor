package com.san.audioeditor.activity

import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.AudioSaveFragment
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class AudioSaveActivity : BaseFragmentActivity() {


    override fun generateFragment(): Fragment {
        return AudioSaveFragment()
    }

    override fun getFragmentTag(): String {
        return AudioSaveFragment::class.java.simpleName
    }

}