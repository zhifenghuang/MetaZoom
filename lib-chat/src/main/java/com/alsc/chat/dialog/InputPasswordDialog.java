package com.alsc.chat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.utils.Utils;
import com.common.lib.activity.BaseActivity;

public class InputPasswordDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private EditText etPayPassword;

    private OnInputPasswordListener mOnInputPasswordListener;

    public InputPasswordDialog(Context context, float amount, String unit, String content) {
        this(context, R.style.LoadingDialog, amount, unit, content);
    }

    public InputPasswordDialog(Context context, float amount, String content) {
        this(context, R.style.LoadingDialog, amount, "UTG", content);
    }

    public InputPasswordDialog(Context context, int themeResId, float amount, String unit, String content) {
        super(context, themeResId);
        this.mContext = context;
        setContentView(R.layout.layout_input_pay_password_dialog);

        etPayPassword = findViewById(R.id.etPayPassword);
        if (amount < 0.0) {
            findViewById(R.id.tvValue).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.tvValue)).setText(String.format("%.2f", amount + 0.00001f));
        }

        if (TextUtils.isEmpty(content)) {
            findViewById(R.id.tvContent).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.tvContent)).setText(content);
        }

        ((TextView) findViewById(R.id.tvUnit)).setText(unit);

        Window view = getWindow();
        WindowManager.LayoutParams lp = view.getAttributes();
        lp.width = ((BaseActivity) context).getDisplayMetrics().widthPixels - Utils.dip2px(context, 52); // 设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        view.setGravity(Gravity.BOTTOM);

        findViewById(R.id.ivClose).setOnClickListener(this);
        findViewById(R.id.tvOk).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        hideSoftInput();
        int id = v.getId();
        if (id == R.id.tvOk) {
            next();
        } else if (id == R.id.ivClose) {
            dismiss();
        }
    }

    private void next() {
        String password = etPayPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            return;
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput() {
        if (etPayPassword != null) {
            InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(etPayPassword.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void setOnInputPasswordListener(OnInputPasswordListener onInputPasswordListener) {
        mOnInputPasswordListener = onInputPasswordListener;
    }


    public interface OnInputPasswordListener {
        public void afterCheckPassword();
    }
}

