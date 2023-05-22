package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alsc.chat.BuildConfig;
import com.alsc.chat.R;
import com.alsc.chat.utils.CacheManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.AppUpgradeDialog;
import com.common.lib.manager.DataManager;

public class OtherSettingFragment extends ChatBaseFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_other_setting;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.wallet_other_setting);
        setText(R.id.tvVer, "V" + DataManager.getInstance().getVersionName());
        try {
            setText(R.id.tvMemory, CacheManager.getTotalCacheSize(getActivity()));
        } catch (Exception e) {

        }
        setViewsOnClickListener(R.id.tvLanguage, R.id.llCheckNewVer, R.id.llClearMemory);
    }

    @Override
    public void updateUIText() {
        setText(R.id.tvTitle, R.string.wallet_other_setting);
        setText(R.id.tvLanguage, R.string.chat_language);
        setText(R.id.tvCheckVer, R.string.chat_check_new_version);
        setText(R.id.tvClearMemory, R.string.chat_clear_memory);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvLanguage) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, ChooseFragment.CHOOSE_LANGUAGE);
            bundle.putInt(Constants.BUNDLE_EXTRA_2, DataManager.getInstance().getLanguage());
            gotoPager(ChooseFragment.class, bundle);
        } else if (id == R.id.llCheckNewVer) {
            checkVersion();
        } else if (id == R.id.llClearMemory) {
            CacheManager.clearAllCache(getActivity());
            try {
                showToast(R.string.chat_clear_success);
                String cacheSize = CacheManager.getTotalCacheSize(getActivity());
                if (!TextUtils.isEmpty(cacheSize)) {
                    setText(R.id.tvMemory, cacheSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void checkVersion() {
//        HttpMethods.getInstance().checkVersion(new HttpObserver(new SubscriberOnNextListener<VersionBean>() {
//            @Override
//            public void onNext(VersionBean bean, String msg) {
//                if (getActivity() == null || getView() == null || bean == null) {
//                    return;
//                }
//                if (bean.getType() > 0) {
//                    AppUpgradeDialog dialog = new AppUpgradeDialog(getActivity(), bean);
//                    dialog.show();
//                } else {
//                    showToast(com.common.R.string.current_is_newest_version);
//                }
//            }
//        }, getActivity(), (BaseActivity) getActivity()));
    }
}
