package com.meta.zoom.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.common.lib.activity.BaseActivity;
import com.common.lib.dialog.MyDialogFragment;
import com.meta.zoom.R;

public class InputDialog extends MyDialogFragment {


    public InputDialog(final BaseActivity activity, final OnInputListener listener) {
        this(activity, listener, null);
    }

    public InputDialog(final BaseActivity activity, final OnInputListener listener, String tips) {
        super(R.layout.layout_input_psw_dialog);
        setOnMyDialogListener(new OnMyDialogListener() {
            @Override
            public void initView(View view) {
                if (!TextUtils.isEmpty(tips)) {
                    ((TextView) view.findViewById(R.id.tv1)).setText(tips);
                }
                setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    String psw = ((EditText) getView().findViewById(R.id.etPsw)).getText().toString().trim();
                    if (TextUtils.isEmpty(psw)) {
                        activity.showToast(R.string.app_please_input_your_pay_psw);
                        return;
                    }
                    if (listener != null) {
                        listener.checkInput(psw);
                    }
                }
            }
        });
        show(activity.getSupportFragmentManager(), "MyDialogFragment");
    }

    public interface OnInputListener {
        public void checkInput(String input);
    }
}
