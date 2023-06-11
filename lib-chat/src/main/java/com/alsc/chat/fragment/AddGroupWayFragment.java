package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AddGroupWayFragment extends ChatBaseFragment {

    private GroupBean mGroup;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_add_group_way;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_group_manager);
        setViewsOnClickListener(R.id.llOnlyOwer, R.id.llOnlyMember, R.id.tvPayEnterGroup);
        resetJoinTypeUI();
    }

    private void resetJoinTypeUI() {
        setImage(R.id.ivOnlyOwer, mGroup.getJoinType() == 1 ? R.drawable.chat_switch_off : R.drawable.chat_switch_on);
        setImage(R.id.ivOnlyMember, mGroup.getJoinType() == 0 ? R.drawable.chat_switch_off : R.drawable.chat_switch_on);
    }


    @Override
    public void updateUIText() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(GroupBean bean) {
        if (getView() != null) {
            mGroup = bean;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llOnlyOwer || id == R.id.llOnlyMember) {
            updateJoinType(mGroup.getJoinType() == 1 ? 0 : 1);
        } else if (id == R.id.tvPayEnterGroup) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            gotoPager(PayEnterGroupFragment.class, bundle);
        }
    }

    private void updateJoinType(final int joinType) {
        ChatHttpMethods.getInstance().updateEnterGroupType(String.valueOf(mGroup.getGroupId()), String.valueOf(joinType),
                new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                    @Override
                    public void onNext(GroupBean bean, String msg) {
                        sendRefreshGroupSystemMsg();
                        if (getView() == null) {
                            return;
                        }
                        mGroup.setJoinType(joinType);
                        EventBus.getDefault().post(mGroup);
                        resetJoinTypeUI();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void sendRefreshGroupSystemMsg() {
        GroupMessageBean messageBean = GroupMessageBean.getGroupSystemMsg(DataManager.getInstance().getUserId(), mGroup.getGroupId(),
                Constants.REFRESH_GROUP_INFO, mGroup.getGroupId());
        WebSocketHandler.getDefault().send(messageBean.toJson());
    }

}
