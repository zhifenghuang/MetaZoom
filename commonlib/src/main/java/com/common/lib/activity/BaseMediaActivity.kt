package com.common.lib.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.common.lib.constant.Constants.APP_NAME
import com.common.lib.constant.Constants.JPG_EXTENSION
import com.common.lib.utils.BaseUtils
import com.common.lib.utils.MediaStoreUtil
import com.common.lib.utils.UidUtil
import java.io.File
import java.util.*

abstract class BaseMediaActivity : BasePermissionActivity() {

    private var openCameraRequestCode = -1
    private var openCameraVideoRequestCode = -1
    private var insertMediaUri: Uri? = null
    private var openGalleryRequestCode = -1
    private var openGalleryVideoRequestCode = -1


    private fun createPhotoUri(): Uri? {
        val fileName = UidUtil.createUid() + JPG_EXTENSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 新方法
            val values = ContentValues()
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                fileName
            )
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$APP_NAME")

            return MediaStoreUtil.insert(
                this,
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
            insertMediaUri = createPhotoUri()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, insertMediaUri)
            openCameraRequestCode = Random().nextInt(10000)
            startActivityForResult(intent, openCameraRequestCode)
        }
    }

    open fun openCameraForVideo() {
        if (requestCameraVideoPermission()) {
            openCameraVideoRequestCode = Random().nextInt(10000)
            val openCameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            openCameraIntent.putExtra(
                MediaStore.EXTRA_VIDEO_QUALITY,
                0.99
            ) // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大

            openCameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60) // 设置视频录制的最长时间

            val outFile =
                File(BaseUtils.getSaveFilePath(this@BaseMediaActivity, "output.mp4"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                openCameraIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                insertMediaUri = FileProvider.getUriForFile(
                    this@BaseMediaActivity,
                    packageName + ".fileprovider",
                    outFile
                )
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, insertMediaUri)
            } else {
                insertMediaUri = Uri.fromFile(outFile)
                openCameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    insertMediaUri
                )
            }
            startActivityForResult(openCameraIntent, openCameraVideoRequestCode)
        }
    }

    override fun onGetCameraPermission() {
        super.onGetCameraPermission()
        openCamera()
    }

    override fun onGetCameraVideoPermission() {
        super.onGetCameraVideoPermission()
        openCameraForVideo()
    }

    fun openGallery() {
        if (requestGalleryPermission(0)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            openGalleryRequestCode = Random().nextInt(10000)
            startActivityForResult(intent, openGalleryRequestCode)
        }
    }

    fun openGalleryForVideo() {
        if (requestGalleryPermission(1)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            intent.putExtra(
                MediaStore.EXTRA_VIDEO_QUALITY,
                0.9999
            ) // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 180) // 设置视频录制的最长时间
            openGalleryVideoRequestCode = Random().nextInt(10000)
            startActivityForResult(intent, openGalleryVideoRequestCode)
        }
    }

    override fun onGetGalleryPermission() {
        super.onGetGalleryPermission()
        openGallery()
    }

    override fun onGetGalleryVideoPermission() {
        super.onGetGalleryVideoPermission()
        openGalleryForVideo()
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
                } ?: insertMediaUri?.let { uri ->
                    onGetImageWithUri(uri)
                }
            }
            openCameraVideoRequestCode -> {
                data?.data?.let { uri ->
                    onGetVideoWithUri(uri)
                } ?: insertMediaUri?.let { uri ->
                    onGetVideoWithUri(uri)
                }
            }
            openGalleryRequestCode -> {
                data?.data?.let { uri ->
                    onGetImageWithUri(uri)
                }
            }
            openGalleryVideoRequestCode -> {
                data?.data?.let { uri ->
                    onGetVideoWithUri(uri)
                }
            }
        }
    }

    open fun onGetImageWithUri(uri: Uri) {
    }

    open fun onGetVideoWithUri(uri: Uri) {
    }
}