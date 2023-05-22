package com.alsc.chat.fragment;


import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class UpdateNickFragment extends ChatBaseFragment {

    private UserBean mUserInfo;
    private boolean isMySelf;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_update_nick;
    }

    @Override
    protected void onViewCreated(View view) {
        mUserInfo = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        UserBean myInfo = DataManager.getInstance().getUser();
        isMySelf = mUserInfo.getUserId() == myInfo.getUserId();
        setTopStatusBarStyle(view);

        final TextView tvLeft = fv(R.id.tvLeft);
        tvLeft.setVisibility(View.VISIBLE);
        tvLeft.setOnClickListener(this);
        setText(tvLeft, getString(R.string.chat_finish));

        EditText etMemo = view.findViewById(R.id.etName);
        setText(R.id.tvTitle, isMySelf ? R.string.chat_update_nick : R.string.chat_add_memo);
        setViewGone(R.id.tvLetterNum);
        etMemo.setHint(getString(isMySelf ? R.string.chat_nick : R.string.chat_friend_memo));
        etMemo.setMaxLines(1);
        etMemo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        etMemo.setText(mUserInfo.getMemo());
        etMemo.setSelection(Math.min(mUserInfo.getMemo().length(), 20));
        etMemo.requestFocus();
        setViewsOnClickListener(R.id.ivClear);
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvLeft) {
            String text = getTextById(R.id.etName);
            if (TextUtils.isEmpty(text)) {
                return;
            }
            if (!isMySelf) {
                ChatHttpMethods.getInstance().operateContact(mUserInfo.getContactId(), -1, text, -1, new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        mUserInfo.setMemo(text);
                        HashMap<String, UserBean> map = new HashMap<>();
                        map.put(Constants.EDIT_FRIEND, mUserInfo);
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
            } else {
                ChatHttpMethods.getInstance().updateUserProfile(text, "", -1, "", new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.UPDATE_NICK, text);
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
            }
        } else if (id == R.id.ivClear) {
            setText(R.id.etName, "");
        }
    }


}
