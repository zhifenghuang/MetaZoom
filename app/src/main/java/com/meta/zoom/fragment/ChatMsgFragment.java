package com.meta.zoom.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alsc.chat.fragment.AddFriendFragment;
import com.alsc.chat.fragment.ChatBaseFragment;
import com.alsc.chat.fragment.ChatListFragment;
import com.alsc.chat.fragment.ContactFragment;
import com.alsc.chat.fragment.GroupListFragment;
import com.alsc.chat.fragment.LeaveMsgFragment;
import com.alsc.chat.fragment.MyCollectionFragment;
import com.alsc.chat.fragment.SearchFragment;
import com.alsc.chat.fragment.SelectFriendFragment;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.fragment.BaseFragment;
import com.common.lib.manager.DataManager;
import com.meta.zoom.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMsgFragment extends ChatBaseFragment {

    private ArrayList<UserBean> mFriendList;
    private ArrayList<GroupBean> mGroupList;
    private ChatListFragment mChatListFragment;
    private ContactFragment mFriendListFragment;
    private GroupListFragment mGroupListFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_msg;
    }

    @Override
    protected void onViewCreated(View view) {
        mChatListFragment = new ChatListFragment();
        mFriendListFragment = new ContactFragment();
        mGroupListFragment = new GroupListFragment();
        switchFragment(mChatListFragment);
        setViewsOnClickListener(R.id.tvChat, R.id.tvContacts, R.id.tvGroup, R.id.ivSearch,
                R.id.ivAdd, R.id.ivMenu);
        setData(DataManager.getInstance().getFriends(), DataManager.getInstance().getGroups());
    }


    public void setNewVerify(boolean isHadNew) {
//        if (getView() == null) {
//            return;
//        }
//        getView().findViewById(R.id.ivNewFriend).setVisibility(isHadNew ? View.VISIBLE : View.INVISIBLE);
//        ((ContactFragment) mBaseFragment.get(1)).setNewVerify(isHadNew);
    }


    @Override
    public void updateUIText() {
        setNewVerify(DataManager.getInstance().isHasNewVerify());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvChat:
                switchFragment(mChatListFragment);
                TextView textView = (TextView) v;
                textView.setBackgroundResource(R.drawable.shape_7a5bd0_8_left);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_3));

                textView = fv(R.id.tvContacts);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));

                textView = fv(R.id.tvGroup);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));
                break;
            case R.id.tvContacts:
                switchFragment(mFriendListFragment);
                textView = (TextView) v;
                textView.setBackgroundColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.color_7a_5b_d0));
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_3));

                textView = fv(R.id.tvChat);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));

                textView = fv(R.id.tvGroup);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));
                break;
            case R.id.tvGroup:
                switchFragment(mGroupListFragment);
                textView = (TextView) v;
                textView.setBackgroundResource(R.drawable.shape_7a5bd0_8_right);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_3));

                textView = fv(R.id.tvChat);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));

                textView = fv(R.id.tvContacts);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));
                break;
            case R.id.ivMenu:
                showMoreOperatorDialog();
                break;
            case R.id.ivAdd:
                gotoPager(AddFriendFragment.class);
                break;
            case R.id.ivSearch:
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.BUNDLE_EXTRA, SearchFragment.SEARCH_LOCAL_FRIEND);
                gotoPager(SearchFragment.class, bundle);
                break;
        }
    }

    public void setData(ArrayList<UserBean> friendList, ArrayList<GroupBean> groupList) {
        mFriendList = friendList;
        mGroupList = groupList;
        if (getView() == null || mChatListFragment == null || mFriendListFragment == null) {
            return;
        }
        mChatListFragment.setData(mFriendList, mGroupList);
        mFriendListFragment.setData(mFriendList);
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onReceive(ChangeLanguageEvent event) {
//        if (getView() == null) {
//            return;
//        }
//        setText(R.id.tvTitle, R.string.wallet_contact);
//        setText(R.id.tvChatMsg, R.string.wallet_chat);
//        setText(R.id.tvContact, R.string.wallet_chat_friend);
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (map != null) {
            if (map.containsKey(Constants.REDRESH_FRIENDS)) {
                getFriendFromServer();
            } else if (map.containsKey(Constants.REMOVE_FRIEND)) {
                long userId = (long) map.get(Constants.REMOVE_FRIEND);
                removeFriend(userId);
                mFriendListFragment.setData(mFriendList);
                mChatListFragment.setData(mFriendList, mGroupList);
            } else if (map.containsKey(Constants.BLOCK_FRIEND)) {
                long userId = (long) map.get(Constants.BLOCK_FRIEND);
                blockFriend(userId, 1);
                mFriendListFragment.setData(mFriendList);
                mChatListFragment.setData(mFriendList, mGroupList);
            } else if (map.containsKey(Constants.REMOVE_BLOCK)) {
                long userId = (long) map.get(Constants.REMOVE_BLOCK);
                blockFriend(userId, 0);
                mFriendListFragment.setData(mFriendList);
                mChatListFragment.setData(mFriendList, mGroupList);
            }
        }
    }

    private void removeFriend(long userId) {
        if (mFriendList == null || mFriendList.isEmpty()) {
            return;
        }
        for (UserBean userBean : mFriendList) {
            if (userBean.getContactId() == userId) {
                DatabaseOperate.getInstance().deleteUserChatRecord(DataManager.getInstance().getUser().getUserId(), userId);
                mFriendList.remove(userBean);
                DataManager.getInstance().saveFriends(mFriendList);
                return;
            }
        }
    }

    private void blockFriend(long userId, int block) {
        if (mFriendList == null || mFriendList.isEmpty()) {
            return;
        }
        for (UserBean userBean : mFriendList) {
            if (userBean.getContactId() == userId) {
                userBean.setBlock(block);
                DataManager.getInstance().saveFriends(mFriendList);
                return;
            }
        }
    }

    @Override
    public boolean isNeedSetTopStyle() {
        return true;
    }

    public void showMoreOperatorDialog() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_chat_list_more_dialog);
        dialogFragment.setClickDismiss(false);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                UserBean myInfo = DataManager.getInstance().getUser();
                ((TextView) view.findViewById(R.id.tvNick)).setText(myInfo.getNickName());
                String account = myInfo.getLoginAccount();
                ((TextView) view.findViewById(R.id.tvID)).setText("ID: " + account.substring(0, 6) + "..." + account.substring(account.length() - 6));
                int resId = getResources().getIdentifier("chat_default_avatar_" + myInfo.getUserId() % 6,
                        "drawable", getActivity().getPackageName());
                Utils.loadImage(getActivity(), resId, myInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.tvNewGroup,
                        R.id.tvAddFriend, R.id.ivClose, R.id.tvFAQ, R.id.tvID, R.id.tvMyCollection);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.tvNewGroup) {
                    if (mFriendList == null || mFriendList.isEmpty()) {
                        showToast(R.string.app_no_friends);
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_GROUP);
                    gotoPager(SelectFriendFragment.class, bundle);
                    dialogFragment.dismiss();
                } else if (viewId == R.id.tvAddFriend) {
                    gotoPager(AddFriendFragment.class);
                    dialogFragment.dismiss();
                } else if (viewId == R.id.tvFAQ) {
                    dialogFragment.dismiss();
                } else if (viewId == R.id.tvMyCollection) {
                    gotoPager(MyCollectionFragment.class);
                    dialogFragment.dismiss();
                } else if (viewId == R.id.tvID) {
                    ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", DataManager.getInstance().getUser().getLoginAccount());
                    cm.setPrimaryClip(mClipData);
                    showToast(com.alsc.chat.R.string.chat_copy_successful);
                } else if (viewId == R.id.ivClose) {
                    dialogFragment.dismiss();
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    public int getContainerViewId() {
        return R.id.fl;
    }

}
