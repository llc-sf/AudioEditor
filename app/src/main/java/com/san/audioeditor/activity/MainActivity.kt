package com.san.audioeditor.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.san.audioeditor.R
import com.san.audioeditor.databinding.ActivityMainBinding
import com.san.audioeditor.fragment.IndexFragment
import com.san.audioeditor.storage.AudioSyncService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object{
        var returnActivityB = false
    }



    init {
        System.loadLibrary("media-handle")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AudioSyncService.sync(this)
        showFragment()
    }

    override fun onResume() {
        super.onResume()
        if(returnActivityB) {
            returnActivityB = false
            AudioCutActivity.open(this)
        }
    }

    private fun showFragment() {
        try {
            clearPopBackStack()
            val manager = supportFragmentManager
            val transaction = manager.beginTransaction()
            //获取旧的Fragment 保持Fragment堆栈干净
            val oldFragment = manager.findFragmentById(R.id.fragment_container)
            if (oldFragment != null) {
                transaction.remove(oldFragment)
            }
            val f = manager.findFragmentByTag(IndexFragment::class.java.simpleName)
            if (f != null) {
                transaction.remove(f)
            }
            Log.d("MainFragment", "showLocalSongs() called")
            transaction.replace(
                R.id.fragment_container,
                IndexFragment(),
                IndexFragment::class.java.simpleName
            )
                .commitNowAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearPopBackStack() {
        try {
            val manager = supportFragmentManager
            if (manager.backStackEntryCount > 0) {
                val entry = manager.getBackStackEntryAt(0)
                manager.popBackStack(entry.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * A native method that is implemented by the 'audioeditor' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

}