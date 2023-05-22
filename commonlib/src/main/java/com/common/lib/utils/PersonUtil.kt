package com.common.lib.utils

import android.content.Context

object PersonUtil {
    fun getStrByType(context: Context, list: List<Int>?, content: String): String {
        if (list == null || list.isEmpty()) {
            return ""
        }
        var str = ""
        val array = BaseUtils.getTextByKey(context, content).split(",")
        for (index in list) {
            for (s in array) {
                if (s.startsWith(index.toString() + "_")) {
                    str += s.split("_")[1] + ","
                    break
                }
            }
        }
        return str.substring(0, str.length - 1)
    }
}