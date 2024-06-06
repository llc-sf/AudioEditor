package com.san.audioeditor.delete

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.android.player.framework.base.ActivityLifecycleCallbacksAdapter

/**
 * 删除歌曲文件
 */
class DeleteSongAppListener : ActivityLifecycleCallbacksAdapter() {

    private val mContainer = mutableMapOf<Int, DeleteSongPresenterCompat?>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is AppCompatActivity) {
            mContainer[activity.hashCode()] = DeleteSongPresenterCompat(activity)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is AppCompatActivity) {
            mContainer[activity.hashCode()] = null
        }
    }
}