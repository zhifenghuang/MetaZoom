package com.common.lib.network

import com.common.lib.bean.BasicResponse
import com.common.lib.bean.UserBean
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST


interface Api {

    /**
     * 创建或导入冷钱包
     *
     * @param ask
     * @return
     */
    @POST("api/cold/create")
    fun  //Post请求发送数据
            requestColdCreate(@Body map: HashMap<String, Any>): Observable<BasicResponse<Any>> //@body即非表单请求体，被@Body注解的ask将会被Gson转换成json发送到服务器，返回到Take。 // 其中返回类型为Call<*>，*是接收数据的类


    @POST("api/v1/passport/login")
    fun login(
        @Body map: HashMap<String, Any>
    ): Observable<BasicResponse<UserBean>>

}