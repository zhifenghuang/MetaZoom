package com.common.lib.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.lib.R
import com.common.lib.constant.EventBusEvent
import com.common.lib.dialog.CommonProgressDialog
import com.common.lib.dialog.MyDialogFragment
import com.common.lib.interfaces.OnClickCallback
import com.common.lib.manager.DataManager
import com.common.lib.utils.BaseUtils
import org.greenrobot.eventbus.EventBus

abstract class BaseDialogFragment : BaseMediaFragment() {

    private var progressDialog: CommonProgressDialog? = null

    abstract fun onLogout()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dismissProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
    }

    fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = CommonProgressDialog(context)
            progressDialog!!.setCanceledOnTouchOutside(false)
        }
        progressDialog!!.show()
    }

    fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    fun showErrorDialog(errorCode: Int, msg: String?) {
        if (TextUtils.isEmpty(msg)) {
            return
        }
        val dialogFragment = showOneBtnDialog(msg, getString(R.string.common_ok))
        dialogFragment?.setOnDismiss(listener = object : MyDialogFragment.IDismissListener {
            override fun onDismiss() {
//                if (errorCode == ErrorCode.ACCOUNT_DELETED || errorCode == ErrorCode.TOKEN_TIME_OUT) {
//                    logout()
//                }
            }
        })
    }

    fun logout() {
        onLogout()
        DataManager.getInstance().loginOut()
        EventBus.getDefault().post(BaseUtils.getMap(EventBusEvent.FINISH_ACTIVITIES, ""))
    }

    open fun showOneBtnDialog(
        msg: String?,
        btnText: String?,
        callBack: OnClickCallback? = null
    ): MyDialogFragment? {
        return showOneBtnDialog(null, msg, btnText, callBack)
    }

    open fun showOneBtnDialog(
        msg: String?
    ): MyDialogFragment? {
        return showOneBtnDialog(
            getString(R.string.common_tip),
            msg,
            getString(R.string.common_ok),
            null
        )
    }

    open fun showOneBtnDialog(
        title: String?,
        msg: String?,
        btnText: String?,
        callBack: OnClickCallback? = null
    ): MyDialogFragment? {
        val dialogFragment = MyDialogFragment(R.layout.layout_one_btn_dialog)
        dialogFragment.setOnMyDialogListener(object : MyDialogFragment.OnMyDialogListener {
            override fun initView(view: View?) {
                if (TextUtils.isEmpty(title)) {
                    view!!.findViewById<View>(R.id.tv1).visibility = View.GONE
                } else {
                    (view!!.findViewById<View>(R.id.tv1) as TextView).text = title
                }
                (view.findViewById<View>(R.id.tv2) as TextView).text = msg
                (view.findViewById<View>(R.id.btn2) as TextView).text = btnText
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn2)
            }

            override fun onViewClick(viewId: Int) {
                callBack?.onClick(viewId)
            }
        })
        dialogFragment.show(childFragmentManager, "MyDialogFragment")
        return dialogFragment
    }


    open fun showTwoBtnDialog(
        msg: String?,
        btnText1: String?,
        btnText2: String?,
        callBack: OnClickCallback? = null
    ) {
        showTwoBtnDialog(null, msg, btnText1, btnText2, callBack)
    }

    open fun showTwoBtnDialog(
        title: String?,
        msg: String?,
        btnText1: String?,
        btnText2: String?,
        callBack: OnClickCallback? = null
    ) {
        val dialogFragment = MyDialogFragment(R.layout.layout_two_btn_dialog)
        dialogFragment.setOnMyDialogListener(object : MyDialogFragment.OnMyDialogListener {
            override fun initView(view: View?) {
                if (TextUtils.isEmpty(title)) {
                    view!!.findViewById<View>(R.id.tv1).visibility = View.GONE
                } else {
                    (view!!.findViewById<View>(R.id.tv1) as TextView).text = title
                }
                (view.findViewById<View>(R.id.tv2) as TextView).text = msg
                (view.findViewById<View>(R.id.btn1) as TextView).text = btnText1
                (view.findViewById<View>(R.id.btn2) as TextView).text = btnText2
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2)
            }

            override fun onViewClick(viewId: Int) {
                if (viewId == R.id.btn2) {
                    callBack?.onClick(viewId)
                }
            }
        })
        dialogFragment.show(childFragmentManager, "MyDialogFragment")
    }

//    open fun showSelectPhotoTypeDialog() {
//        val dialogFragment = MyDialogFragment(R.layout.layout_select_media_type)
//        dialogFragment.setOnMyDialogListener(object : MyDialogFragment.OnMyDialogListener {
//            override fun initView(view: View?) {
//                (view!!.findViewById<View>(R.id.btnTakePhoto) as TextView).text =
//                    getTextByKey("prfl_txt_camera")
//                (view.findViewById<View>(R.id.btnAlbum) as TextView).text =
//                    getTextByKey("prfl_txt_gallery")
//                (view.findViewById<View>(R.id.btnCancel) as TextView).text =
//                    getTextByKey("pblc_btn_cancel")
//                dialogFragment.setDialogViewsOnClickListener(
//                    view,
//                    R.id.view,
//                    R.id.btnTakePhoto,
//                    R.id.btnAlbum,
//                    R.id.btnCancel
//                )
//            }
//
//            override fun onViewClick(viewId: Int) {
//                when (viewId) {
//                    R.id.btnTakePhoto -> {
//                        openCamera()
//                    }
//                    R.id.btnAlbum -> {
//                        openGallery()
//                    }
//                }
//            }
//        })
//        dialogFragment.show(childFragmentManager, "MyDialogFragment")
//    }

}