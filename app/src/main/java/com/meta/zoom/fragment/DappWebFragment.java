package com.meta.zoom.fragment;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.CaptureActivity;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.fragment.BaseFragment;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.PermissionUtil;
import com.meta.zoom.R;
import com.meta.zoom.activity.DappWebActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

public class DappWebFragment extends BaseFragment<EmptyContract.Presenter> implements EmptyContract.View {
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dapp_web;
    }


    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setViewsOnClickListener(R.id.ivScan);
        setTopStatusBarStyle(view);
        EditText etUrl = view.findViewById(R.id.etUrl);
        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String url = getTextById(v);
                    if (!TextUtils.isEmpty(url)) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, url);
                        openActivity(DappWebActivity.class, bundle);
                    }
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivScan:
                if (!PermissionUtil.INSTANCE.isGrantPermission(getActivity(), Manifest.permission.CAMERA)) {
                    requestPermission(null, Manifest.permission.CAMERA);
                    return;
                }
                openActivity(CaptureActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap<String, Object> map) {
        if (map == null) {
            return;
        }
        if (map.containsKey(EventBusEvent.SCAN_RESULT)) {
            String url = (String) map.get(EventBusEvent.SCAN_RESULT);
            if (!TextUtils.isEmpty(url)) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, url);
                openActivity(DappWebActivity.class, bundle);
            }
        }
    }
}
