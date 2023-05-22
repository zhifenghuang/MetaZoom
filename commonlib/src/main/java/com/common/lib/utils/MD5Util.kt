package com.common.lib.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5Util {
    fun getMd5(string: String?): String? {
        if (string == null) {
            return ""
        }
        return try {
            val lDigest =
                MessageDigest.getInstance("MD5")
            lDigest.update(string.toByteArray())
            val lHashInt = BigInteger(1, lDigest.digest())
            String.format("%1$032x", lHashInt)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}