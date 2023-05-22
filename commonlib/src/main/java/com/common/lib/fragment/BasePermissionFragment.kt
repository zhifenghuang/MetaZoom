package com.common.lib.fragment

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.common.lib.interfaces.PermissionCallBack
import com.common.lib.utils.PermissionUtil
import java.util.*

abstract class BasePermissionFragment : Fragment() {

    private var requestPermissionRequestCode = -1
    private var mPermissionCallBack: PermissionCallBack? = null

    fun requestPermission(
        callback: PermissionCallBack?,
        vararg permissions: String
    ) {
        val uncheckPermissions = PermissionUtil.getUncheckPermissions(context!!, *permissions)
        requestPermissionRequestCode = Random().nextInt(10000)
        if (uncheckPermissions.isNotEmpty()) {
            requestPermissions(
                uncheckPermissions.toTypedArray(),
                requestPermissionRequestCode
            )
            mPermissionCallBack = callback
        } else {
            mPermissionCallBack?.onSuccess()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestPermissionRequestCode != requestCode) {
            return
        }

        var isAllGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false
                break
            }
        }

        if (isAllGranted) {
            mPermissionCallBack?.onSuccess()
        } else {
            mPermissionCallBack?.onFailure()
        }
        mPermissionCallBack = null
    }

    open fun requestExternalStoragePermission() {
        val uncheckPermission = PermissionUtil.getUncheckPermissions(
            context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (uncheckPermission.isNotEmpty()) {
            requestPermission(null, *uncheckPermission.toTypedArray())
        }
    }

    /**
     * @return has permission ?
     * **/
    fun requestCameraPermission(): Boolean {
        val permission = Manifest.permission.CAMERA
        return if (!PermissionUtil.isGrantPermission(context!!, permission)) {
            requestPermission(object : PermissionCallBack {
                override fun onSuccess() {
                    onGetCameraPermission()
                }

                override fun onFailure() {
                }

            }, permission)
            false
        } else {
            true
        }
    }

    open fun onGetCameraPermission() {

    }

    open fun requestGalleryPermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        return if (!PermissionUtil.isGrantPermission(context!!, permission)) {
            requestPermission(object : PermissionCallBack {
                override fun onSuccess() {
                    onGetGalleryPermission()
                }

                override fun onFailure() {
                }

            }, permission)
            false
        } else {
            true
        }
    }

    open fun onGetGalleryPermission() {


    }

    open fun requestPhonePermission(): Boolean {
        val permission = Manifest.permission.READ_PHONE_STATE
        return if (!PermissionUtil.isGrantPermission(context!!, permission)) {
            requestPermission(object : PermissionCallBack {
                override fun onSuccess() {
                    onGetPhonePermission()
                }

                override fun onFailure() {
                }

            }, permission)
            false
        } else {
            true
        }
    }

    open fun onGetPhonePermission() {


    }
}