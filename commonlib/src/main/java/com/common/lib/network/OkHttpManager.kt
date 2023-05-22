package com.common.lib.network

import android.text.TextUtils
import com.common.lib.manager.ConfigurationManager
import com.common.lib.utils.BaseUtils
import com.common.lib.utils.LogUtil
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

class OkHttpManager private constructor() {

    private val TAG: String = "OkHttpManager"
    private var mOkHttpClient: OkHttpClient? = null

    private val mDownloadingUrl =
        HashMap<String, Long>()
    private val mNeedDownLoadingUrl =
        ArrayList<String>()

    val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

    init {
        mOkHttpClient = OkHttpClient()
    }

    companion object {
        @Volatile
        private var instance: OkHttpManager? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: OkHttpManager().also { instance = it }
            }
    }

    operator fun get(url: String, callback: Callback?) {
        val cc: CacheControl = CacheControl.Builder()
            .noCache()
            .noStore()
            .maxStale(5, TimeUnit.SECONDS)
            .build()
        val request: Request = Request.Builder()
            .cacheControl(cc)
            .url(url)
            .build()
        val call = mOkHttpClient!!.newCall(request)
        call.enqueue(callback!!)
    }

    fun post(url: String, map: HashMap<String, Any>, callback: Callback) {
        val body: RequestBody = Gson().toJson(map).toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val call: Call = mOkHttpClient!!.newCall(request)
        call.enqueue(callback)
    }

    fun post(url: String, json: String, callback: Callback) {
        val body: RequestBody = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val call: Call = mOkHttpClient!!.newCall(request)
        call.enqueue(callback)
    }

    /**
     * 异步下载文件
     *
     * @param url
     * @param file 本地文件存储的文件夹
     */
    @Synchronized
    fun downAsyn(
        url: String?,
        file: File,
        callBack: HttpCallBack?
    ) {
        if (file.exists() || TextUtils.isEmpty(url)) {
            return
        }
        if (mDownloadingUrl.size < 5) {
            if (!mDownloadingUrl.containsKey(url)) {
                mDownloadingUrl[url!!] = System.currentTimeMillis()
            } else {
                if (System.currentTimeMillis() - mDownloadingUrl[url!!]!! > 10 * 60 * 1000) {  //超过10分钟还没下载完重新下载
                    mDownloadingUrl[url] = System.currentTimeMillis()
                } else {
                    return
                }
            }
        } else {
            mNeedDownLoadingUrl.add(url!!)
            return
        }
        val request = Request.Builder()
            .url(url)
            .build()
        val call = mOkHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack?.failed(e)
                mDownloadingUrl.remove(url)
//                downloadNext(file.name)
            }

            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                var out: FileOutputStream? = null
                try {
                    `is` = response.body!!.byteStream()
//                    val tempFile = File(
//                        BaseUtils.getSaveFilePath(
//                            ConfigurationManager.getInstance().getContext(),
//                            file.name + ".download"
//                        )
//                    )
                    val tempFile = file;
                    out = FileOutputStream(tempFile)
                    val buf = ByteArray(4096)
                    var len = 0
                    while (`is`.read(buf).also { len = it } != -1) {
                        out.write(buf, 0, len)
                    }
                    out.flush()
                    try {
                        tempFile.renameTo(file)
                        callBack?.successful(file)
                    }catch (e: Exception){
                        callBack?.successful(tempFile)
                    }
                } catch (e: Exception) {
                    callBack?.failed(e)
                } finally {
                    try {
                        `is`?.close()
                        out?.close()
                    } catch (e: Exception) {
                        callBack?.failed(e)
                    }
                    mDownloadingUrl.remove(url)
//                    downloadNext(file.name)
                }
            }
        })
    }

    /**
     * 异步下载文件
     *
     * @param url
     * @param file 本地文件存储的文件夹
     */
    @Synchronized
    fun downloadAsync(
        url: String?,
        file: File,
        callBack: HttpCallBack?
    ) {
        if (file.exists() || TextUtils.isEmpty(url)) {
            return
        }
        if (mDownloadingUrl.size < 5) {
            if (!mDownloadingUrl.containsKey(url)) {
                mDownloadingUrl[url!!] = System.currentTimeMillis()
            } else {
                if (System.currentTimeMillis() - mDownloadingUrl[url!!]!! > 10 * 60 * 1000) {  //超过10分钟还没下载完重新下载
                    mDownloadingUrl[url] = System.currentTimeMillis()
                } else {
                    return
                }
            }
        } else {
            mNeedDownLoadingUrl.add(url!!)
            return
        }
        val request = Request.Builder()
            .url(url)
            .build()
        val call = mOkHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack?.failed(e)
                mDownloadingUrl.remove(url)
                downloadNext(file.name)
            }

            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                var out: FileOutputStream? = null
                try {
                    `is` = response.body!!.byteStream()
                    val tempFile = File(
                        BaseUtils.getSaveFilePath(
                            ConfigurationManager.getInstance().getContext(),
                            file.name + ".download"
                        )
                    )
                    out = FileOutputStream(tempFile)
                    val buf = ByteArray(4096)
                    var len = 0
                    while (`is`.read(buf).also { len = it } != -1) {
                        out.write(buf, 0, len)
                    }
                    out.flush()
                    try {
                        tempFile.renameTo(file)
                        callBack?.successful(file)
                    }catch (e: Exception){
                        callBack?.successful(tempFile)
                    }
                } catch (e: Exception) {
                    callBack?.failed(e)
                } finally {
                    try {
                        `is`?.close()
                        out?.close()
                    } catch (e: Exception) {
                        callBack?.failed(e)
                    }
                    mDownloadingUrl.remove(url)
                    downloadNext(file.name)
                }
            }
        })
    }

    /**
     * 异步下载文件
     *
     * @param url
     * @param file 本地文件存储的文件夹
     */
    @Synchronized
    fun downloadAsyncWithProgress(url: String, file: File, callBack: HttpCallBack?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        val request: Request = Request.Builder()
            .url(url)
            .build()
        val call = mOkHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack?.failed(e)
            }

            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                var out: FileOutputStream? = null
                try {
                    val body = response.body
                    val total = body!!.contentLength()
                    if (total == 0L) {
                        callBack?.failed(null)
                        return
                    }
                    `is` = body.byteStream()
//                    val tempFile = File(
//                        BaseUtils.getSaveFilePath(
//                            ConfigurationManager.getInstance().getContext(),
//                            file.name + ".download"
//                        )
//                    )
                    out = FileOutputStream(file)
                    val buf = ByteArray(4096)
                    var len = 0
                    var progress: Long = 0
                    while (`is`.read(buf).also { len = it } != -1) {
                        out.write(buf, 0, len)
                        progress += len.toLong()
                        callBack?.progress((progress * 100 / total + 0.5).toInt())
                    }
                    out.flush()
                    callBack?.successful(file)
//                    try {
//                        tempFile.renameTo(file)
//                        callBack?.successful(file)
//                    }catch (e: Exception){
//                        LogUtil.LogE("e: "+e.toString())
//                        callBack?.successful(tempFile)
//                    }

                } catch (e: java.lang.Exception) {
                    callBack?.failed(e)
                } finally {
                    try {
                        `is`?.close()
                        out?.close()
                    } catch (e: java.lang.Exception) {
                        callBack?.failed(e)
                    }
                }
            }
        })
    }

    @Synchronized
    private fun downloadNext(fileName: String) {
        if (mNeedDownLoadingUrl.isEmpty()) {
            return
        }
        val url = mNeedDownLoadingUrl.removeAt(mNeedDownLoadingUrl.size - 1)
        val path =
            File(
                BaseUtils.getSaveFilePath(
                    ConfigurationManager.getInstance().getContext(),
                    fileName
                )
            )
        if (!path.exists()) {
            downloadAsync(url, path, null)
        }
    }

    interface HttpCallBack {
        fun successful(file: File)
        fun progress(progress: Int)
        fun failed(e: java.lang.Exception?)
    }

}