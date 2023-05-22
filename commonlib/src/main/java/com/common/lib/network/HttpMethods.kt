package com.common.lib.network

import com.common.lib.bean.*
import com.common.lib.utils.LogUtil
import com.google.gson.GsonBuilder
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.jvm.Throws


class HttpMethods private constructor() {

    private val TAG: String = "HttpMethods"

    private val api: Api

    private var mRetrofit: Retrofit? = null
    private val mBuilder: OkHttpClient.Builder

    companion object {
        const val TAG = "HttpMethods"
        const val CONNECT_TIMEOUT: Long = 90
        const val WRITE_TIMEOUT: Long = 90
        const val READ_TIMEOUT: Long = 90

        @Volatile
        private var instance: HttpMethods? = null

        fun getInstance() =
            instance
                ?: synchronized(this) {
                    instance
                        ?: HttpMethods()
                            .also { instance = it }
                }
    }

    init {
        mBuilder = OkHttpClient.Builder()
        val loggingInterceptor =
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    LogUtil.LogE(message)
                }
            })
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)


        val interceptor: Interceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val builder = chain.request()
                    .newBuilder()
//                val token = DataManager.getInstance().getToken()
//                if (!TextUtils.isEmpty(token)) {
//                    builder.addHeader("Authorization", token!!)
//                }
//                builder.addHeader(
//                    "Accept-Language",
//                    if (DataManager.getInstance().getLanguage() == 0) {
//                        "en-us"
//                    } else {
//                        "zh-cn"
//                    }
//                )
                builder.addHeader("Platform", "APP_ANDROID")
                return chain.proceed(builder.build())
            }
        }
        mBuilder
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(loggingInterceptor)
        resetRetrofit()
        api = mRetrofit!!.create(Api::class.java)
    }

    fun resetRetrofit() {
        mRetrofit = Retrofit.Builder()
            .client(mBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl("http://im.metazoom.pro")//
            .build()
    }

    fun requestColdCreate(
        type: Int, list: List<HashMap<String, String>>,
        observer: HttpObserver<BasicResponse<Any>, Any>
    ) {
        val map = HashMap<String, Any>()
        map["type"] = type
        map["list"] = list
        val observable = api.requestColdCreate(map)
        toSubscribe(observable, observer)
    }

    fun login(
        address: String,
        observer: HttpObserver<BasicResponse<UserBean>, UserBean>
    ) {
        val map = HashMap<String, Any>()
        map["address"] = address
        val observable = api.login(map)
        toSubscribe(observable, observer)
    }


    private fun <T : BasicResponse<Data>, Data> toSubscribe(
        observable: Observable<T>,
        observer: HttpObserver<T, Data>
    ) {
        observable.retry(2) { throwable ->
            throwable is SocketTimeoutException ||
                    throwable is ConnectException ||
                    throwable is TimeoutException
        }.subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

}