package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.LabelAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
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

public class LabelFragment extends ChatBaseFragment {

    private LabelAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_label;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        TextView tvLeft = view.findViewById(R.id.tvLeft);
        tvLeft.setVisibility(View.VISIBLE);
        tvLeft.setOnClickListener(this);
        tvLeft.setText(getString(R.string.chat_create));
        setViewsOnClickListener(R.id.btnAddLabel);
        setText(R.id.tvTitle, R.string.chat_all_labels);
        getLabels();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        getAdapter().setNewData(DataManager.getInstance().getLabels());
    }

    private LabelAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new LabelAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, getAdapter().getItem(position));
                    gotoPager(LabelFriendsFragment.class, bundle);
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
        if (id == R.id.btnAddLabel || id == R.id.tvLeft) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_LABEL);
            gotoPager(SelectFriendFragment.class, bundle);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.REDRESH_LABELS)) {
                getLabels();
            } else if (map.containsKey(Constants.DELETE_LABEL)) {
                long tagId = (long) map.get(Constants.DELETE_LABEL);
                getAdapter().deleteLabel(tagId);
                DataManager.getInstance().saveLabels(getAdapter().getData());
                resetUI();
            } else if (map.containsKey(Constants.EDIT_LABEL)) {
                LabelBean label = (LabelBean) map.get(Constants.EDIT_LABEL);
                getAdapter().editLabel(label);
                DataManager.getInstance().saveLabels(getAdapter().getData());
            }
        }
    }


    private void getLabels() {
        ChatHttpMethods.getInstance().getLabels(new HttpObserver(new SubscriberOnNextListener<ArrayList<LabelBean>>() {
            @Override
            public void onNext(ArrayList<LabelBean> list, String msg) {
                DataManager.getInstance().saveLabels(list);
                if (getView() == null) {
                    return;
                }
                getAdapter().setNewInstance(list);
                resetUI();
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    private void resetUI() {
        if (getAdapter().getItemCount() == 0) {
            setViewVisible(R.id.llEmpty);
            setViewGone(R.id.recyclerView);
        } else {
            setViewGone(R.id.llEmpty);
            setViewVisible(R.id.recyclerView);
        }
    }
}
