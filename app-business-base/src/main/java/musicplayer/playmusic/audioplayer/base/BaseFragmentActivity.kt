package musicplayer.playmusic.audioplayer.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.android.player.framework.base.BaseActivity

abstract class BaseFragmentActivity : BaseActivity() {

    var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contain_layout)
        try {
            fragment = generateFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.containView, fragment!!, getFragmentTag())
                    .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun generateFragment(): Fragment

    abstract fun getFragmentTag(): String
}