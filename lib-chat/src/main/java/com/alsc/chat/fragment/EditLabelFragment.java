package com.alsc.chat.fragment;

import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.adapter.LabelUserAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class EditLabelFragment extends ChatBaseFragment {

    private int mOperatorLabelType;

    public static final int EDIT_LABEL = 0;
    public static final int SAVE_LABEL = 1;

    private LabelUserAdapter mAdapter;

    private LabelBean mLabel;

    private ArrayList<UserBean> mLabelUsers, mShowLabelUsers;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_label;
    }

    @Override
    protected void onViewCreated(View view) {
        mOperatorLabelType = getArguments().getInt(Constants.BUNDLE_EXTRA, EDIT_LABEL);
        setTopStatusBarStyle(view);
        setViewsOnClickListener(R.id.tvDeleteLabel, R.id.tvLeft);
        setViewVisible(R.id.tvLeft);
        if (mOperatorLabelType == EDIT_LABEL) {
            mLabel = (LabelBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
            setText(R.id.tvTitle, R.string.chat_edit_label);
            setText(R.id.tvLeft, R.string.chat_save);
            setViewVisible(R.id.llDeleteLabel);
            setText(R.id.etLabelName, mLabel.getName());
        } else {
            setText(R.id.tvTitle, R.string.chat_save_as_label);
            setText(R.id.tvLeft, R.string.chat_save);
            setViewGone(R.id.llDeleteLabel);
        }
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 5);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        mLabelUsers = new ArrayList<>();
        mShowLabelUsers = new ArrayList<>();
    }

    private LabelUserAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new LabelUserAdapter(getActivity());
        }
        return mAdapter;
    }

    @Override
    public void updateUIText() {
        Object object = DataManager.getInstance().getObject();
        if (object instanceof ArrayList) {
            mLabelUsers.addAll((ArrayList<UserBean>) object);
        }
        mShowLabelUsers.clear();
        mShowLabelUsers.addAll(mLabelUsers);
        mShowLabelUsers.add(null);
        mShowLabelUsers.add(null);
        getAdapter().setUsers(mLabelUsers);
        getAdapter().setNewData(mShowLabelUsers);
        getAdapter().notifyDataSetChanged();
        DataManager.getInstance().setObject(null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvLeft) {
            final String name = getTextById(R.id.etLabelName);
            if (TextUtils.isEmpty(name)) {
                return;
            }
            final ArrayList<UserBean> users = getAdapter().getSelectedUsers();
            if (users.isEmpty()) {
                return;
            }
            if (mOperatorLabelType == EDIT_LABEL) {
                ChatHttpMethods.getInstance().editLabel(mLabel.getTagId(), name, users, new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        mLabel.setName(name);
                        mLabel.setContactCount(users.size());
                        mLabel.setLabelFriends(users);
                        HashMap<String, LabelBean> map = new HashMap<>();
                        map.put(Constants.EDIT_LABEL, mLabel);
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
            } else {
                ChatHttpMethods.getInstance().createLabel(name, users, new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.REDRESH_LABELS, "");
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
            }
        } else if (id == R.id.tvDeleteLabel) {
            ChatHttpMethods.getInstance().deleteLabel(mLabel.getTagId(), new HttpObserver(new SubscriberOnNextListener() {
                @Override
                public void onNext(Object o, String msg) {
                    HashMap<String, Long> map = new HashMap<>();
                    map.put(Constants.DELETE_LABEL, mLabel.getTagId());
                    EventBus.getDefault().post(map);
                    finish();
                }
            }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }
}
