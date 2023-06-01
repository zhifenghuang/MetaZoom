package com.meta.zoom.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.TokenBean;
import com.common.lib.constant.Constants;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.QRCodeUtil;
import com.meta.zoom.R;

public class ReceiveActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private TokenBean mToken;
    private Bitmap mQrBmp;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_receive_assets;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, "");
        setImage(R.id.ivBack, R.drawable.app_back_white);
        setViewsOnClickListener(R.id.flCopy);
        mToken = (TokenBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        int drawableId = 0;
        if (TextUtils.isEmpty(mToken.getContractAddress())) {
            try {
                drawableId = getResources().getIdentifier("app_symbol_" + mToken.getChainId(), "drawable", getPackageName());
            } catch (Exception e) {
            }
        }
        setImage(R.id.ivSymbol, drawableId == 0 ? R.drawable.app_unknow_symbol : drawableId);
        mQrBmp = QRCodeUtil.createQRImage(this, mToken.getWalletAddress(), null);
        setText(R.id.tvAddress, mToken.getWalletAddress());
        setText(R.id.tvName, getString(R.string.app_xxx_receive, mToken.getSymbol()));
        ((ImageView) findViewById(R.id.ivQRCode)).setImageBitmap(mQrBmp);
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flCopy:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", mToken.getWalletAddress());
                cm.setPrimaryClip(mClipData);
                showToast(com.alsc.chat.R.string.chat_copy_successful);
                break;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mQrBmp != null) {
            mQrBmp.recycle();
        }
        mQrBmp = null;
    }
}
