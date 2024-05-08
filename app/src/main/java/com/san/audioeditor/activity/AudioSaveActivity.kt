package com.san.audioeditor.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.AudioSaveFragment
import dev.android.player.framework.data.model.Song
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class AudioSaveActivity : BaseFragmentActivity() {


    companion object {
        const val PARAM_SONG = "param_song"

        fun open(context: Context) {
            val intent = Intent(context, AudioSaveActivity::class.java)
            context.startActivity(intent)
        }

        fun open(context: Context, song: Song) {
            val intent = Intent(context, AudioSaveActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(PARAM_SONG, song)
                })
            }
            context.startActivity(intent)
        }

    }


    override fun generateFragment(): Fragment {
        return AudioSaveFragment().apply {
            arguments = intent.extras
        }
    }

    override fun getFragmentTag(): String {
        return AudioSaveFragment::class.java.simpleName
    }


}