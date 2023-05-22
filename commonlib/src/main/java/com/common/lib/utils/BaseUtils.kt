package com.common.lib.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.common.lib.constant.Constants.JPG_EXTENSION
import com.common.lib.utils.LanguageUtil.getLanguage
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class BaseUtils {
    companion object StaticParams {

        private const val IMAGE_EXTENSION = ".jpg"
        const val VIDEO_EXTENSION = ".mp4"

        var mContentValues: ContentValues? = null

        const val IMAGE_CONTENT_URI = "content://media/external/images/media"
        const val VIDEO_CONTENT_URI = "content://media/external/video/media"


        /**
         * 复制
         *
         * @param context context
         * @param content 内容
         */
        fun copyData(context: Context, content: String?) {
            if (TextUtils.isEmpty(content)) {
                return
            }
            //获取剪贴板管理器：
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText("Label", content)
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData)
        }

        fun createImagePath(context: Context): String {
            val fileName: String = UidUtil.createUid() + JPG_EXTENSION
            val dirPath = Environment.getExternalStorageDirectory()
                .toString() + "/Android/data/" + context.packageName + "/download"
            val file = File(dirPath)
            if (!file.exists() || !file.isDirectory) file.mkdirs()
            return "$dirPath/$fileName"
        }

        fun getSaveFilePath(
            context: Context?,
            fileName: String
        ): String {
            val dirPath = Environment.getExternalStorageDirectory()
                .toString() + "/Android/data/" + context!!.packageName + "/download"
            val file = File(dirPath)
            if (!file.exists() || !file.isDirectory) file.mkdirs()
            return "$dirPath/$fileName"
        }

        /**
         * dp转px
         *
         * @param context
         * @param dipValue
         * @return
         */
        fun dp2px(context: Context?, dipValue: Float): Int {
            val scale = context!!.resources.displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }

        /**
         * Return the status bar's height.
         *
         * @return the status bar's height
         */
        fun getStatusBarHeight(resources: Resources): Int {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return resources.getDimensionPixelSize(resourceId)
        }

        fun getTextByKey(context: Context?, key: String): String {
            var value = PrefUtil.getString(context, key + "_${getLanguage()}", "")
            if (!TextUtils.isEmpty(value)) {
                return value!!
            }
            try {
                val stringId = context!!.resources.getIdentifier(
                    key,
                    "string", context.packageName
                )
                // 取出配置的string文件中的默认值
                value = context.resources.getString(stringId)

            } catch (e: Exception) {
                value = ""
            }
            return value!!
        }

        /**
         * 获取diviceId,在测试升级时可能用到，上线时可以不再获取，可重写此方法返回一个固定的字符串，如：android，
         * 这样就可以不添加读取手机状态的权限
         * 需要增加 权限     <uses-permission android:name="android.permission.READ_PHONE_STATE"></uses-permission>
         *
         * @param context
         * @return
         */
        @SuppressLint("MissingPermission")
        fun getDeviceId(context: Context): String? {
            var deviceId = PrefUtil.getString(context, "deviceId", "")
            if (TextUtils.isEmpty(deviceId)) {
                if (!PermissionUtil.isGrantPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    )
                ) {
                    deviceId = Settings.System.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    PrefUtil.putString(context, "deviceId", deviceId)
                    return deviceId
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    val telephonyManager =
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    deviceId = telephonyManager.deviceId
                    if (deviceId == null) {
                        deviceId = Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        if (deviceId == null) {
                            deviceId = ""
                        }
                    }
                } else {
                    deviceId = Settings.System.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                }
                PrefUtil.putString(context, "deviceId", deviceId)
            }
            return deviceId
        }


        fun getMap(key: String, value: Any): HashMap<String, Any> {
            val map = HashMap<String, Any>()
            map.put(key, value)
            return map
        }

        fun getNewText(number: Int): String? {
            return if (number < 10) "0$number" else number.toString()
        }

        /**
         * 保存JPG图片
         *
         * @param bmp
         */
        fun saveJpeg(bmp: Bitmap, context: Context): File {
            val jpegFile = File(createImagePath(context))
            var fout: FileOutputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                fout = FileOutputStream(jpegFile)
                bos = BufferedOutputStream(fout)
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos)
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } finally {
                try {
                    fout?.close()
                    if (bos != null) {
                        bos.flush()
                        bos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return jpegFile
        }

        /**
         * 保存JPG图片
         *
         * @param bmp
         */
        fun saveJpeg(bmp: Bitmap, jpegFile: File): File {
            var fout: FileOutputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                fout = FileOutputStream(jpegFile)
                bos = BufferedOutputStream(fout)
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos)
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } finally {
                try {
                    fout?.close()
                    if (bos != null) {
                        bos.flush()
                        bos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return jpegFile
        }


        /**
         * @param bmp
         */
        fun saveJpegToAlbum(bmp: Bitmap, context: Context): String? {
            val folder = makeAlbumPath(0)
            var fout: FileOutputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                fout = FileOutputStream(folder)
                bos = BufferedOutputStream(fout)
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                registerPath(context, folder, 0)
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } finally {
                try {
                    fout?.close()
                    if (bos != null) {
                        bos.flush()
                        bos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return folder
        }

        private fun makeAlbumPath(type: Int): String {
            val title = UUID.randomUUID().toString()
            val fileName = UUID.randomUUID()
                .toString() + if (type == 0) IMAGE_EXTENSION else VIDEO_EXTENSION
            val albumPath = Environment.getExternalStorageDirectory()
                .toString() + "/" + Environment.DIRECTORY_DCIM + "/Camera"
            val file = File(albumPath)
            if (!file.exists() || !file.isDirectory) file.mkdirs()
            val path = "$albumPath/$fileName"
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, title)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(
                MediaStore.Images.Media.MIME_TYPE,
                if (type == 0) "image/jpeg" else "video/mp4"
            )
            values.put(MediaStore.Images.Media.DATA, path)
            mContentValues = values
            return path
        }

        /**
         * 将图片在系统内注册
         */
        private fun registerPath(context: Context, path: String, type: Int) {
            if (mContentValues != null) {
                var table: Uri? = null
                if (type == 0) {
                    table = Uri.parse(IMAGE_CONTENT_URI)
                    mContentValues!!.put(
                        MediaStore.Images.Media.SIZE,
                        File(path).length()
                    )
                } else {
                    table = Uri.parse(VIDEO_CONTENT_URI)
                    mContentValues!!.put(
                        MediaStore.Video.Media.SIZE,
                        File(path).length()
                    )
                }
                try {
                    context.contentResolver.insert(table, mContentValues)
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                }
                mContentValues = null
            }
        }

        /**
         * 显示圆形图片
         *
         * @param context   上下文
         * @param defaultId
         * @param url       图片路径
         * @param imageView 图片view
         */
        fun displayCircleImageView(
            context: Context,
            defaultId: Int,
            url: String?,
            imageView: ImageView?
        ) {
            Glide.with(context.applicationContext).load(url)
                .placeholder(defaultId)
                .error(defaultId)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(imageView!!)
        }

        /**
         * 显示圆角图片
         *
         * @param context   上下文
         * @param defaultId
         * @param url       图片路径
         * @param imageView 图片view
         */
        fun displayRoundImageView(
            context: Context,
            defaultId: Int,
            url: String?,
            imageView: ImageView?,
            radius: Int
        ) {
            Glide.with(context.applicationContext)
                .load(url)
                .placeholder(defaultId)
                .error(defaultId)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
                .into(imageView!!)
        }

        /**
         * @param defaultId
         * @param path
         * @param iv
         */
        fun loadImage(
            context: Context,
            defaultId: Int,
            path: String?,
            iv: ImageView?
        ) {
            Glide.with(context.applicationContext)
                .load(path)
                .apply(
                    RequestOptions()
                        .placeholder(defaultId)
                        .error(defaultId)
                        .centerCrop() //中心切圖, 會填滿
                        .fitCenter() //中心fit, 以原本圖片的長寬為主
                        .diskCacheStrategy(DiskCacheStrategy.DATA) //                      .dontAnimate()
                )
                .into(iv!!)
        }

        /**
         * @param defaultId
         * @param path
         * @param iv
         */
        fun loadImage(
            context: Context,
            defaultId: Int,
            file: File,
            iv: ImageView?
        ) {
            Glide.with(context.applicationContext)
                .load(Uri.fromFile(file))
                .apply(
                    RequestOptions()
                        .placeholder(defaultId)
                        .error(defaultId)
                        .centerCrop() //中心切圖, 會填滿
                        .fitCenter() //中心fit, 以原本圖片的長寬為主
                        .diskCacheStrategy(DiskCacheStrategy.DATA) //                      .dontAnimate()
                )
                .into(iv!!)
        }

        /**
         * @param defaultId
         * @param path
         * @param iv
         */
        fun loadImage(
            context: Context,
            defaultId: Int,
            file: File,
            path: String?,
            iv: ImageView?
        ) {
            if (file.exists()) {
                loadImage(context, defaultId, file, iv)
            } else {
                loadImage(context, defaultId, path, iv)
            }
        }

        /**
         * @param defaultId
         * @param path
         * @param iv
         */
        fun loadImage(
            context: Context,
            defaultId: Int,
            path: Uri?,
            iv: ImageView?
        ) {
            Glide.with(context.applicationContext)
                .load(path)
                .apply(
                    RequestOptions()
                        .placeholder(defaultId)
                        .error(defaultId)
                        .centerCrop() //中心切圖, 會填滿
                        .fitCenter() //中心fit, 以原本圖片的長寬為主
                        .diskCacheStrategy(DiskCacheStrategy.DATA) //                      .dontAnimate()
                )
                .into(iv!!)
        }

        /**
         * 显示圆角图片
         *
         * @param context   上下文
         * @param defaultId
         * @param url       图片路径
         * @param imageView 图片view
         */
        fun displayLocalRoundImageView(
            context: Context,
            defaultId: Int,
            uri: Uri,
            imageView: ImageView,
            radius: Int
        ) {
            Glide.with(context.applicationContext)
                .load(uri)
                .placeholder(defaultId)
                .error(defaultId)
                .apply(
                    RequestOptions()
                        .transforms(
                            CenterCrop(), RoundedCorners(radius)
                        )
                )
                .into(imageView)
        }

        /**
         * 显示圆角图片
         *
         * @param context   上下文
         * @param defaultId
         * @param url       图片路径
         * @param imageView 图片view
         */
        fun displayNetRoundImageView(
            context: Context,
            defaultId: Int,
            url: String,
            imageView: ImageView,
            radius: Int
        ) {
            Glide.with(context.applicationContext)
                .load(url)
                .placeholder(defaultId)
                .error(defaultId)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(imageView)
        }

        /**
         * 显示圆角图片
         *
         * @param context   上下文
         * @param defaultId
         * @param file
         * @param url       图片路径
         * @param imageView 图片view
         */
        fun displayLocalOrNetRoundImageView(
            context: Context,
            defaultId: Int,
            file: File,
            url: String,
            imageView: ImageView,
            radius: Int
        ) {
            if (file.exists()) {
                displayLocalRoundImageView(
                    context,
                    defaultId,
                    Uri.fromFile(file),
                    imageView,
                    radius
                )
            } else {
                displayNetRoundImageView(context, defaultId, url, imageView, radius)
            }
        }

        fun longToDate(time: Long): String? {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val date = Date(time)
            return sdf.format(date)
        }

        fun longToDate2(time: Long): String? {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val date = Date(time)
            return sdf.format(date)
        }

        fun longToDate3(time: Long): String? {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = Date(time)
            return sdf.format(date)
        }

        fun dateStrToLong2(DateTime: String?): Long {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val time = sdf.parse(DateTime)
                return time.time
            } catch (e: java.lang.Exception) {
            }
            return 0
        }

        fun transferTime(startime: String): String? {
            val calendar = Calendar.getInstance()
            val dataformat =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currenttime = calendar.timeInMillis //获取系统当先时间的毫秒值
            var formattingtime = currenttime
            try {
                formattingtime = dataformat.parse(startime).time
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            val TimeCha =
                (currenttime - formattingtime) / 1000 - 8 * 60 * 60 //将毫秒值转化成分钟数
            val str = StringBuffer()
            if (TimeCha > 0 && TimeCha < 60) {
                return str.append("刚刚").toString()
            } else if (TimeCha >= 60 && TimeCha < 3600) {
                return str.append((TimeCha / 60).toString() + "分钟前").toString()
            } else if (TimeCha >= 3600 && TimeCha < 3600 * 24) {
                return str.append((TimeCha / 3600).toString() + "小时前").toString()
            } else if (TimeCha >= 3600 * 1 * 24 && TimeCha < 3600 * 2 * 24) {
                return str.append("1天前").toString()
            } else if (TimeCha >= 3600 * 2 * 24 && TimeCha < 3600 * 3 * 24) {
                return str.append("2天前").toString()
            } else if (TimeCha >= 3600 * 3 * 24 && TimeCha < 3600 * 4 * 24) {
                return str.append("3天前").toString()
            }
            return startime.substring(5, 16)
        }

        fun getAllApps(
            context: Context,
            app_flag_1: String?,
            app_flag_2: String?
        ): PackageInfo? {
            val pManager = context.packageManager
            // 获取手机内所有应用
            val packlist = pManager.getInstalledPackages(0)
            for (i in 0 until packlist.size) {
                val pak =
                    packlist[i]
                if (pak.packageName.contains(app_flag_1!!) || pak.packageName.contains(app_flag_2!!)) {
                    return pak
                }
            }
            return null
        }

        private fun changeAppLanguage(resources: Resources, language: Locale) {
            val configuration = resources.configuration
            val displayMetrics = resources.displayMetrics
            configuration.setLocale(language)
            resources.updateConfiguration(configuration, displayMetrics)
        }

        fun changeAppLanguage(context: Context, language: Int) {
            var locale = Locale.SIMPLIFIED_CHINESE
            when (language) {
                0 -> locale = Locale.ENGLISH
                1 -> locale = Locale.SIMPLIFIED_CHINESE
            }
            changeAppLanguage(context.resources, locale)
        }
    }

}

