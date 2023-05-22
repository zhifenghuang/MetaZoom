package com.common.lib.utils;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class CountDownUtil {

    /**
     * 开始倒计时code
     */
    private final int MSG_WHAT_START = 10_010;
    /**
     * 弱引用
     */
    private WeakReference<TextView> mWeakReference;
    private WeakReference<EditText> mEditTextWeakReference;
    /**
     * 倒计时时间
     */
    private long mCountDownMillis = 60_000;
    /**
     * 提示文字
     */
    private String mHintText = "重新发送";
    /**
     * 提示文字后方
     */
    private String mHintAfterText = "s";
    /**
     * 剩余倒计时时间
     */
    private long mLastMillis;

    /**
     * 间隔时间差(两次发送handler)
     */
    private long mIntervalMillis = 1_000;

    /**
     * 可用状态下字体颜色Id
     */
    private int usableColorId = android.R.color.holo_blue_light;
    /**
     * 不可用状态下字体颜色Id
     */
    private int unusableColorId = android.R.color.darker_gray;
    /**
     * 可用状态下字体背景颜色Id
     */
    private int usableBackgroundColorId = 0;
//    private int usableBackgroundColorId = R.drawable.corner_595757_26;
    /**
     * 不可用状态下字体背景颜色Id
     */
    private int unusableBackgroundColorId = 0;
    //    private int unusableBackgroundColorId = R.drawable.corner_dbdcdc_26;
    private OnTimeFinishListener mOnTimeFinishListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_WHAT_START:
                    if (mLastMillis > 0) {
                        setUsable(false);
                        mLastMillis -= mIntervalMillis;
                        if (mWeakReference.get() != null) {
                            mHandler.sendEmptyMessageDelayed(MSG_WHAT_START, mIntervalMillis);
                        }
                    } else {
                        setUsable(true);
                        if (mOnTimeFinishListener != null) {
                            mOnTimeFinishListener.onTimeFinish();
                        }
                    }
                    break;
            }
        }
    };

    public CountDownUtil(TextView textView) {
        mWeakReference = new WeakReference<>(textView);
    }

    public CountDownUtil(TextView textView, EditText editText) {
        mWeakReference = new WeakReference<>(textView);
        mEditTextWeakReference = new WeakReference<>(editText);
    }

    public CountDownUtil(TextView textView, long countDownMillis) {
        mWeakReference = new WeakReference<>(textView);
        this.mCountDownMillis = countDownMillis;
    }

    public CountDownUtil setCountDownMillis(long countDownMillis) {
        this.mCountDownMillis = countDownMillis;
        return this;
    }


    /**
     * 设置是否可用
     *
     * @param usable
     */
    private void setUsable(boolean usable) {
        TextView mTextView = mWeakReference.get();
        if (mTextView != null) {
            if (usable) {
                //可用
                mTextView.setEnabled(true);
                mTextView.setTextColor(mTextView.getResources().getColor(usableColorId));
                if (usableBackgroundColorId != 0) {
                    mTextView.setBackgroundResource(usableBackgroundColorId);
                }
                mTextView.setText(mHintText);
            } else {
                //不可用
                mTextView.setEnabled(false);
                mTextView.setTextColor(mTextView.getResources().getColor(unusableColorId));
                if (unusableBackgroundColorId != 0) {
                    mTextView.setBackgroundResource(unusableBackgroundColorId);
                }
                String content;
                if ("距开抢仅剩：".equals(mHintText)) {
                    content = getFormatTime(mLastMillis);
                } else {
                    content = mLastMillis / 1000 + mHintAfterText;
                }
                mTextView.setText(content);
            }
        }
        if (mEditTextWeakReference != null) {
            EditText editText = mEditTextWeakReference.get();
            if (editText != null) {
                if (usable) {
                    editText.setEnabled(true);
                } else {
                    editText.setEnabled(false);
                }
            }
        }

    }

    /**
     * 设置倒计时颜色
     *
     * @param usableColorId   可用状态下的颜色
     * @param unusableColorId 不可用状态下的颜色
     */
    public CountDownUtil setCountDownColor(@ColorRes int usableColorId, @ColorRes int unusableColorId) {
        this.usableColorId = usableColorId;
        this.unusableColorId = unusableColorId;
        return this;
    }

    /**
     * 设置倒计时背景颜色
     *
     * @param usableColorId   可用状态下的颜色
     * @param unusableColorId 不可用状态下的颜色
     */
    public CountDownUtil setCountDownBackgroundColor(int usableColorId, int unusableColorId) {
        this.usableBackgroundColorId = usableColorId;
        this.unusableBackgroundColorId = unusableColorId;
        return this;
    }

    /**
     * 设置倒计时提示文字
     *
     * @param hint 文案
     * @return
     */
    public CountDownUtil setCountDownHint(String hint) {
        this.mHintText = hint;
        return this;
    }

    /**
     * 设置后方倒计时提示文字
     *
     * @param hint 文案
     * @return
     */
    public CountDownUtil setHintAfterText(String hint) {
        this.mHintAfterText = hint;
        return this;
    }

    /**
     * 开始倒计时
     */
    public CountDownUtil start() {
        mLastMillis = mCountDownMillis;
        mHandler.sendEmptyMessage(MSG_WHAT_START);
        return this;
    }

    public CountDownUtil setOnClickListener(@Nullable final View.OnClickListener onClickListener) {
        TextView mTextView = mWeakReference.get();
        if (mTextView != null)
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.removeMessages(MSG_WHAT_START);
                    start();
                    onClickListener.onClick(v);
                }
            });
        return this;
    }

    public CountDownUtil setOnTimeListener(@Nullable final OnTimeFinishListener timeFinishListener) {
        mOnTimeFinishListener = timeFinishListener;
        return this;
    }

    /**
     * 重置停止倒计时
     */
    public CountDownUtil reset() {
        mLastMillis = 0;
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_WHAT_START);
        }
        return this;
    }


    public String getFormatTime(long diff) {
        int hours = (int) (diff / (1000 * 60 * 60));
        int minutes = (int) ((diff - hours * (1000 * 60 * 60)) / (1000 * 60));
        int second = (int) ((diff - hours * (1000 * 60 * 60) - minutes * (1000 * 60)) / 1000);

        return "" + hours + "小时" + (minutes < 10 ? "0" + minutes : minutes) + "分" + (second < 10 ? "0" + second : second) + "秒";
    }

    public interface OnTimeFinishListener {
        void onTimeFinish();
    }
}
