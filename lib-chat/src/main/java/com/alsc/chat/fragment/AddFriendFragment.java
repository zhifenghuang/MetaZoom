package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.common.lib.bean.UserBean;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;

import java.util.ArrayList;

public class AddFriendFragment extends ChatBaseFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_add_friend;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(R.id.topView);
        setText(R.id.tvTitle, R.string.chat_add_friend);
        setViewsOnClickListener(R.id.ivSearch);
        EditText et = view.findViewById(R.id.etSearch);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    searchContact();
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivSearch) {
            searchContact();
        }
    }

    private void searchContact() {
        String text = getTextById(R.id.etSearch);
        if (TextUtils.isEmpty(text)) {
            showToast(R.string.chat_id_phone_email);
            return;
        }
        ChatHttpMethods.getInstance().searchContact(text, new HttpObserver(new SubscriberOnNextListener<UserBean>() {
            @Override
            public void onNext(UserBean user, String msg) {
                if (getView() == null || user == null) {
                    return;
                }
                user.setContactId(user.getUserId());
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, user);

                UserBean myInfo = DataManager.getInstance().getUser();
                if (myInfo.isService()) {  //如果是客服直接让客服搜索发消息
                    gotoPager(UserInfoFragment.class, bundle);
                    return;
                }

                boolean isFriend = false;
                ArrayList<UserBean> list = DataManager.getInstance().getFriends();
                if (list != null && !list.isEmpty()) {
                    for (UserBean bean : list) {
                        if (bean.getContactId() == user.getUserId()) {
                            isFriend = true;
                            break;
                        }
                    }
                }
                if (!isFriend) {
                    bundle.putInt(Constants.BUNDLE_EXTRA_2, VerifyApplyFragment.ADD_BY_ID);
                }
                gotoPager(isFriend ? UserInfoFragment.class : VerifyApplyFragment.class, bundle);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

}
