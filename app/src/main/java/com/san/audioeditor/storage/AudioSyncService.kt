package com.san.audioeditor.storage

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.android.app.AppProvider
import java.util.concurrent.atomic.AtomicBoolean


/**
 * 应用数据库同步服务
 */
class AudioSyncService : IntentService(TAG) {

    companion object {
        const val TAG = "MediaSyncService"

        private const val SYNC_ACTION = "MediaSyncService.SYNC"

        var ACTION_SYNC_COMPLETED = "${AppProvider.context.packageName}.SYNC_COMPLETED"

        /**
         * 启动同步服务
         */
        fun sync(context: Context) {
            try {
                val intent = Intent(context, AudioSyncService::class.java)
                intent.action = SYNC_ACTION
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var isRunning = AtomicBoolean(false)


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            SYNC_ACTION -> {
                if (isRunning.compareAndSet(false, true)) {
                    Log.d(TAG, "onHandleIntent() Start Time = " + System.currentTimeMillis())
                    AudioSyncUtil.sync(this)
                    isRunning.set(false)

                    // 同步完成后发送广播
                    val syncCompletedIntent = Intent(ACTION_SYNC_COMPLETED)
                    sendBroadcast(syncCompletedIntent)

                    stopSelf()
                    Log.d(TAG, "onHandleIntent() End Time = " + System.currentTimeMillis())
                }
            }
        }
    }
}