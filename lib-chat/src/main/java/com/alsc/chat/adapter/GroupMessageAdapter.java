package com.alsc.chat.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.fragment.MyInfoFragment;
import com.alsc.chat.fragment.UserInfoFragment;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.*;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class GroupMessageAdapter extends MessageAdapter {

    private ArrayList<UserBean> mGroupUsers;
    private boolean mIsShowNick;
    private GroupBean mGroup;

    public GroupMessageAdapter(Context context, UserBean myInfo) {
        super(context, myInfo);
        mContext = context;
        mMyInfo = myInfo;
    }

    public void setGroupUsers(ArrayList<UserBean> groupUsers) {
        mGroupUsers = groupUsers;
        notifyDataSetChanged();
    }

    public void setGroup(GroupBean groupBean) {
        mGroup = groupBean;
    }

    public void setShowNick(boolean isShowNick) {
        mIsShowNick = isShowNick;
        notifyDataSetChanged();
    }

    protected void showUserInfo(final BaseViewHolder helper,final BasicMessage item) {
        if (item.isMySendMsg(mMyInfo.getUserId())) {
            Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, mMyInfo.getAvatarUrl(), helper.getView(R.id.ivRight));
            helper.getView(R.id.ivRight).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ChatBaseActivity) mContext).gotoPager(MyInfoFragment.class);
                }
            });
        } else {
            UserBean userBean = getUserById(item.getFromId());
            if (userBean == null && !TextUtils.isEmpty(item.getExtra())) {
                ArrayList<UserBean> users = getGson().fromJson(item.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                if (users != null && !users.isEmpty()) {
                    for (UserBean bean : users) {
                        if (bean.getUserId() == item.getFromId()) {
                            userBean = bean;
                            break;
                        }
                    }
                }
            }
            helper.setGone(R.id.tvNickLeft, !mIsShowNick)
                    .setText(R.id.tvNickLeft, userBean == null ? "" : userBean.getNickName());
            ImageView ivLeft = helper.getView(R.id.ivLeft);
            ivLeft.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setOnItemChildLongClick(v, getItemPosition(item));
                    return true;
                }
            });
            Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, userBean == null ? "" : userBean.getAvatarUrl(), ivLeft);
            ivLeft.setTag(R.id.chat_id, userBean);
            ivLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mGroup.getDisableFriend() == 1) {
                        ((ChatBaseActivity) mContext).showToast(R.string.chat_group_forbid_add_friend);
                        return;
                    }
                    UserBean bean = (UserBean) v.getTag(R.id.chat_id);
                    if (bean != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                        ((ChatBaseActivity) mContext).gotoPager(UserInfoFragment.class, bundle);
                    }
                }
            });
        }
    }

    private UserBean getUserById(long userId) {
        if (mGroupUsers == null || mGroupUsers.isEmpty()) {
            return null;
        }
        for (UserBean bean : mGroupUsers) {
            if (bean.getUserId() == userId) {
                return bean;
            }
        }
        return null;
    }
}
