package com.meta.zoom.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alsc.chat.fragment.ChatBaseFragment;
import com.alsc.chat.fragment.MyInfoFragment;
import com.alsc.chat.fragment.PrivacyFragment;
import com.alsc.chat.utils.Utils;
import com.common.lib.bean.UserBean;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.BaseUtils;
import com.meta.zoom.R;

public class SettingFragment extends ChatBaseFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting;
    }


    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setTopStatusBarStyle(R.id.tvTitle);
        UserBean myInfo = DataManager.getInstance().getUser();
        setText(R.id.tvNick, myInfo.getNickName());
        String account = myInfo.getLoginAccount();
        setText(R.id.tvID, "ID: " + account.substring(0, 6) + "..." + account.substring(account.length() - 6));
        int resId = getResources().getIdentifier("chat_default_avatar_" + myInfo.getUserId() % 6,
                "drawable", getActivity().getPackageName());
        Utils.loadImage(getActivity(), resId, myInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));

        setViewsOnClickListener(R.id.tvID, R.id.flEditProfile, R.id.flInviteFriends, R.id.tvChatPrivacy);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvID:
                BaseUtils.StaticParams.copyData(getActivity(), DataManager.getInstance().getUser().getLoginAccount());
                showToast(com.alsc.chat.R.string.chat_copy_successful);
                break;
            case R.id.flEditProfile:
                gotoPager(MyInfoFragment.class);
                break;
            case R.id.tvChatPrivacy:
                gotoPager(PrivacyFragment.class);
                break;
        }
    }


    public void onRefresh() {
        if (getView() == null) {
            return;
        }
        UserBean myInfo = DataManager.getInstance().getUser();
        setText(R.id.tvNick, myInfo.getNickName());
        String account = myInfo.getLoginAccount();
        setText(R.id.tvID, "ID: " + account.substring(0, 6) + "..." + account.substring(account.length() - 6));
        int resId = getResources().getIdentifier("chat_default_avatar_" + myInfo.getUserId() % 6,
                "drawable", getActivity().getPackageName());
        Utils.loadImage(getActivity(), resId, myInfo.getAvatarUrl(), getView().findViewById(R.id.ivAvatar));
    }

}
