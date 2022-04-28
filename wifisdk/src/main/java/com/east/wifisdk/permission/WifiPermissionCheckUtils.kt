package com.east.wifisdk.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.east.permission.rxpermissions.RxPermissions

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:   Wifi的权限检测工具
 *  @author: East
 *  @date: 2019-12-04
 * |---------------------------------------------------------------------------------------------------------------|
 */
object WifiPermissionCheckUtils {
    fun checkPermission(activity: FragmentActivity,listener : PermissionListener) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val rxPermissions = RxPermissions(activity!!)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(
                        activity,
                        listener
                    )
                }
                else -> {
                    showMissingPermissionDialog(
                        activity,
                        listener
                    )
                }
            }
        }
    }


    fun checkPermission(fragment: Fragment, listener : PermissionListener) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val rxPermissions = RxPermissions(fragment)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(
                        fragment,
                        listener
                    )
                }
                else -> {
                    showMissingPermissionDialog(
                        fragment.requireContext(),
                        listener
                    )
                }
            }
        }
    }


    fun showMissingPermissionDialog(context: Context, listener: PermissionListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("提示")
        builder.setMessage("当前应用缺少必要权限。请点击\"设置\"-\"权限\"-打开所需权限。")

        // 拒绝, 退出应用
        builder.setNegativeButton(
            "取消"
        ) { _, _ -> listener.onCancel() }

        builder.setPositiveButton(
            "设置"
        ) { _, _ ->
            startAppSettings(
                context
            )
        }

        builder.setCancelable(false)

        builder.show()
    }

    /**
     * 启动应用的设置
     */
    fun startAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
//        finish()
    }
}