package com.common.lib.widget


import android.os.Handler
import android.os.Looper
import java.util.*

class TimeCounter(var listener: Listener?) {
    companion object {
        const val ONE_SECOND = 1000L
    }

    interface Listener {
        fun onNextSecond(currentTimeMillis: Long)
    }

    private var timer: Timer? = null

    /**
     * 終點
     * **/
    private var expiredTimeMillis = 0L

    /**
     * 前一秒
     * **/
    private var preSecondTimeMillis = 0L

    fun startTimer(expiredTimeMillis: Long) {
        this.expiredTimeMillis = expiredTimeMillis
        this.preSecondTimeMillis = System.currentTimeMillis()

        stopTimer()

        timer = Timer()

        val monitor = object : TimerTask() {
            override fun run() {
                val currentTimeMillis = System.currentTimeMillis()

                if (currentTimeMillis >= expiredTimeMillis) {
                    stopTimer()

                } else if (currentTimeMillis - preSecondTimeMillis >= ONE_SECOND) {
                    preSecondTimeMillis = currentTimeMillis
                    callListener(currentTimeMillis)
                }
            }
        }

        timer!!.schedule(monitor, 0, ONE_SECOND)
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        callListener(System.currentTimeMillis())
    }

    fun destroy() {
        stopTimer()
        listener = null
    }

    private fun callListener(currentTimeMillis: Long) {
        Handler(Looper.getMainLooper()).post {
            if (listener == null) return@post
            listener!!.onNextSecond(currentTimeMillis)
        }
    }

}