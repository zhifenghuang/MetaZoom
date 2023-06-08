package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.NewMemberRedPackageAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class NewMemberRedPackageSettingFragment extends ChatBaseFragment {

    private GroupBean mGroup;
    private NewMemberRedPackageAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_member_red_package_setting;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_new_member_red_package_setting);
        TextView tvLeft = view.findViewById(R.id.tvLeft);
        tvLeft.setText(getString(R.string.chat_add));
        tvLeft.setVisibility(View.VISIBLE);
        tvLeft.setOnClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        newcomerList();
    }

    private NewMemberRedPackageAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new NewMemberRedPackageAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (getAdapter().getItem(position).getStatus() != 3) {
                        unLockNewEnvelopeDialog(position);
                    }
                }
            });
        }
        return mAdapter;
    }

    protected void unLockNewEnvelopeDialog(final int position) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.chat_layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                view.findViewById(R.id.tv1).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_are_you_sure_delete_new_member_red_package));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    ChatHttpMethods.getInstance().unLockNewEnvelope(getAdapter().getItem(position).getEnvelopeId(), new HttpObserver(new SubscriberOnNextListener() {
                        @Override
                        public void onNext(Object o, String msg) {
                            EnvelopeBean bean = getAdapter().getItem(position);
                            bean.setStatus(3);
                            getAdapter().notifyDataSetChanged();
                        }
                    }, getActivity(), (ChatBaseActivity) getActivity()));
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvLeft) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            gotoPager(NewMemberRedPackageFragment.class, bundle);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (map.containsKey(Constants.NEW_MEMBER_RED_PACKAGE)) {
            String content = (String) map.get(Constants.NEW_MEMBER_RED_PACKAGE);
            EnvelopeBean bean = new Gson().fromJson(content, EnvelopeBean.class);
            if (bean != null) {
                setViewVisible(R.id.recyclerView);
                setViewGone(R.id.llEmpty);
                getAdapter().addData(bean);
            }
        }
    }

    private void newcomerList() {
        ChatHttpMethods.getInstance().newcomerList(String.valueOf(mGroup.getGroupId()), 5,
                new HttpObserver(new SubscriberOnNextListener<ArrayList<EnvelopeBean>>() {
                    @Override
                    public void onNext(ArrayList<EnvelopeBean> list, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        getAdapter().setNewData(list);
                        if (getAdapter().getItemCount() > 0) {
                            setViewVisible(R.id.recyclerView);
                            setViewGone(R.id.llEmpty);
                        } else {
                            setViewGone(R.id.recyclerView);
                            setViewVisible(R.id.llEmpty);
                        }
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
