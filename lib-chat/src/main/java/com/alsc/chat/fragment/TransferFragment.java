package com.alsc.chat.fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.alsc.chat.R;
import com.alsc.chat.dialog.InputPasswordDialog;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class TransferFragment extends ChatBaseFragment {

    private UserBean mUserInfo;
    private TransferFeeBean mFee;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_transfer;
    }

    @Override
    protected void onViewCreated(View view) {
        mUserInfo = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_transfer);
        setText(R.id.tvName, mUserInfo.getNickName());
        Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, mUserInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));
        setViewsOnClickListener(R.id.tvOk, R.id.ivRefreshMoney);
        getBalance();
        getTransferFee();

        mFee = DataManager.getInstance().getTransferFeeBean();
        setText(R.id.tvFee, getString(R.string.chat_transfer_fee, "0.00"));
        ((EditText) view.findViewById(R.id.etValue)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getView() == null) {
                    return;
                }
                EditText etValue = fv(R.id.etValue);
                String text = etValue.getText().toString();
                if (text.length() > 1 && text.startsWith("0") && text.charAt(1) != '.') {
                    text = text.substring(1);
                    etValue.setText(text);
                    etValue.setSelection(text.length());
                    return;
                }
                if (text.contains(".")) {
                    String[] strs = text.split("\\.");
                    if (strs.length > 1 && strs[1] != null && strs[1].length() > 2) {
                        text = text.substring(0, text.length() - 1);
                        etValue.setText(text);
                        etValue.setSelection(text.length());
                    }
                }
                if (TextUtils.isEmpty(text)) {
                    setText(R.id.tvFee, getString(R.string.chat_transfer_fee, "0.00"));
                } else {
                    if (mFee == null) {
                        return;
                    }
                    String fee;
                    if (mFee.getType() == 1) {
                        fee = String.format("%.4f", Float.parseFloat(text) * mFee.getFee() + 0.000001f);
                    } else {
                        fee = String.format("%.4f", mFee.getFee());
                    }
                    setText(R.id.tvFee, getString(R.string.chat_transfer_fee, String.valueOf(fee)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRefreshMoney) {
            getBalance();
        } else if (id == R.id.tvOk) {
            String value = getTextById(R.id.etValue);
            if (TextUtils.isEmpty(value)) {
                return;
            }
            float amount = Float.parseFloat(value);
            if (amount <= 0) {
                return;
            }
            showInputPayPasswordDialog(amount);
        }
    }

    private void showInputPayPasswordDialog(final float amount) {
        InputPasswordDialog dialog = new InputPasswordDialog(getActivity(), amount, getString(R.string.chat_are_you_sure_transfer_to_xxxx, mUserInfo.getNickName()));
        dialog.setOnInputPasswordListener(new InputPasswordDialog.OnInputPasswordListener() {
            @Override
            public void afterCheckPassword() {
                if (getView() == null) {
                    return;
                }
                transfer(amount);
            }
        });
        dialog.show();
    }


    private void transfer(float amount) {
        ChatHttpMethods.getInstance().envelopeSend(amount, 1, "", 3,
                mUserInfo.getContactId(), -1,
                new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
                    @Override
                    public void onNext(EnvelopeBean bean, String msg) {
                        if (bean == null) {
                            return;
                        }
                        String content = new Gson().toJson(bean);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.TRANSFER, content);
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void getBalance() {
//        HttpMethods.getInstance().getBalance(DataManager.getInstance().getToken(), new HttpObserver(new SubscriberOnNextListener<HashMap<String, Double>>() {
//            @Override
//            public void onNext(HashMap<String, Double> map, String msg) {
//                if (getView() == null || !map.containsKey("a13")) {
//                    return;
//                }
//                setText(R.id.tvRestMoney, getString(R.string.chat_xxx_alsc, String.valueOf(map.get("a13"))));
//            }
//        }, getActivity(), (BaseActivity) getActivity()));
    }

    private void getTransferFee() {
        ChatHttpMethods.getInstance().transferFee(new HttpObserver(new SubscriberOnNextListener<TransferFeeBean>() {
            @Override
            public void onNext(TransferFeeBean bean, String msg) {
                if (getView() == null || bean == null) {
                    return;
                }
                DataManager.getInstance().saveTransferFeeBean(bean);
                mFee = bean;
                String text = getTextById(R.id.etValue);
                if (TextUtils.isEmpty(text)) {
                    setText(R.id.tvFee, getString(R.string.chat_transfer_fee, "0.00"));
                } else {
                    String fee;
                    if (mFee.getType() == 1) {
                        fee = String.format("%.4f", Float.parseFloat(text) * mFee.getFee() + 0.000001f);
                    } else {
                        fee = String.format("%.4f", mFee.getFee());
                    }
                    setText(R.id.tvFee, getString(R.string.chat_transfer_fee, String.valueOf(fee)));
                }
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
