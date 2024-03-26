package com.san.audioeditor.activity

import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.MediaPickFragment
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class MediaPickActivity : BaseFragmentActivity() {


    override fun generateFragment(): Fragment {
        return MediaPickFragment()
    }

    override fun getFragmentTag(): String {
        return MediaPickFragment::class.java.simpleName
    }

}