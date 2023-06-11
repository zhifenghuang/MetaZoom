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
import com.common.lib.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
                setViewVisible(R.id.tvAddToContract);
                setViewGone(R.id.ivToChat, R.id.tvAddNoteName, R.id.line, R.id.llNotification,
                        R.id.paddingView, R.id.llBlock);
                setViewsOnClickListener(R.id.tvAddToContract);
            } else {
                setViewsOnClickListener(R.id.tvAddNoteName, R.id.ivToChat, R.id.llNotification, R.id.llBlock);
                setViewGone(R.id.tvAddToContract);
                resetBlockUI();
            }

        }
        resetUI();
    }

    private void resetUI() {
        setText(R.id.tvNick, mUserInfo.getNickName());
        String account = mUserInfo.getLoginAccount();
        setText(R.id.tvID, "ID: " + account.substring(0, 6) + "..." + account.substring(account.length() - 6));
        int resId = getResources().getIdentifier("chat_default_avatar_" + mUserInfo.getUserId() % 6,
                "drawable", getActivity().getPackageName());
        Utils.loadImage(getActivity(), resId, mUserInfo.getAvatarUrl(), fv(R.id.ivAvatar));
    }

    @Override
    public void updateUIText() {
        setImage(R.id.ivNotification, mUserInfo.getIgnore() == 0 ? R.drawable.chat_switch_on : R.drawable.chat_switch_off);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvAddToContract) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mUserInfo);
            bundle.putInt(Constants.BUNDLE_EXTRA_2, VerifyApplyFragment.ADD_BY_QRCODE);
            gotoPager(VerifyApplyFragment.class, bundle);
        } else if (id == R.id.llBlock) {
            blockUser(mUserInfo.getBlock() == 1 ? 0 : 1);
        } else if (id == R.id.ivToChat) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mUserInfo);
            gotoPager(ChatFragment.class, bundle);
            ((BaseActivity) getActivity()).finishAllOtherActivity();
        } else if (id == R.id.tvAddNoteName) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mUserInfo);
            gotoPager(UpdateNickFragment.class, bundle);
        } else if (id == R.id.llNotification) {
            int msgSwitch = mUserInfo.getIgnore() == 1 ? 0 : 1;
            operatorIgnore(msgSwitch);
        }
    }

    private void resetBlockUI() {
        LogUtil.LogE("block: " + mUserInfo.getBlock());
        if (mUserInfo.getBlock() == 1) {
            setImage(R.id.ivBlock, R.drawable.chat_unblock);
            setText(R.id.tvBlock, R.string.chat_unblock);
            setTextColor(R.id.tvBlock, com.common.lib.R.color.text_color_2);
        } else {
            setImage(R.id.ivBlock, R.drawable.chat_shield);
            setText(R.id.tvBlock, R.string.chat_shield);
            setTextColor(R.id.tvBlock, com.common.lib.R.color.text_color_8);
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
                resetBlockUI();
                HashMap<String, Long> map = new HashMap<>();
                if (block == 1) {
                    map.put(Constants.BLOCK_FRIEND, mUserInfo.getContactId());
                } else {
                    map.put(Constants.REMOVE_BLOCK, mUserInfo.getContactId());
                }
                EventBus.getDefault().post(map);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void operatorIgnore(final int ignore) {
        ChatHttpMethods.getInstance().operateContact(mUserInfo.getContactId(), -1, ignore, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                mUserInfo.setIgnore(ignore);
                setImage(R.id.ivNotification, mUserInfo.getIgnore() == 0 ? R.drawable.chat_switch_on : R.drawable.chat_switch_off);
                HashMap<String, UserBean> map = new HashMap<>();
                map.put(Constants.EDIT_FRIEND, mUserInfo);
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
                resetBlockUI();
                HashMap<String, UserBean> map = new HashMap<>();
                map.put(Constants.EDIT_FRIEND, mUserInfo);
                EventBus.getDefault().post(map);
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.EDIT_FRIEND)) {
                mUserInfo = (UserBean) map.get(Constants.EDIT_FRIEND);
                resetUI();
            } else if (map.containsKey(Constants.REMOVE_FRIEND)) {
                finish();
            }
        }
    }

}
