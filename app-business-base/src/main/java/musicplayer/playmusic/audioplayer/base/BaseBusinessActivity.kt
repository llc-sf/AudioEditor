package musicplayer.playmusic.audioplayer.base

import android.os.Bundle
import dev.android.player.framework.base.BaseActivity

/**
 * 业务类型的基础Activity,增加对 播放服务的绑定操作
 */
open class BaseBusinessActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}