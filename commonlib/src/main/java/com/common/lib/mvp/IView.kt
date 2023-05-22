package com.common.lib.mvp

interface IView {

    fun showProgressDialog()

    fun dismissProgressDialog()

    fun showErrorDialog(errorCode: Int, msg: String?)
}