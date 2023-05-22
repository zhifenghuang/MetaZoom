package com.common.lib.utils

import java.util.*

object UidUtil {
    fun createUid ():String{
        return UUID.randomUUID().toString().replace("-","").toUpperCase()
    }
}