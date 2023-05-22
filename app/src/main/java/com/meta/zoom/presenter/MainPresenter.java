package com.meta.zoom.presenter;

import com.alsc.chat.manager.ChatManager;
import com.common.lib.bean.UserBean;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.BasePresenter;
import com.common.lib.network.HttpListener;
import com.common.lib.network.HttpMethods;
import com.common.lib.network.HttpObserver;
import com.meta.zoom.contract.MainContract;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {

    public MainPresenter(@NotNull MainContract.View rootView) {
        super(rootView);
    }

    @Override
    public void login(String address) {
        HttpMethods.Companion.getInstance().login(address, new HttpObserver(getRootView(), new HttpListener<UserBean>() {
            @Override
            public void onSuccess(@Nullable UserBean bean) {
                if (getRootView() == null) {
                    return;
                }
                DataManager.getInstance().saveUser(bean);
                ChatManager.getInstance().initWebSocket(bean.getToken());
                getRootView().loginSuccess();
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
