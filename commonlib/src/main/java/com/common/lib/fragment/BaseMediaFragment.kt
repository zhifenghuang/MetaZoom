package com.common.lib.fragment

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.common.lib.constant.Constants.APP_NAME
import com.common.lib.constant.Constants.JPG_EXTENSION

import com.common.lib.utils.MediaStoreUtil
import com.common.lib.utils.UidUtil
import java.io.File
import java.util.*

abstract class BaseMediaFragment : BasePermissionFragment() {

    private var openCameraRequestCode = -1
    private var insertPhotoUri: Uri? = null
    private var openGalleryRequestCode = -1

    private fun createPhotoUri(): Uri? {
        val fileName = UidUtil.createUid() + JPG_EXTENSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 新方法
            val values = ContentValues()
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                fileName
            )
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${APP_NAME}")

            return MediaStoreUtil.insert(
                context!!,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

        } else {
            // 舊方法
            val folder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                APP_NAME
            )
            val file = File(folder, fileName)

            return Uri.fromFile(file)
        }
    }

    open fun openCamera() {
        if (requestCameraPermission()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            insertPhotoUri = createPhotoUri()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, insertPhotoUri)
            openCameraRequestCode = Random().nextInt(10000)
            startActivityForResult(intent, openCameraRequestCode)
        }
    }

    override fun onGetCameraPermission() {
        super.onGetCameraPermission()
        openCamera()
    }

    fun openGallery() {
        if (requestGalleryPermission()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            openGalleryRequestCode = Random().nextInt(10000)
            startActivityForResult(intent, openGalleryRequestCode)
        }
    }

    override fun onGetGalleryPermission() {
        super.onGetGalleryPermission()
        openGallery()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            openCameraRequestCode -> {
                data?.data?.let { uri ->
                    onGetImageWithUri(uri)
                } ?: insertPhotoUri?.let { uri ->
                    onGetImageWithUri(uri)
                }
            }
            openGalleryRequestCode -> {
                data?.data?.let { uri ->
                    onGetImageWithUri(uri)
                }
            }
        }
    }

    open fun onGetImageWithUri(uri: Uri) {
    }
}