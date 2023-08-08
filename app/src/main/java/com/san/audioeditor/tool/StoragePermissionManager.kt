package com.san.audioeditor.tool

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


typealias OnGrant = () -> Unit

/**
 * 存储权限适配
 */
object StoragePermissionManager {
    private const val TAG_FRAGMENT = "InternalFragment"


    @JvmStatic
    fun hasStoragePermissionAdapter33(context: Context?): Boolean {
        return AndroidUtil.hasStoragePermissionAdapter33(context)
    }


    /**
     * 请求权限,如果已经有权限,则不会弹出权限框
     */
    fun onAfterRequestPermission(activity: AppCompatActivity?, grant: OnGrant) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var innerFragment: InternalFragment? = null
            activity?.apply {

                val findFragmentByTag =
                    activity.supportFragmentManager.findFragmentByTag(TAG_FRAGMENT)
                if (findFragmentByTag != null && findFragmentByTag is InternalFragment) {
                    innerFragment = findFragmentByTag
                }
                if (innerFragment == null) {
                    innerFragment = InternalFragment()
                    supportFragmentManager.beginTransaction().add(innerFragment!!, TAG_FRAGMENT)
                        .commit()
                    supportFragmentManager.executePendingTransactions()
                }
                innerFragment?.onRequestPermission(grant)
            }
        } else {
            grant()
        }

    }
}

class InternalFragment : Fragment() {

    private var isFirst: Boolean = true
    private var onGrant: OnGrant? = null

    /**
     * 获取到权限后自动取消当前Fragment
     */
    private val onGrantWrapper = {
        onGrant?.invoke()
        activity?.apply {
            supportFragmentManager.beginTransaction().remove(this@InternalFragment).commit()
        }
    }

    /**
     * 请求存储权限
     */
    private var Manager: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Manager = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                onGrantWrapper()
            } else {
                onShowPermissionDenied()
            }
        }
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    /**
     * 权限拒接弹窗
     */
    private fun onShowPermissionDenied() {

    }


    /**
     * 判断是否应该显示弹窗
     */
    private fun getShouldRequestPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity?.shouldShowRequestPermissionRationale(AndroidUtil.getStoragePermissionPermissionStringAdapter33())
                ?: true
        } else {
            false
        }
    }

    /**
     * 请求权限
     */
    internal fun onRequestPermission(Grant: () -> Unit) {
        this.onGrant = Grant
        onRequestPermissionReal(true)
    }

    /**
     * 真正的请求权限入口
     * @param isFirst 是否是第一次请求权限
     */
    fun onRequestPermissionReal(isFirst: Boolean) {
        if (StoragePermissionManager.hasStoragePermissionAdapter33(activity)) {
            onGrant?.invoke()
        } else {
            if (isFirst) {
            } else {
            }
            this.isFirst = isFirst
            Manager?.launch(AndroidUtil.getStoragePermissionPermissionStringAdapter33())
        }
    }

    /**
     * StoragePermissionDeniedNoTipsDialog 监听到 权限打开
     */
    fun onStoragePermissionGrant() {
        onGrantWrapper()
    }


    override fun onDestroy() {
        super.onDestroy()
        this.onGrant = null
        this.Manager = null
    }
}

