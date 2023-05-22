package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alsc.chat.R;
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
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;

public class PayEnterGroupFragment extends ChatBaseFragment {

    private GroupBean mGroup;

    private int mPayInState;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pay_enter_group;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_pay_enter_group);
        setViewsOnClickListener(R.id.llState, R.id.llDetail);

        TextView tvLeft= view.findViewById(R.id.tvLeft);
        tvLeft.setText(getString(R.string.chat_save));
        tvLeft.setVisibility(View.VISIBLE);
        tvLeft.setOnClickListener(this);

        mPayInState = mGroup.getPayinState();
        if (mPayInState == 0) {
            setImage(R.id.ivState, R.drawable.icon_switch_off);
            setViewGone(R.id.llSetAmount);
            setViewVisible(R.id.paddingView);
        } else {
            setImage(R.id.ivState, R.drawable.icon_switch_on);
            setViewVisible(R.id.llSetAmount);
            setViewGone(R.id.paddingView);
            if (mGroup.getPayAmount() > 0.0) {
                setText(R.id.etSetAmount, String.valueOf(mGroup.getPayAmount()));
            }
        }
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llState) {
            mPayInState = (mPayInState == 1) ? 0 : 1;
            if (mPayInState == 0) {
                setImage(R.id.ivState, R.drawable.icon_switch_off);
                setViewGone(R.id.llSetAmount);
                setViewVisible(R.id.paddingView);
            } else {
                setImage(R.id.ivState, R.drawable.icon_switch_on);
                setViewVisible(R.id.llSetAmount);
                setViewGone(R.id.paddingView);
                if (mGroup.getPayAmount() > 0.0) {
                    setText(R.id.etSetAmount, String.valueOf(mGroup.getPayAmount()));
                }
            }
        } else if (id == R.id.llDetail) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            gotoPager(PayEnterGroupDetailFragment.class, bundle);
        } else if (id == R.id.tvLeft) {
            String payAmount = getTextById(R.id.etSetAmount);
            if (mPayInState == 1 && TextUtils.isEmpty(payAmount)) {
                return;
            }
            float dAmount;
            if (mPayInState == 1) {
                dAmount = Float.parseFloat(payAmount);
                if (dAmount <= 0.00) {
                    showToast(R.string.chat_pay_amount_must_above_zero);
                    return;
                }
            } else {
                dAmount = 0.00f;
            }
            ChatHttpMethods.getInstance().updateEnterGroupPay(String.valueOf(mGroup.getGroupId()), String.valueOf(mPayInState), payAmount,
                    new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                        @Override
                        public void onNext(GroupBean bean, String msg) {

                            GroupMessageBean messageBean = GroupMessageBean.getGroupSystemMsg(DataManager.getInstance().getUserId(), mGroup.getGroupId(),
                                    Constants.REFRESH_GROUP_INFO, mGroup.getGroupId());
                            WebSocketHandler.getDefault().send(messageBean.toJson());

                            if (getView() == null) {
                                return;
                            }
                            mGroup.setPayinState(mPayInState);
                            mGroup.setPayAmount(dAmount);
                            EventBus.getDefault().post(mGroup);
                            finish();
                        }
                    }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }

}
