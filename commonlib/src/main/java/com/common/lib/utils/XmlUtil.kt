package com.common.lib.utils

import android.util.Log
import android.util.Xml
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

object XmlUtil {
    const val TAG = "XmlUtil"
    private const val REG_1 = "^[a-zA-Z_$][a-zA-Z0-9_]*"
    private const val REG_2 = "[^a-zA-Z_0-9]"
    private const val REG_3 = "[^a-zA-Z_$]"

    fun getXmlString(jsonObject: JSONObject): String {
        val outputStream = ByteArrayOutputStream()
        var result = ""
        try {
            val iterator = jsonObject.keys()
            val serializer = Xml.newSerializer()
            serializer.setOutput(outputStream, "UTF-8")
            serializer.startDocument("UTF-8", true)
            serializer.startTag(null, "resources")
            var key: String
            var value: String
            while (iterator.hasNext()) {
                key = iterator.next()
                value = jsonObject.getString(key)
                key = checkKey(key)
                value = checkValue(value, true)
                serializer.startTag(null, "string")
                serializer.attribute(null, "name", key)
                serializer.text(value)
                serializer.endTag(null, "string")
            }
            serializer.endTag(null, "resources")
            serializer.endDocument()

            result = outputStream.toString()
        } catch (e: Exception) {
            Log.e(TAG, "create xml string e: $e")
        } finally {
            outputStream.flush()
            outputStream.close()
        }

        return result
    }

    fun checkKey(key: String): String {
        var newKey = key
        val p1 = Pattern.compile(REG_1)
        val p2 = Pattern.compile(REG_2)
        val p3 = Pattern.compile(REG_3)
        val m1 = p1.matcher(key)

        if (!m1.matches()) {
            for (i in 1 until key.length - 1) {
                val c = key.substring(i, i + 1)
                val m2 = p2.matcher(c)
                if (m2.matches()) {
                    newKey = newKey.replace(c, " ")
                    continue
                }
            }
            while (true) {
                val c = newKey.substring(0, 1)
                val m3 = p3.matcher(c)
                if (m3.matches()) {
                    newKey = newKey.substring(1, newKey.length)
                    continue
                }
                break
            }
            if (newKey.contains(" ")) {
                newKey = newKey.replace(" ", "")
            }
            return newKey
        }
        return key
    }

    fun checkValue(value: String, createXml: Boolean): String {
        var value = value
        val reg = ".*[%][sd].*"
        var count = 0
        if (value.contains("%@")) {
            value = value.replace("%@", "%s")
        }
        if (createXml) {
            if (value.contains("\n")) {
                value = value.replace("\n", "\\n")
            }
        }
        val p = Pattern.compile(reg)
        val m = p.matcher(value)
        if (m.matches()) {
            for (j in 0 until value.length) {
                val s = value.substring(j, j + 1)
                if (s == "%") {
                    count++
                }
            }
            if (count > 1) {
                value = value.replace("%", "#")
                for (i in 1..count) {
                    val regx = "[#]"
                    value = value.replaceFirst(regx.toRegex(), "%$i\\$")
                }
            }
        }
        if (createXml) {
            if (value.contains("'")) {
                value = value.replace("\'", "\\'")
            }
        }
        return value
    }

}