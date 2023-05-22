package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.adapter.LabelFriendAdapter;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.http.ChatHttpMethods;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.bean.*;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class BlacklistFragment extends ChatBaseFragment {

    private LabelFriendAdapter mAdapter;

    private ArrayList<UserBean> mBlockList;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_black_list;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_blacklist);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        getBlockUsers();
    }

    private LabelFriendAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new LabelFriendAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, getAdapter().getItem(position));
                    gotoPager(UserInfoFragment.class, bundle);
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

    private void getBlockUsers() {
        ChatHttpMethods.getInstance().getBlockUsers(1, new HttpObserver(new SubscriberOnNextListener<ArrayList<UserBean>>() {
            @Override
            public void onNext(ArrayList<UserBean> list, String msg) {
                if (getView() == null) {
                    return;
                }
                mBlockList = list;
                setBlockList();
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (map != null) {
            if (map.containsKey(Constants.BLOCK_FRIEND)) {
                long userId = (long) map.get(Constants.BLOCK_FRIEND);
                blockUser(userId, 1);

            } else if (map.containsKey(Constants.REMOVE_BLOCK)) {
                long userId = (long) map.get(Constants.REMOVE_BLOCK);
                blockUser(userId, 0);
            }
        }
    }

    private void blockUser(long userId, int block) {
        if (mBlockList == null || mBlockList.isEmpty()) {
            return;
        }
        for (UserBean userBean : mBlockList) {
            if (userBean.getContactId() == userId) {
                userBean.setBlock(block);
                break;
            }
        }
        setBlockList();
    }

    private void setBlockList() {
        if (mBlockList == null || mBlockList.isEmpty()) {
            return;
        }
        ArrayList<UserBean> list = new ArrayList<>();
        for (UserBean bean : mBlockList) {
            if (bean.getBlock() == 1) {
                list.add(bean);
            }
        }
        getAdapter().setNewData(list);
        getAdapter().notifyDataSetChanged();
    }
}
