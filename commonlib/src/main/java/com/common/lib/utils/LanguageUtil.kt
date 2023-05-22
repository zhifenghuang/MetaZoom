package com.common.lib.utils

import java.util.*

object LanguageUtil {

    /**
     * 获取系統語言
     *
     * @param context 上下文
     * @return
     */
    fun getLanguage(): String {
        val systemLocale = Locale.getDefault()
        if (systemLocale == Locale.SIMPLIFIED_CHINESE || systemLocale == Locale.CHINA || systemLocale == Locale.CHINESE || systemLocale.country == "CN" || systemLocale.toString()
                .contains("_#Hans")
        ) {
            return "zh_CN"
        } else if (systemLocale == Locale.TRADITIONAL_CHINESE || systemLocale == Locale.TAIWAN || systemLocale.toString()
                .contains("_#Hant")
            || systemLocale.country == "TW" || systemLocale.country == "HK"
        ) {
            return "zh_TW"
        }
        return "en_US"
    }
}