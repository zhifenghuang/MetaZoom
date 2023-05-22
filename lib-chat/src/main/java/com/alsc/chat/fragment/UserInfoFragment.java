package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

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
import com.alsc.chat.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInfoFragment extends ChatBaseFragment {

    private UserBean mUserInfo;

    private boolean mIsFriend;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user_info;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        mUserInfo = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        mUserInfo.setUserId(mUserInfo.getContactId());
        UserBean myInfo = DataManager.getInstance().getUser();
        if (mUserInfo.getContactId() != myInfo.getUserId()) {
            refreshUserInfo();
            mIsFriend = false;
            ArrayList<UserBean> list = DataManager.getInstance().getFriends();
            for (UserBean bean : list) {
                if (bean.getContactId() == mUserInfo.getContactId()) {
                    mIsFriend = true;
                    break;
                }
            }
            if (!myInfo.isService() && !mIsFriend) {
                setViewVisible(R.id.llBottom);
                setImage(R.id.iv1, R.drawable.icon_add_friend);
                setText(R.id.tv1, R.string.chat_apply_to_friend);
            } else {
                if (mUserInfo.getBlock() == 1) {
                    setViewVisible(R.id.llBottom, R.id.ll2, R.id.tvBlockTip);
                    setImage(R.id.iv1, R.drawable.icon_had_black);
                    setText(R.id.tv1, R.string.chat_had_black);
                    setImage(R.id.iv2, R.drawable.icon_remove_from_blacklist);
                    setText(R.id.tv2, R.string.chat_remove_from_black_list);
                }
            }

        }
        setViewsOnClickListener(R.id.ll1, R.id.ll2, R.id.ivAvatar);
        resetUI();
    }

    private void resetUI() {
        setText(R.id.tvNick, mUserInfo.getNickName());
        Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, mUserInfo.getAvatarUrl(), fv(R.id.ivAvatar));
        setText(R.id.tvLocation, getString(mUserInfo.getGender() == 1 ? R.string.chat_male : R.string.chat_female) + "  " +
                (TextUtils.isEmpty(mUserInfo.getDistrict()) ? getString(R.string.chat_default_area) : mUserInfo.getDistrict()));
        setText(R.id.tvId, getString(R.string.chat_account_2, mUserInfo.getLoginAccount()));
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll1) {
            if (!mIsFriend) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mUserInfo);
                bundle.putInt(Constants.BUNDLE_EXTRA_2, VerifyApplyFragment.ADD_BY_QRCODE);
                gotoPager(VerifyApplyFragment.class, bundle);
            }
        } else if (id == R.id.ll2) {
            if (mUserInfo.getBlock() == 1) {
                blockUser(0);
            }
        } else if (id == R.id.ivAvatar) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mUserInfo);
            if (DataManager.getInstance().getUser().isService() || mIsFriend) {
                gotoPager(ChatFragment.class, bundle);
                ((BaseActivity) getActivity()).finishAllOtherActivity();
            } else {
                bundle.putInt(Constants.BUNDLE_EXTRA_2, VerifyApplyFragment.ADD_BY_QRCODE);
                gotoPager(VerifyApplyFragment.class, bundle);
            }
        }
    }

    private void blockUser(final int block) {
        ChatHttpMethods.getInstance().operateContact(mUserInfo.getContactId(), -1, "", block, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                mUserInfo.setBlock(block);
                HashMap<String, Long> map = new HashMap<>();
                if (block == 1) {
                    map.put(Constants.BLOCK_FRIEND, mUserInfo.getContactId());
                } else {
                    map.put(Constants.REMOVE_BLOCK, mUserInfo.getContactId());
                }
                setViewGone(R.id.llBottom);
                EventBus.getDefault().post(map);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }


    private void refreshUserInfo() {
        ChatHttpMethods.getInstance().getContactProfile(String.valueOf(mUserInfo.getContactId()), new HttpObserver(new SubscriberOnNextListener<UserBean>() {
            @Override
            public void onNext(UserBean bean, String msg) {
                if (getView() == null || bean == null) {
                    return;
                }
                mUserInfo = bean;
                resetUI();
                HashMap<String, UserBean> map = new HashMap<>();
                map.put(Constants.EDIT_FRIEND, mUserInfo);
                EventBus.getDefault().post(map);
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

}
