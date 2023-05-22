package com.alsc.chat.fragment;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.RedPackageRecordAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.common.lib.bean.EnvelopeRecordBean;

import java.util.ArrayList;

public class RedPackageRecordFragment extends ChatBaseFragment {

    private RedPackageRecordAdapter mAdapter;

    private int mCurrentType;  //1我收到的红包2我发出的红包

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_red_package_record;
    }

    @Override
    protected void onViewCreated(View view) {
        mCurrentType = 1;
        setTopStatusBarStyle(R.id.llTop);
        setText(R.id.tvTitle, R.string.chat_my_receive_red_package);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        getRedPackageRecord();
        ArrayList<RedPackageRecordItem> list = new ArrayList<>();
        list.add(new RedPackageRecordItem());
        getAdapter().setNewData(list);
        setViewsOnClickListener(R.id.ivMore);
    }


    private RedPackageRecordAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new RedPackageRecordAdapter(getActivity());
            mAdapter.setEnvelope(DataManager.getInstance().getUser(), mCurrentType);
        }
        return mAdapter;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivMore) {
            showSelectrecordDialog();
        }
    }

    private void showSelectrecordDialog() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_red_package_record_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                dialogFragment.setDialogViewsOnClickListener(view,
                        R.id.tvReceiveRecord, R.id.tvSendRecord, R.id.tvCancel);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.tvReceiveRecord) {
                    mCurrentType = 1;
                    getRedPackageRecord();
                } else if (viewId == R.id.tvSendRecord) {
                    mCurrentType = 2;
                    getRedPackageRecord();
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }


    private void getRedPackageRecord() {
        ChatHttpMethods.getInstance().envelopeList(mCurrentType, new HttpObserver(new SubscriberOnNextListener<EnvelopeRecordBean>() {
            @Override
            public void onNext(EnvelopeRecordBean bean, String msg) {
                if (getView() == null || bean == null) {
                    return;
                }
                setText(R.id.tvTitle, mCurrentType == 1 ? R.string.chat_my_receive_red_package : R.string.chat_my_send_red_package);
                getAdapter().setRedPackageData(bean, mCurrentType);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    public static class RedPackageRecordItem implements MultiItemEntity {

        private int itemType;
        private EnvelopeRecordBean.Session session;


        public void setItemType(int itemType) {
            this.itemType = itemType;
        }

        public EnvelopeRecordBean.Session getSession() {
            return session;
        }

        public void setSession(EnvelopeRecordBean.Session session) {
            this.session = session;
        }

        @Override
        public int getItemType() {
            return itemType;
        }

    }
}
