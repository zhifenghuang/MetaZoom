package com.meta.zoom.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.fragment.BaseFragment;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;

public class SettingFragment extends BaseFragment<EmptyContract.Presenter> implements EmptyContract.View {


    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wallet;
    }


    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setTopStatusBarStyle(view);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }



    public void onRefresh() {
        if (getView() == null) {
            return;
        }
    }

}
