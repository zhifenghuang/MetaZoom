package com.meta.zoom.presenter;

import com.common.lib.mvp.BasePresenter;
import com.common.lib.network.HttpListener;
import com.common.lib.network.HttpMethods;
import com.common.lib.network.HttpObserver;
import com.meta.zoom.contract.VerifyMnemonicContract;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class VerifyMnemonicPresenter extends BasePresenter<VerifyMnemonicContract.View> implements VerifyMnemonicContract.Presenter {

    public VerifyMnemonicPresenter(@NotNull VerifyMnemonicContract.View rootView) {
        super(rootView);
    }

    @Override
    public void requestColdCreate(int type, ArrayList<HashMap<String, String>> list) {
        HttpMethods.Companion.getInstance().requestColdCreate(type, list, new HttpObserver(getRootView(), new HttpListener<Object>() {
                    @Override
                    public void onSuccess(@Nullable Object object) {
                        if (getRootView() == null) {
                            return;
                        }
                        getRootView().createSuccess();
                    }

                    @Override
                    public void dataError(@Nullable int code, @Nullable String msg) {
                        if (getRootView() == null) {
                            return;
                        }
                        getRootView().showErrorDialog(code, msg);
                    }

                    @Override
                    public void connectError(@Nullable Throwable e) {
                        if (getRootView() == null) {
                            return;
                        }
                        getRootView().showErrorDialog(-1, "");
                    }
                }, getCompositeDisposable()));
    }
}
