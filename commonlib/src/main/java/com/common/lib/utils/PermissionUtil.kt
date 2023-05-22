package com.common.lib.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtil {

    /**
     * @param context
     * @param permission
     * @return
     */
    fun isGrantPermission(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun getUncheckPermissions(context: Context, vararg permissions: String): ArrayList<String> {
        val result = ArrayList<String>()
        for (permission in permissions) {
            if (!isGrantPermission(context, permission)) {
                //进行权限请求
                result.add(permission)
            }
        }
        return result
    }

}