package com.san.audioeditor.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.san.audioeditor.fragment.AudioCutEditorFragment
import dev.android.player.framework.data.model.Song
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.player.PlayerManager
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class AudioCutActivity : BaseFragmentActivity() {


    companion object {
        const val PARAM_SONG = "param_song"
        const val PARAM_AUDIO = "param_audio"


        fun open(context: Context, song: Song) {
            val intent = Intent(context, AudioCutActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(PARAM_SONG, song)
                })
            }
            context.startActivity(intent)
        }

        fun open(context: Context, audioFragmentBean: AudioFragmentBean) {
            val intent = Intent(context, AudioCutActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(PARAM_AUDIO, audioFragmentBean)
                })
            }
            context.startActivity(intent)
        }
    }

    override fun generateFragment(): Fragment {
        return AudioCutEditorFragment().apply {
            arguments = intent.extras
        }
    }

    override fun getFragmentTag(): String {
        return AudioCutEditorFragment::class.java.simpleName
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        (fragment as? AudioCutEditorFragment)?.onNewIntent(intent)
    }

    override fun onDestroy() {
        PlayerManager.releasePlayer()
        fragment?.let {
            PlayerManager.removeListener(it as AudioCutEditorFragment)
        }
        super.onDestroy()
    }

}