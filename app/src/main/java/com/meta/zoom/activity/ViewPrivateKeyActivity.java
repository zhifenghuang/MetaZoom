package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.constant.Constants;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.BaseUtils;
import com.meta.zoom.R;

public class ViewPrivateKeyActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private String mPrivateKey;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_view_private_key;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_view_private_key);
        setViewsOnClickListener(R.id.tvCopy);
        Bundle bundle = getIntent().getExtras();
        mPrivateKey = bundle.getString(Constants.BUNDLE_EXTRA);
        setText(R.id.tvPrivateKey, mPrivateKey);
    }


    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvCopy:
                BaseUtils.StaticParams.copyData(this, mPrivateKey);
                showToast(com.alsc.chat.R.string.chat_copy_successful);
                break;
        }
    }
}
