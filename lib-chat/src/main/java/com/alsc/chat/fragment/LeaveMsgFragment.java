package com.alsc.chat.fragment;


import android.text.TextUtils;
import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;

public class LeaveMsgFragment extends ChatBaseFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_leave_msg;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_please_leave_your_msg);
        setViewsOnClickListener(R.id.tvSubmit);
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvSubmit) {
            String text = getTextById(R.id.etMsg);
            if (TextUtils.isEmpty(text)) {
                ((BaseActivity) getActivity()).showToast(getString(R.string.chat_not_input_your_problem));
                return;
            }

            ChatHttpMethods.getInstance().suggest(text, "", null, new HttpObserver(new SubscriberOnNextListener() {
                @Override
                public void onNext(Object o, String msg) {
                    if (getView() == null) {
                        return;
                    }
                    setText(R.id.etMsg, "");
                    ((BaseActivity) getActivity()).showToast(getString(R.string.chat_leave_msg_successful));
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },1100);
                }
            }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }


}
