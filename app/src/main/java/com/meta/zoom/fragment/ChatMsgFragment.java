package com.meta.zoom.fragment;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alsc.chat.fragment.AddFriendFragment;
import com.alsc.chat.fragment.ChatBaseFragment;
import com.alsc.chat.fragment.ChatListFragment;
import com.alsc.chat.fragment.ContactFragment;
import com.alsc.chat.fragment.LeaveMsgFragment;
import com.alsc.chat.fragment.MyCollectionFragment;
import com.alsc.chat.fragment.SearchFragment;
import com.alsc.chat.fragment.SelectFriendFragment;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.fragment.BaseFragment;
import com.common.lib.manager.DataManager;
import com.meta.zoom.Manifest;
import com.meta.zoom.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMsgFragment extends ChatBaseFragment {
    private ArrayList<BaseFragment> mBaseFragment;

    private ArrayList<UserBean> mFriendList;
    private ArrayList<GroupBean> mGroupList;
    private ChatListFragment mChatListFragment;
    private ContactFragment mFriendListFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_msg;
    }

    @Override
    protected void onViewCreated(View view) {
        initFragments();
        switchFragment(mBaseFragment.get(0));
        setViewsOnClickListener(R.id.tvChatMsg, R.id.llContact, R.id.ivSearch,
                R.id.ivAdd,R.id.ivMenu);
    }

    private void initFragments() {
        mBaseFragment = new ArrayList<>();
        mChatListFragment = new ChatListFragment();
        mBaseFragment.add(mChatListFragment);
        mFriendListFragment = new ContactFragment();
        mBaseFragment.add(mFriendListFragment);
    }

    public void setNewVerify(boolean isHadNew) {
        if (getView() == null) {
            return;
        }
        getView().findViewById(R.id.ivNewFriend).setVisibility(isHadNew ? View.VISIBLE : View.INVISIBLE);
        ((ContactFragment) mBaseFragment.get(1)).setNewVerify(isHadNew);
    }


    @Override
    public void updateUIText() {
        setNewVerify(DataManager.getInstance().isHasNewVerify());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvChatMsg:
                switchFragment(mBaseFragment.get(0));
                LinearLayout ll2 = fv(R.id.llContact);
                TextView textView = (TextView) v;
                textView.setBackgroundResource(com.alsc.chat.R.drawable.shape_7a5bd0_19);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_3));

                textView = (TextView) ll2.getChildAt(0);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));
                break;
            case R.id.llContact:
                switchFragment(mBaseFragment.get(1));
                ll2 = (LinearLayout) v;
                textView = (TextView) ll2.getChildAt(0);
                textView.setBackgroundResource(com.alsc.chat.R.drawable.shape_7a5bd0_19);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_3));

                textView = fv(R.id.tvChatMsg);
                textView.setBackgroundResource(0);
                textView.setTextColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_1));
                break;
            case R.id.ivMenu:
            //    showMoreOperatorDialog();
                break;
            case R.id.ivAdd:
                showMoreOperatorDialog();
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
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                dialogFragment.setDialogViewsOnClickListener(view, R.id.llRoot, R.id.tvStartGroupChat,
                        R.id.tvAddFriend, R.id.tvScan, R.id.tvFeedback, R.id.tvMyCollection);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.tvStartGroupChat) {
                    if (mFriendList == null || mFriendList.isEmpty()) {
                        showToast(R.string.chat_no_friend);
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_GROUP);
                    gotoPager(SelectFriendFragment.class, bundle);
                } else if (viewId == R.id.tvAddFriend) {
                    gotoPager(AddFriendFragment.class);
                } else if (viewId == R.id.tvScan) {
                    if (!Utils.isGrantPermission(getActivity(),
                            Manifest.permission.CAMERA)) {
                        ((BaseActivity) getActivity()).requestPermission(0, Manifest.permission.CAMERA);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("wallet://capture"));
                        startActivity(intent);
                    }
                } else if (viewId == R.id.tvFeedback) {
                    gotoPager(LeaveMsgFragment.class);
                } else if (viewId == R.id.tvMyCollection) {
                    gotoPager(MyCollectionFragment.class);
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    public int getContainerViewId() {
        return R.id.fl;
    }

}
