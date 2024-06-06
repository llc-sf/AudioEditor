package com.san.audioeditor.activity

import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.AudioMultiManagerFragment
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class MultiManagerActivity : BaseFragmentActivity() {


    override fun generateFragment(): Fragment {
        return AudioMultiManagerFragment()
    }

    override fun getFragmentTag(): String {
        return AudioMultiManagerFragment::class.java.simpleName
    }

}