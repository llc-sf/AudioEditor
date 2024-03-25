package musicplayer.playmusic.audioplayer.base

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.widget.Toast
import android.widget.toast.ToastCompat
import dev.android.player.business.service.*
import dev.android.player.framework.base.ActivityStackManager
import dev.android.player.framework.base.BaseActivity
import dev.android.player.framework.rx.add
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.LogUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * 业务类型的基础Activity,增加对 播放服务的绑定操作
 */
open class BaseBusinessActivity : BaseActivity(), ServiceConnection, MusicStateListener {

    private val TAG = "BaseBusinessPlayActivity"

    private val mMusicStateListener = mutableListOf<MusicStateListener?>()

    private var mToken: MusicPlayer.ServiceToken? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidUtil.hasStoragePermissionAdapter33(this)) {
            bindService()
        }
        registerEventReceiver()
    }


    override fun onResume() {
        super.onResume()
        if (AndroidUtil.hasStoragePermissionAdapter33(this)) {
            bindService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMusicStateListener.clear()
        unbindService()
    }


    protected fun bindService() {
        if (mToken == null) {
            mToken = MusicPlayer.bindToService(this, this)
        }
    }

    protected fun unbindService() {
        // Unbind from the service
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken)
            mToken = null
        }
    }


    private fun registerEventReceiver() {
        //初始化音乐播放器
        Session.registerReceiver()
        Session.eventReceiverPublisher
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ intent ->
                    val action = intent.getAction()
                    if (action == ConstantsActions.META_CHANGED) {
                        onMetaChanged()
                    } else if (action == ConstantsActions.PLAY_STATE_CHANGED) {
                        onMetaChanged()
                    } else if (action == ConstantsActions.REFRESH) {
                        onRestartLoader()
                    } else if (action == ConstantsActions.PLAYLIST_CHANGED) {
                        onPlaylistChanged()
                    } else if (action == ConstantsActions.TRACK_ERROR) {
                        val throwable = intent.getSerializableExtra(TrackErrorExtra.TRACK_ERROR) as? Throwable
                        val name = intent.getStringExtra(TrackErrorExtra.TRACK_NAME)
                        val msg = TrackErrorHandler.getErrorTrackMessage(this, throwable, name)
                        onShowErrorMsg(msg)
                    }
                }) { error -> LogUtils.logException(TAG, "PlayerPresenter: Error sending broadcast", error) }
                .add(this)
    }

    private fun onShowErrorMsg(msg: String?) {
        if (!TextUtils.isEmpty(msg) && ActivityStackManager.getInstance().currentActivity() == this) {
            ToastCompat.makeText(this, false, msg, Toast.LENGTH_SHORT).show()
        }
    }

    /*===== MusicStateListener =====*/
    override fun onMetaChanged() {
        for (listener in mMusicStateListener) {
            listener?.onMetaChanged()
        }
    }


    override fun onRestartLoader() {
        for (listener in mMusicStateListener) {
            listener?.onRestartLoader()
        }
    }

    override fun onPlaylistChanged() {
        for (listener in mMusicStateListener) {
            listener?.onPlaylistChanged()
        }
    }

    override fun onServiceConnected() {
        for (listener in mMusicStateListener) {
            listener?.onServiceConnected()
        }
        onDoubleCheckState()
    }

    private fun onDoubleCheckState() {
        try {
            //如果Session中没有音乐，则再次刷新一下
            if (Session.currentSong == null) {
                val intent = Intent(this, PlayerService::class.java)
                intent.action = ConstantsActions.UI_RECREATED
                startService(intent)
            }
        } catch (e: Exception) {
            LogUtils.logException(TAG, "onDoubleCheckState", e)
        }
    }

    fun setMusicStateListenerListener(status: MusicStateListener?) {
        if (status === this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (status != null) {
            mMusicStateListener.add(status)
        }
    }

    fun removeMusicStateListenerListener(status: MusicStateListener?) {
        if (status != null) {
            mMusicStateListener.remove(status)
        }
    }


    /*===== ServiceConnection =====*/
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        onMetaChanged()
        onServiceConnected()
    }


    override fun onServiceDisconnected(name: ComponentName?) {
    }
}