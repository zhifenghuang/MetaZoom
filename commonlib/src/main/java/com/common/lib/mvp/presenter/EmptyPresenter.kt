package com.common.lib.mvp.presenter

import com.common.lib.mvp.BasePresenter
import com.common.lib.mvp.contract.EmptyContract


class EmptyPresenter(rootView: EmptyContract.View) : BasePresenter<EmptyContract.View>(rootView),
    EmptyContract.Presenter