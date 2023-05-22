package com.meta.zoom.contract;

import com.common.lib.mvp.IPresenter;
import com.common.lib.mvp.IView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public interface VerifyMnemonicContract {
    public interface View extends IView {
        public void createSuccess();
    }

    public interface Presenter extends IPresenter {

        public void requestColdCreate(int type, ArrayList<HashMap<String, String>> list);
    }
}
