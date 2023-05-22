package com.common.lib.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object MediaStoreUtil {
    const val TAG = "MediaStoreUtil"
    fun saveStringFile(
        context: Context,
        externalDirUri: Uri,
        contentValues: ContentValues,
        string: String
    ) {
        val displayName: String =
            contentValues.getAsString(MediaStore.Images.Media.DISPLAY_NAME)

        // 如果檔案已存在，則刪除它
        val exists: Boolean = exists(context, externalDirUri, displayName)
        if (exists) {
            delete(context, externalDirUri, displayName)
        }

        // 透過insert，系統會生成一個id，
        // 並返回一個由dir uri和id組成的新uri，
        // 作為要插入的檔案的uri。
        val insertUri = insert(context, externalDirUri, contentValues)
        Log.i(
            TAG,
            "insertUri: $insertUri"
        )

        if (insertUri == null) {
            Log.i(TAG, "insertUri is null")
            return
        }

        // 寫入檔案
        var os: OutputStream? = null
        var `is`: InputStream? = null
        try {
            val resolver: ContentResolver = context.contentResolver
            os = resolver.openOutputStream(insertUri)
            if (os != null) {
                `is` = string.byteInputStream()
                val buffer = ByteArray(1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            } else {
                Log.e(TAG, "openOutputStream fail")

            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "拷貝失敗")
        } finally {
            closeCloseable(os)
            closeCloseable(`is`)
        }
    }

    fun insert(
        context: Context,
        insertUri: Uri,
        contentValues: ContentValues
    ): Uri? {
        val resolver: ContentResolver = context.contentResolver

        // 透過insert，系統會生成一個id，
        // 並返回一個由dir uri和id組成的新uri，
        // 作為要插入的檔案的uri。
        return resolver.insert(insertUri, contentValues)
    }

    /**
     * 刪除，透過檔案的uri來實現刪除
     */
    fun delete(
        context: Context,
        dirUri: Uri?,
        displayName: String?
    ) {
        if (!exists(context, dirUri, displayName)) {
            return
        }
        val id = queryId(context, dirUri, displayName)
        val fileUri = Uri.withAppendedPath(dirUri, id.toString())
        val resolver: ContentResolver = context.contentResolver
        resolver.delete(fileUri, null, null)
    }

    /**
     * 判斷檔案是否存在
     */
    fun exists(
        context: Context,
        dirUri: Uri?,
        displayName: String?
    ): Boolean {
        return queryId(context, dirUri, displayName) != -1L
    }

    /**
     * 查詢檔案的id
     */
    fun queryId(
        context: Context,
        dirUri: Uri?,
        displayName: String?
    ): Long {
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            dirUri!!, arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DISPLAY_NAME + "=?", arrayOf(displayName),
            null
        )
        var id: Long = -1
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getLong(0)
            cursor.close()
        }
        return id
    }

    fun closeCloseable(closeable: Closeable?) {
        if (closeable == null) {
            return
        }
        try {
            closeable.close()
        } catch (ignored: IOException) {
        }
    }

    fun getRealPathFromUri(context: Context, uri: Uri?): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getRealPathFromUriAboveApi19(context, uri)
        } else {
            getRealPathFromUriBelowAPI19(context, uri)
        }
    }

    /****** old code *******/


    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param uri 图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    fun getRealPathFromUriBelowAPI19(context: Context, uri: Uri?): String {
        return getDataColumn(context, uri, null, null)
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String {
        var path = ""
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                path = cursor.getString(columnIndex)
            }
        } catch (e: java.lang.Exception) {
            cursor?.close()
        }
        return path
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri!!.authority
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri!!.authority
    }


    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param uri 图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    fun getRealPathFromUriAboveApi19(context: Context, uri: Uri?): String {
        var filePath = ""
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            val documentId = DocumentsContract.getDocumentId(uri)
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                val id = documentId.split(":").toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(id)
                filePath = getDataColumn(
                    context,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection,
                    selectionArgs
                )
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(documentId)
                )
                filePath = getDataColumn(context, contentUri, null, null)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            uri.path?.let {
                filePath = it
            }
        }
        return filePath
    }

}