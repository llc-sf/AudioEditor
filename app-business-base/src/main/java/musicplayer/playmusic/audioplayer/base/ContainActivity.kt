package musicplayer.playmusic.audioplayer.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

/**
 *  fragment 壳子
 *
 */
open class ContainActivity : BaseBusinessActivity() {

    lateinit var mCurrentFragment: Fragment

    companion object {
        private const val EXTRA_FRAGMENT = "EXTRA_FRAGMENT"

        fun start(context: Context, clazz: Class<*>, params: Bundle?, activityResultLauncher: ActivityResultLauncher<Intent>) {
            activityResultLauncher.launch(newIntent(clazz, context, params))
        }


        fun start(context: Context?, clazz: Class<*>, params: Bundle?) {
            if (context != null && Fragment::class.java.isAssignableFrom(clazz)) {
                context.startActivity(newIntent(clazz, context, params))
            }
        }

        fun start(activity: Activity, clazz: Class<*>, params: Bundle) {
            start(activity as Context, clazz, params)
        }

        fun start(fragment: Fragment?, clazz: Class<*>, params: Bundle) {
            if (fragment != null) {
                start(fragment.activity as Context, clazz, params)
            }
        }

        fun start(context: Context, clazz: Class<*>) {
            start(context, clazz, null)
        }


        private fun newIntent(clazz: Class<*>, context: Context, bundle: Bundle?): Intent {
            val intent = Intent(context, getMyClass())
            intent.putExtra(EXTRA_FRAGMENT, clazz.name)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            return intent
        }

        private fun getMyClass(): Class<*> {
            return ContainActivity::class.java

        }
    }

    public fun getCurrentFragment(): Fragment {
        return mCurrentFragment
    }

    private var mFragmentClazz: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contain_layout)
        replaceFragment()
    }


    private fun replaceFragment() {
        try {
            mFragmentClazz = intent.getStringExtra(EXTRA_FRAGMENT)
            mCurrentFragment = Fragment.instantiate(this, mFragmentClazz!!, intent.extras)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.containView, mCurrentFragment)
            transaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCurrentFragment.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
