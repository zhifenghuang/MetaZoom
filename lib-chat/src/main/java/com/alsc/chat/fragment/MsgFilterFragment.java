package com.alsc.chat.fragment;


import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.R;
import com.alsc.chat.adapter.MsgFilterAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.utils.Constants;
import com.common.lib.bean.FilterMsgBean;
import com.common.lib.bean.GroupBean;
import com.common.lib.bean.GroupMessageBean;
import com.common.lib.manager.DataManager;
import com.zhangke.websocket.WebSocketHandler;

import java.util.ArrayList;

public class MsgFilterFragment extends ChatBaseFragment {

    private GroupBean mGroup;
    private MsgFilterAdapter mAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg_filter;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);

        ImageView ivAddMsgFilter = view.findViewById(R.id.ivAddMsgFilter);
        ivAddMsgFilter.setAlpha(0.5f);
        ivAddMsgFilter.setEnabled(false);
        ivAddMsgFilter.setVisibility(View.VISIBLE);
        ivAddMsgFilter.setOnClickListener(this);
        setText(R.id.tvTitle, R.string.chat_msg_filter);
        setViewGone(R.id.tvLetterNum);
        EditText etFilter = view.findViewById(R.id.etFilter);
        etFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getView() == null) {
                    return;
                }
                int size = s.toString().trim().length();
                if (size > 0) {
                    ivAddMsgFilter.setAlpha(1.0f);
                    ivAddMsgFilter.setEnabled(true);
                } else {
                    ivAddMsgFilter.setAlpha(0.5f);
                    ivAddMsgFilter.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        getFilterMsg();
    }

    private MsgFilterAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MsgFilterAdapter(getActivity(), mGroup);
        }
        return mAdapter;
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivAddMsgFilter) {
            String content = getTextById(R.id.etFilter).trim();
            if (TextUtils.isEmpty(content)) {
                return;
            }
            ChatHttpMethods.getInstance().groupBlockCreate(String.valueOf(mGroup.getGroupId()), content,
                    new HttpObserver(new SubscriberOnNextListener<FilterMsgBean>() {
                        @Override
                        public void onNext(FilterMsgBean bean, String msg) {
                            if (getView() == null) {
                                return;
                            }
                            getAdapter().addData(bean);
                            setText(R.id.etFilter, "");
                            DataManager.getInstance().setGroupFilterMsg(mGroup.getGroupId(), getAdapter().getData());
                            GroupMessageBean messageBean = GroupMessageBean.getGroupSystemMsg(DataManager.getInstance().getUserId(), mGroup.getGroupId(),
                                    Constants.REFRESH_FORBID_LETTER, mGroup.getGroupId());
                            WebSocketHandler.getDefault().send(messageBean.toJson());
                        }
                    }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }

    private void getFilterMsg() {
        ChatHttpMethods.getInstance().groupBlockList(String.valueOf(mGroup.getGroupId()),
                new HttpObserver(new SubscriberOnNextListener<ArrayList<FilterMsgBean>>() {
                    @Override
                    public void onNext(ArrayList<FilterMsgBean> list, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        getAdapter().setNewData(list);
                        DataManager.getInstance().setGroupFilterMsg(mGroup.getGroupId(), list);
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
