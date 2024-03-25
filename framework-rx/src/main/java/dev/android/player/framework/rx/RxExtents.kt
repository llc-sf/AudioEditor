package dev.android.player.framework.rx

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

object DisposableMap : Handler.Callback {

    private const val MSG_DISPOSE = 0

    private val mSourceMap = hashMapOf<Int, CompositeDisposable?>()

    private val DisposableThreadHandler by lazy { HandlerThread("RxDisposableThread") }

    private var DisposableHandler: Handler? = null


    private fun getDisposableHandler(): Handler {
        if (DisposableHandler == null) {
            DisposableThreadHandler.start()
            DisposableHandler = Handler(DisposableThreadHandler.looper, this)
        }
        return DisposableHandler!!
    }

    @JvmStatic
    fun add(any: Any?, disposable: Disposable) {
        any?.apply {
            if (mSourceMap[hashCode()] == null) {
                mSourceMap[hashCode()] = CompositeDisposable()
            }
            mSourceMap[hashCode()]?.add(disposable)
        }
    }

    @JvmStatic
    fun dispose(any: Any?, delay: Long = 0) {
        val msg = getDisposableHandler().obtainMessage(MSG_DISPOSE)
        msg.obj = any
        getDisposableHandler().sendMessageDelayed(msg, delay)
//            any?.apply {
//                mSourceMap[hashCode()]?.dispose()
//                mSourceMap[hashCode()] = null
//            }
//        }, delay)
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_DISPOSE -> {
                val any = msg.obj
                any?.apply {
                    mSourceMap[hashCode()]?.dispose()
                    mSourceMap[hashCode()] = null
                }
                Log.d("DisposableMap", "handleMessage() called with: msg = $msg any = $any")
            }
        }
        return true
    }
}

fun Disposable.add(dialog: DialogFragment?) {
    DisposableMap.add(dialog, this)
}

fun Disposable.add(fragment: Fragment?) {
    DisposableMap.add(fragment, this)
}

fun Disposable.add(context: Context?) {
    DisposableMap.add(context, this)
}


inline fun <T : Any> Flowable<T>.ioMain(): Flowable<T> = this.compose(RxIOMainCompose())

inline fun <T : Any> Observable<T>.ioMain(): Observable<T> = this.compose(RxIOMainCompose())

inline fun <T : Any> Single<T>.ioMain(): Single<T> = this.compose(RxIOMainCompose())

/**
 *  如果是33以上的版本，使用更新的api，否则使用旧的api
 *  @param key
 *  @param clazz eg: Song::class.java
 */
fun <T : Parcelable> Intent.getParcelable(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, clazz)
    } else {
        getParcelableExtra<T>(key)
    }
}

/**
 * 创建与activity生命周期绑定的ViewModel
 */
fun <T : ViewModel> ComponentActivity.getVM(modelClass: Class<T>): T {
    val provider = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
    return provider[modelClass]
}