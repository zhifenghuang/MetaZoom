package com.common.lib.utils

import android.R
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import android.view.View.MeasureSpec
import android.view.WindowManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object BitmapUtil {
    fun easyFit(bkg: Bitmap, top: Int, left: Int, bottom: Int, right: Int): Bitmap? {
        //long startMs = System.currentTimeMillis();
        val Width = right - left
        val Height = bottom - top
        val scaleFactor = 4f //图片缩放比例；
        val overlay = Bitmap.createBitmap(
            (Width / scaleFactor).toInt(),
            (Height / scaleFactor).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(overlay)
        canvas.translate(-left / scaleFactor, -top / scaleFactor)
        canvas.scale(1 / scaleFactor, 1 / scaleFactor)
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        if (Width >= Height) {
            val srcRectWidth = bkg.width
            val srcRectHeight = bkg.width * Height / Width
            val srcRect = Rect(
                0,
                (bkg.height - srcRectHeight) / 2,
                srcRectWidth,
                (bkg.height - srcRectHeight) / 2 + srcRectHeight
            )
            val dstRect = Rect(0, 0, Width, Height)
            canvas.drawBitmap(bkg, srcRect, dstRect, paint)
        } else if (Width < Height) {
            val srcRectWidth = bkg.height * Width / Height
            val srcRectHeight = bkg.height
            val srcRect = Rect(
                (bkg.width - srcRectWidth) / 2,
                0,
                (bkg.width - srcRectWidth) / 2 + srcRectWidth,
                srcRectHeight
            )
            val dstRect = Rect(0, 0, Width, Height)
            canvas.drawBitmap(bkg, srcRect, dstRect, paint)
        }
        return overlay
    }

    @Throws(FileNotFoundException::class)
    fun decodeFitBitmap(context: Context, file: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val manager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(displayMetrics)
        val x = displayMetrics.widthPixels
        val y = displayMetrics.heightPixels
        return if (file.startsWith("content://media")) {
            val inputStream =
                context.contentResolver.openInputStream(Uri.parse(file))
            BitmapFactory.decodeStream(inputStream, null, options)
            options.inSampleSize = calculateInSampleSize(options, x, y)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeStream(inputStream, null, options)
        } else {
            BitmapFactory.decodeFile(file, options)
            options.inSampleSize = calculateInSampleSize(options, x, y)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(file, options)
        }
    }

    fun easyBlur(
        bkg: Bitmap,
        top: Int,
        left: Int,
        bottom: Int,
        right: Int,
        radius: Float
    ): Bitmap? {
        var overlay = easyFit(bkg, top, left, bottom, right)
        overlay = doBlur(overlay, radius.toInt())
        return overlay
//	    view.setBackgroundDrawable(new BitmapDrawable(context.getResources(), overlay));
    }

    /**
     * 高斯模糊处理算法
     *
     * @param sentBitmap
     * @param radius
     * @return
     */
    private fun doBlur(sentBitmap: Bitmap?, radius: Int): Bitmap? {
        val bitmap = sentBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        if (radius < 1) {
            return null
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (-0x1000000 and pix[yi] or (dv[rsum] shl 16)
                        or (dv[gsum] shl 8) or dv[bsum])
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }

    /**
     * 将图片变圆角
     *
     * @param bitmap
     * @param pixels
     * @return
     */
    fun toRoundCorner(bitmap: Bitmap, pixels: Float): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect =
            Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, pixels, pixels, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun toRoundCornerInShader(bitmap: Bitmap, pixels: Float): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val bitmapShader =
            BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val paint = Paint()
        paint.shader = bitmapShader
        paint.flags = Paint.ANTI_ALIAS_FLAG
        val canvas = Canvas(output)
        val roundRect = RectF(
            0f, 0f,
            bitmap.width.toFloat(),
            bitmap.height.toFloat()
        )
        canvas.drawRoundRect(
            roundRect, pixels, pixels,
            paint
        )
        return output
    }


    /**
     * 往图库插入图片
     */
    fun insertImage(
        cr: ContentResolver,
        source: Bitmap?,
        title: String?,
        description: String?
    ): String? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
        values.put(MediaStore.Images.Media.DESCRIPTION, description)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        var url: Uri? = null
        var stringUrl: String? = null /* value to be returned */
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (source != null) {
                val imageOut = cr.openOutputStream(url!!)
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
                } finally {
                    imageOut!!.close()
                }
                val id = ContentUris.parseId(url)
                // Wait until MINI_KIND thumbnail is generated.
                val miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
                    cr,
                    id,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null
                )
                // This is for backward compatibility.
                storeThumbnail(
                    cr,
                    miniThumb,
                    id,
                    50f,
                    50f,
                    MediaStore.Images.Thumbnails.MICRO_KIND
                )
            } else {
                cr.delete(url!!, null, null)
                url = null
            }
        } catch (e: Exception) {
            if (url != null) {
                cr.delete(url, null, null)
                url = null
            }
        }
        if (url != null) {
            stringUrl = url.toString()
        }
        return stringUrl
    }

    /**
     * 存储缩略图到图库
     */
    private fun storeThumbnail(
        cr: ContentResolver,
        source: Bitmap,
        id: Long,
        width: Float,
        height: Float,
        kind: Int
    ): Bitmap? {

        // create the matrix to scale it
        val matrix = Matrix()
        val scaleX = width / source.width
        val scaleY = height / source.height
        matrix.setScale(scaleX, scaleY)
        val thumb = Bitmap.createBitmap(
            source, 0, 0,
            source.width,
            source.height, matrix,
            true
        )
        val values = ContentValues(4)
        values.put(MediaStore.Images.Thumbnails.KIND, kind)
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)
        val url =
            cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)
        return try {
            val thumbOut = cr.openOutputStream(url!!)
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut)
            thumbOut!!.close()
            thumb
        } catch (ex: FileNotFoundException) {
            null
        } catch (ex: IOException) {
            null
        }
    }

    /**
     * 控件截图
     *
     * @author Damon
     */
    fun getViewDrawingCache(
        context: Context,
        view: View,
        cacheBitmapKey: Int,
        cacheDirtyKey: Int
    ): Bitmap? {
        var bitmap: Bitmap? = view.getTag(cacheBitmapKey) as Bitmap
        var dirty = view.getTag(cacheDirtyKey) as Boolean
        if (view.width + view.height == 0) {
            view.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }
        val viewWidth = view.width
        val viewHeight = view.height
        if (bitmap == null || bitmap.width != viewWidth || bitmap.height != viewHeight) {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
            view.setTag(cacheBitmapKey, bitmap)
            dirty = true
        }
        if (dirty == true) {
            bitmap!!.eraseColor(context.resources.getColor(R.color.white))
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            view.setTag(cacheDirtyKey, false)
        }
        return bitmap
    }


    /**
     * 根据给定的宽高取图片
     *
     * @param imageFile
     * @param width
     * @param height
     * @return
     */
    fun getBitmapFromFile(imageFile: String, width: Int, height: Int): Bitmap? {
        if (width == 0 || height == 0) {
            return null
        }
        val opts = BitmapFactory.Options()
        val bound = getBitmapBound(imageFile)
        val degree = getBitmapDegree(imageFile)
        val wmRatio: Int
        val hmRatio: Int
        if (degree % 180 == 0) {
            wmRatio = bound[0] / width
            hmRatio = bound[1] / height
        } else {
            wmRatio = bound[1] / width
            hmRatio = bound[0] / height
        }
        opts.inSampleSize = 1
        if (wmRatio > 1 || hmRatio > 1) {
            if (wmRatio > hmRatio) {
                opts.inSampleSize = wmRatio
            } else {
                opts.inSampleSize = hmRatio
            }
        }
        val bmp = BitmapFactory.decodeFile(imageFile, opts)
        if (degree % 360 != 0 && bmp != null) {
            val matrix = Matrix()
            matrix.setRotate(
                degree.toFloat(),
                bmp.width / 2.toFloat(),
                bmp.height / 2.toFloat()
            )
            val newBmp =
                Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            bmp.recycle()
            return newBmp
        }
        return bmp
    }

    /**
     * 根据给定的宽高取图片
     *
     * @param imageFile
     * @return
     */
    fun getBitmapFromFile(
        context: Context,
        imageFile: String
    ): Bitmap? {
        val opts = BitmapFactory.Options()
        val degree = getBitmapDegree(imageFile)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile, options)
        val manager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(displayMetrics)
        val x = displayMetrics.widthPixels
        val y = displayMetrics.heightPixels
        if (degree % 180 != 0) {
            val temp = options.outWidth
            options.outWidth = options.outHeight
            options.outHeight = temp
        }
        options.inSampleSize = calculateInSampleSize(options, x, y)
        options.inJustDecodeBounds = false
        val bmp = BitmapFactory.decodeFile(imageFile, opts)
        if (degree % 360 != 0 && bmp != null) {
            val matrix = Matrix()
            matrix.setRotate(
                degree.toFloat(),
                bmp.width / 2.toFloat(),
                bmp.height / 2.toFloat()
            )
            val newBmp =
                Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            bmp.recycle()
            return newBmp
        }
        return bmp
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        while (height / inSampleSize > reqHeight
            && width / inSampleSize > reqWidth
        ) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * 根据给定的宽高取图片
     *
     * @param imageFile
     * @param width
     * @param height
     * @return
     */
    fun getBitmapFromFile(imageFile: File, width: Int, height: Int): Bitmap? {
        if (width == 0 || height == 0) {
            return null
        }
        val opts = BitmapFactory.Options()
        val bound = getBitmapBound(imageFile.absolutePath)
        if (width == 0 || height == 0) {
            return null
        }
        val degree = getBitmapDegree(imageFile.absolutePath)
        val wmRatio: Int
        val hmRatio: Int
        if (degree % 180 == 0) {
            wmRatio = bound[0] / width
            hmRatio = bound[1] / height
        } else {
            wmRatio = bound[1] / width
            hmRatio = bound[0] / height
        }
        if (wmRatio > 1 || hmRatio > 1) {
            if (wmRatio > hmRatio) {
                opts.inSampleSize = wmRatio
            } else {
                opts.inSampleSize = hmRatio
            }
        }
        val bmp = BitmapFactory.decodeFile(imageFile.absolutePath, opts)
        if (degree % 360 != 0 && bmp != null) {
            val matrix = Matrix()
            matrix.setRotate(
                degree.toFloat(),
                bmp.width / 2.toFloat(),
                bmp.height / 2.toFloat()
            )
            val newBmp =
                Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            bmp.recycle()
            return newBmp
        }
        return bmp
    }

    fun getBitmapBound(context: Context, uri: Uri): IntArray {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream, Rect(), options)
        val bound = IntArray(2)
        bound[0] = options.outWidth
        bound[1] = options.outHeight
        return bound
    }

    fun getBitmapBound(imageFile: String?): IntArray {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile, opts)
        val bound = IntArray(2)
        bound[0] = opts.outWidth
        bound[1] = opts.outHeight
        return bound
    }

    fun getBitmapBound(ctx: Context, resID: Int): IntArray? {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeResource(ctx.resources, resID, opts)
        val bound = IntArray(2)
        bound[0] = opts.outWidth
        bound[1] = opts.outHeight
        return bound
    }

    fun bitmap2Bytes(bm: Bitmap): ByteArray? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    fun getVideoThumbnail(filePath: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            bitmap = retriever.frameAtTime
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }


    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    fun getBitmapDegree(path: String): Int {
        var degree = 0
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(path)
            // 获取图片的旋转信息
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    fun bitmapToBase64(bitmap: Bitmap?): String? {
        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.flush()
                baos.close()
                val bitmapBytes = baos.toByteArray()
                result =
                    Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.flush()
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun bmpToByteArray(bmp: Bitmap, needRecycle: Boolean): ByteArray? {
        val output = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
        if (needRecycle) {
            bmp.recycle()
        }
        val result = output.toByteArray()
        try {
            output.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun base64ToBitmap(string: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bitmapArray = Base64.decode(string, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}