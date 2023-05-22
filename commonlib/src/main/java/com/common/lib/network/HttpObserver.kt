package com.common.lib.network


import android.os.Handler
import android.os.Looper
import android.os.Message
import com.common.lib.bean.BasicResponse
import com.common.lib.constant.EventBusEvent
import com.common.lib.manager.DataManager
import com.common.lib.mvp.IView
import com.common.lib.utils.LogUtil
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.greenrobot.eventbus.EventBus

class HttpObserver<T : BasicResponse<Data>, Data> : Observer<T> {

    companion object {
        const val SHOW_LOADING = 1
        const val HIDE_LOADING = 2
    }

    private var isShowLoading: Boolean? = true
    private var view: IView? = null
    private var listener: HttpListener<Data>? = null
    private var compositeDisposable: CompositeDisposable? = null


    constructor(
        listener: HttpListener<Data>,
        compositeDisposable: CompositeDisposable?
    ) : this(false, null, listener, compositeDisposable)

    constructor(
        view: IView?,
        listener: HttpListener<Data>,
        compositeDisposable: CompositeDisposable?
    ) : this(true, view, listener, compositeDisposable)

    constructor(
        isShowLoading: Boolean,
        view: IView?,
        listener: HttpListener<Data>,
        compositeDisposable: CompositeDisposable?
    ) {
        this.isShowLoading = isShowLoading
        this.view = view
        this.listener = listener
        this.compositeDisposable = compositeDisposable
    }

    override fun onComplete() {
        hideLoading()
    }

    override fun onSubscribe(d: Disposable) {
        compositeDisposable?.add(d)
        showLoading()
    }

    override fun onNext(t: T) {
        if (t.isSuccess()) {
            listener?.onSuccess(t.result)
        } else {
            if (t.code == 402) {
                val map = HashMap<String, String>()
                map[EventBusEvent.TOKEN_EXPIRE] = ""
                EventBus.getDefault().post(map)
                DataManager.getInstance().loginOut()
            }
            listener?.dataError(t.code, t.message)
        }
    }

    override fun onError(e: Throwable) {
        hideLoading()
        LogUtil.LogE(e.message)
        listener?.connectError(e)
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SHOW_LOADING -> {
                    view?.showProgressDialog()
                }
                HIDE_LOADING -> {
                    view?.dismissProgressDialog()
                }
            }
        }
    }

    fun showLoading() {
        if (isShowLoading!! && view != null) {
            mHandler.obtainMessage(SHOW_LOADING).sendToTarget()
        }
    }

    fun hideLoading() {
        if (isShowLoading!! && view != null) {
            mHandler.obtainMessage(HIDE_LOADING).sendToTarget()
        }
    }

}