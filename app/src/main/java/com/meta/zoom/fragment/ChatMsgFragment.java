package com.meta.zoom.fragment;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alsc.chat.fragment.ChatBaseFragment;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;
import com.meta.zoom.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMsgFragment extends ChatBaseFragment {

    private Fragment mCurrentFragment;

    private ArrayList<UserBean> mFriendList;
    private ArrayList<GroupBean> mGroupList;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
    }


    public void setNewVerify(boolean isHadNew) {
        if (getView() == null) {
            return;
        }
//        getView().findViewById(R.id.ivNewFriend).setVisibility(isHadNew ? View.VISIBLE : View.INVISIBLE);
//        ((ContactFragment) mBaseFragment.get(1)).setNewVerify(isHadNew);
    }

    /**
     * @param to 马上要切换到的Fragment，一会要显示
     */
    private void switchFragment(Fragment to) {
        if (mCurrentFragment != to) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            if (!to.isAdded()) {
                if (mCurrentFragment != null) {
                    ft.hide(mCurrentFragment);
                }
                ft.add(R.id.fl, to, to.toString()).commit();
            } else {
                if (mCurrentFragment != null) {
                    ft.hide(mCurrentFragment);
                }
                ft.show(to).commit();
            }
        }
        mCurrentFragment = to;
    }


    @Override
    public void updateUIText() {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
//        if (map != null) {
//            if (map.containsKey(Constants.REDRESH_FRIENDS)) {
//                ((MainActivity) getActivity()).getFriendFromServer();
//            } else if (map.containsKey(Constants.REMOVE_FRIEND)) {
//                long userId = (long) map.get(Constants.REMOVE_FRIEND);
//                removeFriend(userId);
//                mFriendListFragment.setData(mFriendList);
//                mChatListFragment.setData(mFriendList, mGroupList);
//            } else if (map.containsKey(Constants.BLOCK_FRIEND)) {
//                long userId = (long) map.get(Constants.BLOCK_FRIEND);
//                blockFriend(userId, 1);
//                mFriendListFragment.setData(mFriendList);
//                mChatListFragment.setData(mFriendList, mGroupList);
//            } else if (map.containsKey(Constants.REMOVE_BLOCK)) {
//                long userId = (long) map.get(Constants.REMOVE_BLOCK);
//                blockFriend(userId, 0);
//                mFriendListFragment.setData(mFriendList);
//                mChatListFragment.setData(mFriendList, mGroupList);
//            }
//        }
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
        return false;
    }

}
