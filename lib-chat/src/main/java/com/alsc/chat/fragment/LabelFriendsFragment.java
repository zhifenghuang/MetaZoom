package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.LabelFriendAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class LabelFriendsFragment extends ChatBaseFragment {

    private LabelFriendAdapter mAdapter;

    private LabelBean mLabel;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_label_friends;
    }

    @Override
    protected void onViewCreated(View view) {
        mLabel = (LabelBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, mLabel.getName() + "(" + mLabel.getContactCount() + ")");
        setViewVisible(R.id.tvLeft);
        setText(R.id.tvLeft, R.string.chat_edit);
        setViewsOnClickListener(R.id.tvLeft);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        getLabelFriends();
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
        int id = v.getId();
        if (id == R.id.tvLeft) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, EditLabelFragment.EDIT_LABEL);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mLabel);
            DataManager.getInstance().setObject(getAdapter().getData());
            gotoPager(EditLabelFragment.class, bundle);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.EDIT_LABEL)) {
                mLabel = (LabelBean) map.get(Constants.EDIT_LABEL);
                setText(R.id.tvLeft, mLabel.getName() + "(" + mLabel.getContactCount() + ")");
                getAdapter().setNewData(mLabel.getLabelFriends());
            } else if (map.containsKey(Constants.DELETE_LABEL)) {
                finish();
            }
        }
    }

    private void getLabelFriends() {
        ChatHttpMethods.getInstance().getLabelFriends(mLabel.getTagId(), new HttpObserver(new SubscriberOnNextListener<ArrayList<UserBean>>() {
            @Override
            public void onNext(ArrayList<UserBean> list, String msg) {
                if (getView() == null) {
                    return;
                }
                getAdapter().setNewData(list);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

}
