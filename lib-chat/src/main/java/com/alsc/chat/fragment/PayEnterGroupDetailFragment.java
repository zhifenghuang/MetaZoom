package com.alsc.chat.fragment;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.PayInGroupAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.utils.Constants;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.AppUpgradeDialog;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;

import java.util.ArrayList;

public class PayEnterGroupDetailFragment extends ChatBaseFragment {

    private GroupBean mGroup;

    private PayInGroupAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pay_enter_group_record;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_records);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        payInGroupRecord();
    }

    private PayInGroupAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new PayInGroupAdapter(getActivity());
        }
        return mAdapter;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {

    }

    private void payInGroupRecord() {
        ChatHttpMethods.getInstance().newcomerList(String.valueOf(mGroup.getGroupId()), 6,
                new HttpObserver(new SubscriberOnNextListener<ArrayList<EnvelopeBean>>() {
                    @Override
                    public void onNext(ArrayList<EnvelopeBean> list, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        getAdapter().setNewData(list);
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }

}
