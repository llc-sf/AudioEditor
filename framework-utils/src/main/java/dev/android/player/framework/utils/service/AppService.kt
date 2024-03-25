package dev.android.player.framework.utils.service

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.functions.Consumer

interface AppService {
    fun getVersionCode(): Int

    fun isUpUser(): Boolean

    fun isNewUser(): Boolean

    fun isAppExit(): Boolean

    fun isFirstLunch(): Boolean

    fun isRelease(): Boolean

    fun getRemoteConfigABTest(context: Context?, key: String?, defaultValue: String?): String?
    fun openVideoManagerPage(context: Context, isGrid: Boolean = false)
    fun showVideoMoreDialog(appCompatActivity: AppCompatActivity, info: Parcelable, playList: List<Parcelable>)

    fun go2ScanVideoPage(context: Context?)


    fun globalScanVideo(context: Context?, callback: (List<Pair<String?, Uri?>>) -> Unit)
    fun shareVideo(context: Context, currentUrl: String)

    fun isPlayingMusic(): Boolean

    fun go2MusicPlayerPage(context: Context?)
    fun playOrPause()

    fun subscribeToMusicPlayPublisher(context: Context, songConsumer: Consumer<android.util.Pair<Long, Boolean>>, errorConsumer: Consumer<Throwable>)

    fun topPageIsMusicPlayer(): Boolean

    fun go2SplashActivity(context: Context)
    fun go2SearchPage(context: Context)
    fun onDialogDismiss(context: Context)
}