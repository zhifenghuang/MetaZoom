package com.common.lib.bean

class BasicResponse<T> {

    var code: Int = 0 // 返回的结果标志

    var message // 错误描述
            : String? = null

    var totalCount: Int = 0

    var result: T? = null

    fun isSuccess(): Boolean {
        return code == 200
    }

}