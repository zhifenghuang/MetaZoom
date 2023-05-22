package com.common.lib.mvp

interface IPresenter {

    /**
     * 在框架中 { Activity#onCreate() } 和 { Fragment#onViewCreated() }时会默认调用 [IPresenter.onUnbind]
     */
    fun onBind()

    /**
     * 在框架中 { Activity#onDestroy() } 和 { Fragment#onDestroyView() }时会默认调用 [IPresenter.onUnbind]
     */
    fun onUnbind()

    fun logout()

}