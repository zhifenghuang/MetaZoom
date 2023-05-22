package com.alsc.chat.fragment;


import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alsc.chat.R;
import com.alsc.chat.dialog.InputPasswordDialog;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.utils.Constants;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class SendGroupRedPackageFragment extends ChatBaseFragment {

    private int mType;  //1群普通红包群 2手气红包 3私人转转 4私人红包
    private GroupBean mGroup;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_send_group_red_package;
    }

    @Override
    protected void onViewCreated(View view) {
        mType = 2;
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_send_group_rp);
        setViewVisible(R.id.tvLeft);
        setText(R.id.tvLeft, R.string.chat_red_package_record);
        setViewsOnClickListener(R.id.tvLeft, R.id.tvSend, R.id.llRandomRp, R.id.llCommonRp);

        etListener(view.findViewById(R.id.etQuantity));
        etListener(view.findViewById(R.id.etNum));
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
                    }
                }
//                String value = text;
//                if (mType == 1) {
//                    String numText = getTextById(R.id.etNum);
//                    if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(numText)) {
//                        int num = Integer.parseInt(numText);
//                        float dValue = Float.parseFloat(value);
//                        if (num >= 0) {
//                            value = String.format("%.2f", dValue * num + 0.00001f);
//                        }
//                    } else {
//                        value = "0.00";
//                    }
//                } else {
//                    if (TextUtils.isEmpty(value)) {
//                        value = "0.00";
//                    } else {
//                        value = String.format("%.2f", Float.parseFloat(value));
//                    }
//                }
//                setText(R.id.tvQuantity, value);
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
        if (id == R.id.tvLeft) {
            gotoPager(RedPackageRecordFragment.class);
        } else if (id == R.id.tvSend) {
            String value = getTextById(R.id.etQuantity);
            if (TextUtils.isEmpty(value)) {
                return;
            }

            String numText = getTextById(R.id.etNum);
            if (TextUtils.isEmpty(numText)) {
                return;
            }
            int num = Integer.parseInt(numText);
            if (num <= 0) {
                return;
            }
            float quantity = Float.parseFloat(value);
            if (quantity <= 0.00) {
                return;
            }
            if (mType == 1) {
                if (quantity > 200) {
                    showToast(R.string.chat_red_package_quantity_tip);
                    return;
                }
                value = String.format("%.2f", quantity * num + 0.00001f);
                quantity = Float.parseFloat(value);
            } else {
                if (quantity / num > 200) {
                    showToast(R.string.chat_red_package_quantity_tip);
                    return;
                }
            }
            String remark = getTextById(R.id.etRemark);
            showInputPayPasswordDialog(quantity, remark, num);
        } else if (id == R.id.llRandomRp) {
            mType = 2;
            ViewGroup ll = fv(R.id.llRandomRp);
            ((TextView) ll.getChildAt(0)).
                    setTextColor(ContextCompat.getColor(getActivity(), R.color.color_3a_54_ff));
            ll.getChildAt(1).setVisibility(View.VISIBLE);
            ll = fv(R.id.llCommonRp);
            ((TextView) ll.getChildAt(0)).
                    setTextColor(ContextCompat.getColor(getActivity(), R.color.color_00_00_00));
            ll.getChildAt(1).setVisibility(View.INVISIBLE);
            String text = getTextById(R.id.etQuantity);
            String numText = getTextById(R.id.etNum);
            if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(numText)) {
                int num = Integer.parseInt(numText);
                String value = String.format("%.2f", Float.parseFloat(text));
                float dValue = Float.parseFloat(value);
                if (num >= 0) {
                    value = String.format("%.2f", dValue * num + 0.00001f);
                }
                setText(R.id.etQuantity, value);
            }
        } else if (id == R.id.llCommonRp) {
            mType = 1;
            ViewGroup ll = fv(R.id.llCommonRp);
            ((TextView) ll.getChildAt(0)).
                    setTextColor(ContextCompat.getColor(getActivity(), R.color.color_3a_54_ff));
            ll.getChildAt(1).setVisibility(View.VISIBLE);
            ll = fv(R.id.llRandomRp);
            ((TextView) ll.getChildAt(0)).
                    setTextColor(ContextCompat.getColor(getActivity(), R.color.color_00_00_00));
            ll.getChildAt(1).setVisibility(View.INVISIBLE);
            String text = getTextById(R.id.etQuantity);
            String numText = getTextById(R.id.etNum);
            if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(numText)) {
                int num = Integer.parseInt(numText);
                String value = String.format("%.2f", Float.parseFloat(text));
                float dValue = Float.parseFloat(value);
                if (num >= 0) {
                    value = String.format("%.2f", dValue / num + 0.00001f);
                }
                setText(R.id.etQuantity, value);
            }
        }
    }

    private void showInputPayPasswordDialog(float amount, final String remark, final int num) {
        InputPasswordDialog dialog = new InputPasswordDialog(getActivity(), amount, null);
        dialog.setOnInputPasswordListener(new InputPasswordDialog.OnInputPasswordListener() {
            @Override
            public void afterCheckPassword() {
                if (getView() == null) {
                    return;
                }
                sendRedPackage(amount, remark, num);
            }
        });
        dialog.show();
    }

    private void sendRedPackage(float amount, String remark, int num) {
        ChatHttpMethods.getInstance().envelopeSend(amount, num, remark, mType,
                -1, mGroup.getGroupId(),
                new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
                    @Override
                    public void onNext(EnvelopeBean bean, String msg) {
                        if (bean == null) {
                            return;
                        }
                        String content = new Gson().toJson(bean);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.RED_PACKAGE, content);
                        EventBus.getDefault().post(map);
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }

}
