package com.san.audioeditor.delete.dialog

/**
 * 存储框架请求权限
 */
interface ISAFPermissionRequest {

    /**
     * 请求权限
     */
    fun onAccessPermissionsRequest()

    /**
     * 弹窗关闭
     */
    fun onDismissCancel()
}