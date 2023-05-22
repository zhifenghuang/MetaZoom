package com.common.lib.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.common.lib.R
import com.common.lib.dialog.MyDialogFragment
import com.common.lib.interfaces.OnClickCallback
import com.common.lib.interfaces.PermissionCallBack
import com.common.lib.utils.PermissionUtil
import com.common.lib.utils.PrefUtil
import java.util.*

abstract class BasePermissionActivity : AppCompatActivity() {

    private var requestPermissionRequestCode = -1
    private var mPermissionCallBack: PermissionCallBack? = null

    fun requestPermission(
        callback: PermissionCallBack?,
        vararg permissions: String
    ) {
        val uncheckPermissions = PermissionUtil.getUncheckPermissions(this, *permissions)
        requestPermissionRequestCode = Random().nextInt(10000)
        if (uncheckPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
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
//
//            if (permissions != null && !permissions.isEmpty()
//                && !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions.get(0))) {
//            }
        }
        mPermissionCallBack = null
    }

    open fun requestExternalStoragePermission() {
        val uncheckPermission = PermissionUtil.getUncheckPermissions(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
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
        return if (!PermissionUtil.isGrantPermission(this, permission)) {
            showRequestPermissionDialog(1, "相机", object : OnClickCallback {
                override fun onClick(viewId: Int) {
                    requestPermission(object : PermissionCallBack {
                        override fun onSuccess() {
                            onGetCameraPermission()
                        }

                        override fun onFailure() {
                        }

                    }, permission)
                }
            })
            false
        } else {
            true
        }
    }

    /**
     * @return has permission ?
     * **/
    fun requestCameraVideoPermission(): Boolean {
        val permission = Manifest.permission.CAMERA
        return if (!PermissionUtil.isGrantPermission(this, permission)) {
            showRequestPermissionDialog(1, "相机", object : OnClickCallback {
                override fun onClick(viewId: Int) {
                    requestPermission(object : PermissionCallBack {
                        override fun onSuccess() {
                            onGetCameraVideoPermission()
                        }

                        override fun onFailure() {
                        }

                    }, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                }
            })

            false
        } else {
            true
        }
    }

    open fun onGetCameraPermission() {

    }

    open fun onGetCameraVideoPermission() {

    }

    open fun requestGalleryPermission(type: Int): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        return if (!PermissionUtil.isGrantPermission(this, permission)) {

            showRequestPermissionDialog(1, "存储", object : OnClickCallback {
                override fun onClick(viewId: Int) {
                    requestPermission(object : PermissionCallBack {
                        override fun onSuccess() {
                            if (type == 0) {
                                onGetGalleryPermission()
                            } else {
                                onGetGalleryVideoPermission()
                            }
                        }

                        override fun onFailure() {
                        }

                    }, permission)
                }
            })
            false
        } else {
            true
        }
    }

    open fun onGetGalleryPermission() {

    }

    open fun onGetGalleryVideoPermission() {

    }

    open fun requestPhonePermission(): Boolean {
        val permission = Manifest.permission.READ_PHONE_STATE
        return if (!PermissionUtil.isGrantPermission(this, permission)) {
            showRequestPermissionDialog(1, "手机状态", object : OnClickCallback {
                override fun onClick(viewId: Int) {
                    requestPermission(object : PermissionCallBack {
                        override fun onSuccess() {
                            onGetPhonePermission()
                        }

                        override fun onFailure() {
                        }

                    }, permission)
                }
            })
            false
        } else {
            true
        }
    }

    open fun onGetPhonePermission() {

    }

    open fun showRequestPermissionDialog(type: Int
                                         , permission: String
                                         , callBack: OnClickCallback? = null) {
        var now = Date().time
        var lastTime = PrefUtil.getLong(this, "LAST_PERMISSION_KEY_" + type, -1)
        var notice = false
        if ((now - lastTime) < 86400000) {
            notice = true
        }

        PrefUtil.putLong(this, "LAST_PERMISSION_KEY_" + type, now)

        val dialogFragment = MyDialogFragment(R.layout.layout_permission_dialog)
        if (notice) {
            dialogFragment.setOnMyDialogListener(object : MyDialogFragment.OnMyDialogListener {
                override fun initView(view: View?) {
                    (view!!.findViewById<View>(R.id.btn1) as TextView).visibility = View.VISIBLE
                    if (0 == type) {
                        (view.findViewById<View>(R.id.tv2) as TextView).text = "为了降低流量消耗,缓存图片,需要您去应用中心为应用授权以下权限"
                    } else if (1 == type) {
                        (view.findViewById<View>(R.id.tv2) as TextView).text = "为了正常拍摄照片和视频,需要您去应用中心为应用授权以下权限"
                    } else if (2 == type) {
                        (view.findViewById<View>(R.id.tv2) as TextView).text = "为了更好推荐课程和内容,需要您去应用中心为应用授权以下权限"
                    }

                    (view.findViewById<View>(R.id.tv3) as TextView).text = permission
                    (view.findViewById<View>(R.id.btn2) as TextView).text = "去设置"
                    dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2)
                }

                override fun onViewClick(viewId: Int) {
                    if (viewId == R.id.btn2) {
                        var intent = Intent()
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        var uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent)
                    } else if (viewId == R.id.btn1) {
                        //dialogFragment.dismiss();
                    }
                }
            })
        } else {
            dialogFragment.setOnMyDialogListener(object : MyDialogFragment.OnMyDialogListener {
                override fun initView(view: View?) {
                    if (0 == type) {
                        (view!!.findViewById<View>(R.id.tv2) as TextView).text = "为了降低流量消耗,缓存图片,需要您授权以下权限"
                    } else if (1 == type) {
                        (view!!.findViewById<View>(R.id.tv2) as TextView).text = "为了正常拍摄照片和视频,需要您授权以下权限"
                    } else if (2 == type) {
                        (view!!.findViewById<View>(R.id.tv2) as TextView).text = "为了更好推荐课程和内容,需要您授权以下权限"
                    }
                    (view!!.findViewById<View>(R.id.tv3) as TextView).text = permission
                    dialogFragment.setDialogViewsOnClickListener(view, R.id.btn2)
                }

                override fun onViewClick(viewId: Int) {
                    if (viewId == R.id.btn2) {
                        callBack?.onClick(viewId)
                    }
                }
            })
        }

        dialogFragment.show(supportFragmentManager, "MyDialogFragment")
    }



}