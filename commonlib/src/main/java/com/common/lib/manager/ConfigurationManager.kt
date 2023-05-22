package com.common.lib.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

class ConfigurationManager private constructor() {

    private var mContext: Context? = null
    private var mActivityRecord: Int = 0

    companion object {
        @Volatile
        private var instance: ConfigurationManager? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ConfigurationManager().also { instance = it }
            }
    }

    fun setContext(context: Context?) {
        mContext = context

        (mContext as Application).registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
            }

            override fun onActivityStarted(activity: Activity) {
                ++mActivityRecord
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                --mActivityRecord
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {
            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }

    fun getContext() =
        mContext

    fun isInApp(): Boolean =
        (mActivityRecord > 0)
}