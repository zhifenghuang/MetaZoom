package com.common.lib.network

interface HttpListener<Data> {

    fun onSuccess(bean: Data?)

    fun dataError(code: Int, msg: String?)

    fun connectError(e: Throwable?)
}