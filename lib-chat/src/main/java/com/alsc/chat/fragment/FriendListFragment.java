package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.FriendAdapter;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.bean.FriendItem;
import com.common.lib.bean.UserBean;
import com.common.lib.manager.DataManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendListFragment extends ChatBaseFragment {

    private FriendAdapter mAdapter;

    private ArrayList<UserBean> mFriendList;
    private boolean mIsHadNewVerify;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_friend_list;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        mFriendList = DataManager.getInstance().getFriends();
        getAdapter().setNewData(getNewList(mFriendList));
    }

    private FriendAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new FriendAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (position > 0) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, mAdapter.getItem(position).getFriend());
                        gotoPager(UserInfoFragment.class, bundle);
                    }
                }
            });
        }
        return mAdapter;
    }

    @Override
    public void updateUIText() {
    }

    @Override
    public void onClick(View v) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.EDIT_FRIEND)) {
                UserBean userBean = (UserBean) map.get(Constants.EDIT_FRIEND);
                editFriend(userBean);
            }
        }
    }

    public void setData(ArrayList<UserBean> friendList) {
        mFriendList = friendList;
        if (getView() != null) {
            getAdapter().setNewData(getNewList(mFriendList));
            getAdapter().notifyDataSetChanged();
        }
    }

    public void setNewVerify(boolean isHasNewVerify) {
        if (getView() != null) {
            getAdapter().setNew(isHasNewVerify);
        }
    }

    private void editFriend(UserBean bean) {
        if (mFriendList == null || mFriendList.isEmpty()) {
            return;
        }
        int index = 0;
        for (UserBean userBean : mFriendList) {
            if (userBean.getBlock() == 1) {
                ++index;
                continue;
            }
            if (userBean.getContactId() == bean.getContactId()) {
                mFriendList.set(index, bean);
                DataManager.getInstance().saveFriends(mFriendList);
                List<FriendItem> items = getAdapter().getData();
                index = 0;
                for (FriendItem item : items) {
                    if (item.getItemType() == FriendItem.VIEW_TYPE_3
                            && item.getFriend().getContactId() == bean.getContactId()) {
                        item.setFriend(bean);
                        getAdapter().notifyDataSetChanged();
                        return;
                    }
                    ++index;
                }
                return;
            }
            ++index;
        }
    }

    private ArrayList<FriendItem> getNewList(ArrayList<UserBean> list) {
        ArrayList<FriendItem> newList = new ArrayList<>();
        FriendItem item = new FriendItem();
        item.setItemType(FriendItem.VIEW_TYPE_0);
        newList.add(item);
        if (list != null) {
            for (UserBean bean : list) {
                if (bean.getBlock() == 1) {
                    continue;
                }
                item = new FriendItem();
                item.setItemType(FriendItem.VIEW_TYPE_3);
                item.setFriend(bean);
                newList.add(item);
            }
        }
        return newList;
    }
}
