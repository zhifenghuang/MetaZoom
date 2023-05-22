package com.alsc.chat.fragment;


import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.RedPackageResultAdapter;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.common.lib.bean.EnvelopeBean;
import com.common.lib.manager.DataManager;

import java.util.ArrayList;

public class RedPackageResultFragment extends ChatBaseFragment {

    private RedPackageResultAdapter mAdapter;

    private EnvelopeBean mEnvelopeBean;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_red_package_result;
    }

    @Override
    protected void onViewCreated(View view) {
        mEnvelopeBean = (EnvelopeBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(R.id.llTop);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        ArrayList<RedPackageResultItem> list = new ArrayList<>();
        list.add(new RedPackageResultItem());
        ArrayList<EnvelopeBean.Session> sessions = mEnvelopeBean.getSession();
        if (sessions != null) {
            RedPackageResultItem item;
            for (EnvelopeBean.Session session : sessions) {
                item = new RedPackageResultItem();
                item.setItemType(1);
                item.setSession(session);
                list.add(item);
            }
        }
        getAdapter().setNewData(list);
        setViewsOnClickListener(R.id.tvRedPackageRecord);
    }

    private RedPackageResultAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new RedPackageResultAdapter(getActivity());
            mAdapter.setEnvelope(DataManager.getInstance().getUser(), mEnvelopeBean);
        }
        return mAdapter;
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvRedPackageRecord) {
            gotoPager(RedPackageRecordFragment.class);
        }
    }

    public static class RedPackageResultItem implements MultiItemEntity {

        private int itemType;

        private EnvelopeBean.Session session;

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }

        @Override
        public int getItemType() {
            return itemType;
        }

        public EnvelopeBean.Session getSession() {
            return session;
        }

        public void setSession(EnvelopeBean.Session session) {
            this.session = session;
        }
    }

}
