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
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class NewMemberRedPackageFragment extends ChatBaseFragment {

    private GroupBean mGroup;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_add_new_member_red_package;
    }

    @Override
    protected void onViewCreated(View view) {
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_add_new_member_red_package);
        etListener(view.findViewById(R.id.etQuantity));
        etListener(view.findViewById(R.id.etNum));
        setViewsOnClickListener(R.id.tvAddNewMemberRedPackage);
    }

    private void etListener(EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getView() == null) {
                    return;
                }
                EditText etQuantity = fv(R.id.etQuantity);
                String text = etQuantity.getText().toString();
                if (text.length() > 1 && text.startsWith("0") && text.charAt(1) != '.') {
                    text = text.substring(1);
                    etQuantity.setText(text);
                    etQuantity.setSelection(text.length());
                    return;
                }
                if (text.contains(".")) {
                    String[] strs = text.split("\\.");
                    if (strs.length > 1 && strs[1] != null && strs[1].length() > 2) {
                        text = text.substring(0, text.length() - 1);
                        etQuantity.setText(text);
                        etQuantity.setSelection(text.length());
                        return;
                    }
                }
                String value = text;
                String numText = getTextById(R.id.etNum);
                if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(numText)) {
                    int num = Integer.parseInt(numText);
                    float dValue = Float.parseFloat(value);
                    if (num >= 0) {
                        value = String.format("%.2f", dValue * num + 0.00001f);
                    }
                } else {
                    if (TextUtils.isEmpty(value)) {
                        value = "0.00";
                    } else {
                        value = String.format("%.2f", Float.parseFloat(value));
                    }
                }
                setText(R.id.tvQuantity, value);
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
        if (v.getId() == R.id.tvAddNewMemberRedPackage) {
            String value = getTextById(R.id.etQuantity);
            if (TextUtils.isEmpty(value)) {
                return;
            }
            int num = 1;
            String numText = getTextById(R.id.etNum);
            if (TextUtils.isEmpty(numText)) {
                return;
            }
            num = Integer.parseInt(numText);
            if (num <= 0) {
                return;
            }
            float quantity = Float.parseFloat(value);
            if (quantity <= 0.00) {
                return;
            }
            quantity *= num;
            showInputPayPasswordDialog(quantity, num);
        }
    }


    private void showInputPayPasswordDialog(final float amount, final int num) {
        InputPasswordDialog dialog = new InputPasswordDialog(getActivity(), amount,  null);
        dialog.setOnInputPasswordListener(new InputPasswordDialog.OnInputPasswordListener() {
            @Override
            public void afterCheckPassword() {
                if (getView() == null) {
                    return;
                }
                sendRedPackage(amount, num);
            }
        });
        dialog.show();
    }

    private void sendRedPackage(float amount, int num) {
        ChatHttpMethods.getInstance().envelopeSend(amount, num, "", 5, -1, mGroup.getGroupId(),
                new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
                    @Override
                    public void onNext(EnvelopeBean bean, String msg) {
                        if (bean == null) {
                            return;
                        }
                        String content = new Gson().toJson(bean);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.NEW_MEMBER_RED_PACKAGE, content);
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
