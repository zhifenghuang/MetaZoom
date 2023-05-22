package com.common.lib.mvp

import android.util.Log
import com.common.lib.network.HttpListener
import com.common.lib.network.HttpObserver
import com.common.lib.network.HttpMethods
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BasePresenter<V : IView>(rootView: V) : IPresenter {

    protected var rootView: V? = null

    protected val compositeDisposable = CompositeDisposable()

    init {
        this.rootView = rootView
    }

    override fun onBind() {
        Log.i("BasePresenter", "onStart")
    }

    override fun onUnbind() {
        Log.i("BasePresenter", "onDestroy")
        compositeDisposable.dispose()
        rootView = null
    }

    override fun logout() {

    }
}