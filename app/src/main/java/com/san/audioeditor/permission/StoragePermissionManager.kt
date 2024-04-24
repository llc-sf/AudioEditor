package com.san.audioeditor.permission

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.app.AppProvider
import com.zjsoft.simplecache.SharedPreferenceV2
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.TrackerMultiple


typealias OnGrant = () -> Unit
typealias OnDeny = () -> Unit


/**
 * 存储权限适配
 */
object StoragePermissionManager {
    private const val TAG_FRAGMENT = "InternalFragment"


    private val preferences by lazy {
        SharedPreferenceV2(AppProvider.get().getSharedPreferences("StoragePermissionManager", Context.MODE_PRIVATE))
    }

    /**
     * 是否有存储权限
     */
    @JvmStatic
    fun hasStoragePermission(context: Context?, permissionString: String): Boolean {
        return AndroidUtil.hasPermission(context, permissionString)
    }

    @JvmStatic
    fun isRejectStoragePermission(context: Context?, permissionString: String): Boolean {
        when (permissionString) {
            AndroidUtil.getStoragePermissionPermissionString() -> {
                return preferences.getBoolean("isRejectStoragePermission", false)
            }

            AndroidUtil.getVideoPermissionPermissionStringAdapter33() -> {
                return preferences.getBoolean("isRejectVideoPermission", false)
            }
        }
        return false
        //是否拒绝过权限
    }

    internal fun setRejectStoragePermission(isReject: Boolean, permissionString: String) {
        when (permissionString) {
            AndroidUtil.getStoragePermissionPermissionString() -> {
                preferences.edit().putBoolean("isRejectStoragePermission", isReject).apply()
            }

            AndroidUtil.getVideoPermissionPermissionStringAdapter33() -> {
                preferences.edit().putBoolean("isRejectVideoPermission", isReject).apply()
            }
        }
    }

    /**
     * 请求权限,如果已经有权限,则不会弹出权限框
     */
    fun onAfterRequestPermission(supportFragmentManager: FragmentManager?, permissionString: String, deny: OnDeny, grant: OnGrant) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var innerFragment: InternalFragment? = null

            val findFragmentByTag =
                    supportFragmentManager?.findFragmentByTag(TAG_FRAGMENT)
            if (findFragmentByTag != null && findFragmentByTag is InternalFragment) {
                innerFragment = findFragmentByTag
            }
            if (innerFragment == null) {
                innerFragment = InternalFragment()
                supportFragmentManager?.beginTransaction()?.add(innerFragment!!, TAG_FRAGMENT)
                        ?.commitAllowingStateLoss()
                supportFragmentManager?.executePendingTransactions()
            }
            innerFragment?.onRequestPermission(grant, deny, permissionString)
        } else {
            grant()
        }
    }

}

class InternalFragment : Fragment() {

    private var isFirst: Boolean = true
    private var permissionString: String = Manifest.permission.READ_MEDIA_AUDIO
    private var onGrant: OnGrant? = null
    private var onDeny: OnGrant? = null//最终还是拒绝的回调


    /**
     * 获取到权限后自动取消当前Fragment
     */
    private val onGrantWrapper = {
        onGrant?.invoke()
        if (isAdded) {
            parentFragmentManager.beginTransaction().remove(this@InternalFragment).commit()
        }
    }

    /**
     * 请求存储权限
     */
    private var Manager: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Manager = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                when (permissionString) {
                    AndroidUtil.getStoragePermissionPermissionString() -> {
                        if (isFirst) {
                            TrackerMultiple.onEvent("Storage_Audio", "System1_Allow")
                        } else {
                            TrackerMultiple.onEvent("Storage_Audio", "System2_Allow")
                        }
                    }

                    AndroidUtil.getVideoPermissionPermissionStringAdapter33() -> {
                        if (isFirst) {
                            TrackerMultiple.onEvent("Storage_Videos", "System1_Allow")
                        } else {
                            TrackerMultiple.onEvent("Storage_Videos", "System2_Allow")
                        }
                    }
                }
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
        StoragePermissionManager.setRejectStoragePermission(true, permissionString)
        if (getShouldRequestPermission()) {
            when (permissionString) {
                AndroidUtil.getStoragePermissionPermissionString() -> {
                    TrackerMultiple.onEvent("Storage_Audio", "System1_Deny")
                    StoragePermissionDeniedDialog.show(childFragmentManager)
                }

                AndroidUtil.getVideoPermissionPermissionStringAdapter33() -> {
                    TrackerMultiple.onEvent("Storage_Videos", "System1_Deny")
                    StorageVideoPermissionDeniedDialog.show(childFragmentManager)
                }
            }
        } else {
            when (permissionString) {
                AndroidUtil.getStoragePermissionPermissionString() -> {
                    TrackerMultiple.onEvent("Storage_Audio", "System2_Deny")
                    StoragePermissionDeniedNoTipsDialog.show(childFragmentManager)
                }

                AndroidUtil.getVideoPermissionPermissionStringAdapter33() -> {
                    TrackerMultiple.onEvent("Storage_Videos", "System2_Deny")
                    onDeny?.invoke()
                }
            }
        }
    }


    /**
     * 判断是否应该显示弹窗
     */
    private fun getShouldRequestPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity?.shouldShowRequestPermissionRationale(permissionString)
                    ?: true
        } else {
            false
        }
    }

    /**
     * 请求权限
     */
    internal fun onRequestPermission(Grant: () -> Unit, deny: OnDeny, permissionString: String) {
        this.onGrant = Grant
        this.onDeny = deny
        onRequestPermissionReal(true, permissionString)
    }

    /**
     * 真正的请求权限入口
     * @param isFirst 是否是第一次请求权限
     */
    fun onRequestPermissionReal(isFirst: Boolean, permissionString: String) {
        if (StoragePermissionManager.hasStoragePermission(activity
                                                              ?: AppProvider.context, permissionString)
        ) {
            onGrant?.invoke()
        } else {
            when (permissionString) {
                AndroidUtil.getStoragePermissionPermissionString() -> {
                    if (isFirst) {
                        TrackerMultiple.onEvent("Storage_Audio", "System1_PV")
                    } else {
                        TrackerMultiple.onEvent("Storage_Audio", "System2_PV")
                    }
                }

                AndroidUtil.getVideoPermissionPermissionStringAdapter33() -> {
                    if (isFirst) {
                        TrackerMultiple.onEvent("Storage_Videos", "System1_PV")
                    } else {
                        TrackerMultiple.onEvent("Storage_Videos", "System2_PV")
                    }
                }
            }
            this.isFirst = isFirst
            this.permissionString = permissionString
            Manager?.launch(permissionString)
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
