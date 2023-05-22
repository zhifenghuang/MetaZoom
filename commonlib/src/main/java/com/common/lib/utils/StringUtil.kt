package com.common.lib.utils

import android.content.Context
import android.text.TextUtils
import com.common.lib.utils.LanguageUtil.getLanguage

object StringUtil {

    fun getString(context: Context, key: String): String {
        var value = PrefUtil.getString(
            context,
            key + "_${getLanguage()}",
            ""
        )
        if (!TextUtils.isEmpty(value)) {
            return value!!
        }
        try {
            val stringId = context.resources.getIdentifier(
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
}