package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.AppUpgradeDialog;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

public class PrivacyFragment extends ChatBaseFragment {

    private ChatSettingBean mChatSetting;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_privacy_setting;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_privacy_setting);
        setViewsOnClickListener(R.id.tvDeleteAfterRead, R.id.llP2pPassword, R.id.llNeedVerfiySwitch, R.id.tvBlackList);
        UserBean myInfo = DataManager.getInstance().getUser();
        setImage(R.id.ivNeedVerifySwitch, myInfo.getAllowAdd() == 0 ?
                R.drawable.icon_switch_off : R.drawable.icon_switch_on);
    }

    @Override
    public void updateUIText() {
        mChatSetting = DataManager.getInstance().getChatSetting();
        setImage(R.id.ivP2pPasswordSwitch, mChatSetting.getIsAddPasswordP2P() == 0 ?
                R.drawable.icon_switch_off : R.drawable.icon_switch_on);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvDeleteAfterRead) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, ChooseFragment.CHOOSE_DELETE_TYPE);
            bundle.putInt(Constants.BUNDLE_EXTRA_2, mChatSetting.getReadDeleteType());
            gotoPager(ChooseFragment.class, bundle);
        } else if (id == R.id.llP2pPassword) {
            int isAddPasswordP2P = mChatSetting.getIsAddPasswordP2P();
            mChatSetting.setIsAddPasswordP2P(isAddPasswordP2P == 0 ? 1 : 0);
            setImage(R.id.ivP2pPasswordSwitch, mChatSetting.getIsAddPasswordP2P() == 0 ?
                    R.drawable.icon_switch_off : R.drawable.icon_switch_on);
            DataManager.getInstance().saveChatSetting(mChatSetting);
        } else if (id == R.id.llNeedVerfiySwitch) {
            final UserBean myInfo = DataManager.getInstance().getUser();
            final int allowAdd = myInfo.getAllowAdd() == 0 ? 1 : 0;
            ChatHttpMethods.getInstance().updateUserAllowAdd(allowAdd, new HttpObserver(new SubscriberOnNextListener() {
                @Override
                public void onNext(Object o, String msg) {
                    myInfo.setAllowAdd(allowAdd);
                    if (getView() != null) {
                        setImage(R.id.ivNeedVerifySwitch, myInfo.getAllowAdd() == 0 ?
                                R.drawable.icon_switch_off : R.drawable.icon_switch_on);
                    }
                }
            }, getActivity(), (ChatBaseActivity) getActivity()));
        } else if (id == R.id.tvBlackList) {
            gotoPager(BlacklistFragment.class);
        }
//        else if (id == R.id.tvMsgFilter) {
//
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(ChooseFragment.CHOOSE_DELETE_TYPE)) {
                ChooseFragment.ChooseType type = (ChooseFragment.ChooseType) map.get(ChooseFragment.CHOOSE_DELETE_TYPE);
                mChatSetting.setReadDeleteType(type.type);
                DataManager.getInstance().saveChatSetting(mChatSetting);
            }
        }
    }
}
