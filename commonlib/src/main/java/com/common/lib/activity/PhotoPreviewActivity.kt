package com.common.lib.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.common.lib.R
import com.common.lib.constant.Constants
import com.common.lib.constant.EventBusEvent
import com.common.lib.mvp.contract.EmptyContract
import com.common.lib.mvp.presenter.EmptyPresenter
import com.common.lib.utils.BaseUtils
import com.common.lib.utils.BitmapUtil
import com.common.lib.view.ShowPicView
import org.greenrobot.eventbus.EventBus

class PhotoPreviewActivity : BaseActivity<EmptyPresenter>(), EmptyContract.View {

    private var mFilePath: String? = null
    private var mBmp: Bitmap? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_photo_preview
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        mFilePath = intent.extras!!.getString(Constants.BUNDLE_EXTRA)
        val showPicView: ShowPicView = findViewById(R.id.ivShowPic)
        mBmp = BitmapUtil.getBitmapFromFile(
            mFilePath!!,
            getDisplayMetrics()!!.widthPixels,
            getDisplayMetrics()!!.heightPixels
        )
        showPicView.setImageBitmap(mBmp, false)
        setViewsOnClickListener(R.id.tvCancel, R.id.tvOk)
    }

    override fun onCreatePresenter(): EmptyPresenter {
        return EmptyPresenter(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvCancel -> {
                finish()
            }
            R.id.tvOk -> {
                EventBus.getDefault()
                    .post(BaseUtils.getMap(EventBusEvent.PHOTO_PATH, mFilePath!!))
                finish()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mBmp != null && !mBmp!!.isRecycled) {
            mBmp!!.recycle()
        }
        mBmp = null
    }

}