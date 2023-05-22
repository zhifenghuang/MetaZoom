package com.alsc.chat.fragment;

import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alsc.chat.R;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Utils;

public class NotInteruptModeFragment extends ChatBaseFragment {

    private ChatSettingBean mChatSetting;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_not_interupt_mode;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_not_interpt_mode);
        setViewsOnClickListener(R.id.llNotInteruptMode, R.id.llStartTime, R.id.llEndTime);
    }

    @Override
    public void updateUIText() {
        mChatSetting = DataManager.getInstance().getChatSetting();
        resetUI();
    }

    private void resetUI() {
        setImage(R.id.ivModeSwicth, mChatSetting.getNotInteruptMode() == 0 ?
                R.drawable.icon_switch_off : R.drawable.icon_switch_on);
        if (mChatSetting.getNotInteruptMode() == 0) {
            setViewGone(R.id.llStartTime, R.id.llEndTime);
        } else {
            setViewVisible(R.id.llStartTime, R.id.llEndTime);
            int startTime = mChatSetting.getStartTime();
            int endTime = mChatSetting.getEndTime();
            setText(R.id.tvStartTime, Utils.getNewText(startTime / 60) + ":" + Utils.getNewText(startTime % 60));
            setText(R.id.tvEndTime, Utils.getNewText(endTime / 60) + ":" + Utils.getNewText(endTime % 60));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llNotInteruptMode) {
            int mode = mChatSetting.getNotInteruptMode();
            mChatSetting.setNotInteruptMode(mode == 0 ? 1 : 0);
            resetUI();
            DataManager.getInstance().saveChatSetting(mChatSetting);
        } else if (id == R.id.llStartTime) {
            showSelectTimeDialog(0);
        } else if (id == R.id.llEndTime) {
            showSelectTimeDialog(1);
        }
    }

    private void showSelectTimeDialog(final int type) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_select_time_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tvTitle)).setText(
                        type == 0 ? R.string.chat_set_start_time : R.string.chat_set_end_time);
                TimePicker timePicker = view.findViewById(R.id.timePicker);
                timePicker.setIs24HourView(true);
                if (type == 0) {
                    timePicker.setCurrentHour(mChatSetting.getStartTime() / 60);
                    timePicker.setCurrentMinute(mChatSetting.getStartTime() % 60);
                } else if (type == 1) {
                    timePicker.setCurrentHour(mChatSetting.getEndTime() / 60);
                    timePicker.setCurrentMinute(mChatSetting.getEndTime() % 60);
                }
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btnCancel, R.id.btnOk);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btnOk) {
                    TimePicker timePicker = dialogFragment.getView().findViewById(R.id.timePicker);
                    int hour = timePicker.getCurrentHour();
                    int minute = timePicker.getCurrentMinute();
                    if (type == 0) {
                        mChatSetting.setStartTime(hour * 60 + minute);
                        setText(R.id.tvStartTime, Utils.getNewText(hour) + ":" + Utils.getNewText(minute));
                    } else {
                        mChatSetting.setEndTime(hour * 60 + minute);
                        setText(R.id.tvEndTime, Utils.getNewText(hour) + ":" + Utils.getNewText(minute));
                    }
                    DataManager.getInstance().saveChatSetting(mChatSetting);
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }
}
