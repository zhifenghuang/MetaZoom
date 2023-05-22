package com.meta.zoom.contract;

import com.common.lib.mvp.IPresenter;
import com.common.lib.mvp.IView;

public interface MainContract {
    public interface View extends IView {
        public void loginSuccess();
    }

    public interface Presenter extends IPresenter {

        public void login(String address);
    }
}
