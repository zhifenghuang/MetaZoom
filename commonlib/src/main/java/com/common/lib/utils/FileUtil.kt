package com.common.lib.utils

import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {

    fun saveStringFile(string: String, file: File) {
        val parentFile = file.parentFile
        parentFile?.let {
            if (!it.exists()) {
                if (!it.mkdirs()) {
                    try {
                        it.createNewFile()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val outStream: FileOutputStream = FileOutputStream(file)
            try {
                outStream.write(string.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                closeCloseable(outStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeCloseable(closeable: Closeable?) {
        if (closeable == null) {
            return
        }
        try {
            closeable.close()
        } catch (ignored: IOException) {
        }
    }
}