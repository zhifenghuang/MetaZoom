package com.alsc.chat.fragment;

import android.os.Bundle;
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
import com.common.lib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class AddGroupFragment extends ChatBaseFragment {

    private LabelUserAdapter mAdapter;

    private ArrayList<UserBean> mGroupUsers, mShowGroupUsers;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_add_group;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(R.id.topView);
        setText(R.id.tvTitle, R.string.chat_new_group);
        setViewsOnClickListener(R.id.ivNext);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        mGroupUsers = new ArrayList<>();
        mShowGroupUsers = new ArrayList<>();
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
            mGroupUsers.addAll((ArrayList<UserBean>) object);
        }
        LogUtil.LogE("size: " + mGroupUsers.size());
        mShowGroupUsers.clear();
        mShowGroupUsers.addAll(mGroupUsers);
        getAdapter().setUsers(mGroupUsers);
        getAdapter().setNewInstance(mShowGroupUsers);
        getAdapter().notifyDataSetChanged();
        DataManager.getInstance().setObject(null);
        setText(R.id.tvNum, getString(R.string.chat_xxx_members, String.valueOf(getAdapter().getItemCount())));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivNext) {
            String groupName = getTextById(R.id.etGroupName);
            if (TextUtils.isEmpty(groupName)) {
                return;
            }
            final List<UserBean> list = getAdapter().getData();
            if (list.isEmpty()) {
                return;
            }
            ArrayList<Long> userIds = new ArrayList<>();
            for (UserBean bean : list) {
                if (bean != null && bean.getContactId() > 0L) {
                    userIds.add(bean.getContactId());
                }
            }
            userIds.add(DataManager.getInstance().getUser().getUserId());
            ChatHttpMethods.getInstance().createGroup(groupName, userIds, new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                @Override
                public void onNext(GroupBean group, String msg) {
                    sendInviteToGroupMsg(group, DataManager.getInstance().getUser(), list);
                    if (getView() == null) {
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, group);
                    gotoPager(GroupChatFragment.class, bundle);
                    ((ChatBaseActivity) getActivity()).finishAllOtherActivity();
                }
            }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }
}
