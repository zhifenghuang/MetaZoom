package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;

import java.util.ArrayList;

public class ForbidSayManagerFragment extends ChatBaseFragment {

    private GroupBean mGroup;
    private ArrayList<UserBean> mGroupUsers;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_forbid_say_manager;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        mGroupUsers = (ArrayList<UserBean>) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_forbid_chat_manager);
        setViewsOnClickListener(R.id.tvAddForbid, R.id.tvRemoveForbid);
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvAddForbid) {
            getBlockGroupUsers(SelectFriendFragment.FROM_GROUP_ADD_BLOCK);
        } else if (id == R.id.tvRemoveForbid) {
            getBlockGroupUsers(SelectFriendFragment.FROM_GROUP_REMOVE_BLOCK);
        }
    }

    /**
     * 获取禁言用户
     */
    private void getBlockGroupUsers(final int toType) {
        ChatHttpMethods.getInstance().getGroupUsers(String.valueOf(mGroup.getGroupId()), "1", new HttpObserver(new SubscriberOnNextListener<ArrayList<UserBean>>() {
            @Override
            public void onNext(ArrayList<UserBean> list, String msg) {
                if (getView() == null) {
                    return;
                }
                if (toType == SelectFriendFragment.FROM_GROUP_REMOVE_BLOCK && (list == null || list.isEmpty())) {
                    showToast(R.string.chat_nobody_had_forbid);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.BUNDLE_EXTRA, toType);
                bundle.putSerializable(Constants.BUNDLE_EXTRA_2, list);
                bundle.putSerializable(Constants.BUNDLE_EXTRA_3, mGroup);
                DataManager.getInstance().setObject(mGroupUsers.clone());
                gotoPager(SelectFriendFragment.class, bundle);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

}
