package com.alsc.chat.fragment;

import android.view.View;
import android.widget.TextView;

import com.alsc.chat.R;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;

public class ChatSettingFragment extends ChatBaseFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_setting;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_chat_setting);
        setViewsOnClickListener(R.id.tvNewMsgReminder, R.id.llNotInteruptMode,
                R.id.tvClearAllMsg, R.id.tvPrivacySetting);
    }

    @Override
    public void updateUIText() {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvNewMsgReminder) {
            gotoPager(NewMsgReminderFragment.class);
        } else if (id == R.id.llNotInteruptMode) {
            gotoPager(NotInteruptModeFragment.class);
        } else if (id == R.id.tvClearAllMsg) {
            showDeleteChatRecord();
        } else if (id == R.id.tvPrivacySetting) {
            gotoPager(PrivacyFragment.class);
        }
    }

    private void showDeleteChatRecord() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_are_you_sure_delete_all_chat_record));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    DatabaseOperate.getInstance().deleteAllChatRecord(DataManager.getInstance().getUser().getUserId());
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }
}
