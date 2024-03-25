package dev.android.player.framework.rx

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * RxJava 绑定Activity生命周期，或者绑定Fragment生命周期
 */
class RxDisposedLifecycle : Application.ActivityLifecycleCallbacks,
        FragmentManager.FragmentLifecycleCallbacks() {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is AppCompatActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(this, true)
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        DisposableMap.dispose(activity)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        if (f is DialogFragment) {
            DisposableMap.dispose(f, 5000)
        } else {
            DisposableMap.dispose(f)
        }
    }
}