package com.alsc.chat.fragment;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.alsc.chat.R;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;

public class NewMsgReminderFragment extends ChatBaseFragment {

    private ChatSettingBean mChatSetting;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_msg_reminder;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_new_msg_notification);
        setViewsOnClickListener(R.id.llReceiveNewMsgSwitch, R.id.llNotInteruptMode);
    }

    @Override
    public void updateUIText() {
        mChatSetting = DataManager.getInstance().getChatSetting();
        setImage(R.id.ivReceiveNewMsgSwitch, mChatSetting.getIsReceiveNewMsg() == 0 ?
                R.drawable.icon_switch_off : R.drawable.icon_switch_on);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llReceiveNewMsgSwitch) {
            int isReceiveNewMsgSwitch = mChatSetting.getIsReceiveNewMsg();
            mChatSetting.setIsReceiveNewMsg(isReceiveNewMsgSwitch == 0 ? 1 : 0);
            setImage(R.id.ivReceiveNewMsgSwitch, mChatSetting.getIsReceiveNewMsg() == 0 ?
                    R.drawable.icon_switch_off : R.drawable.icon_switch_on);
            DataManager.getInstance().saveChatSetting(mChatSetting);
        } else if (id == R.id.llNotInteruptMode) {
            try {
                // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, getActivity().getApplicationInfo().uid);

                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                intent.putExtra("app_package", getActivity().getPackageName());
                intent.putExtra("app_uid", getActivity().getApplicationInfo().uid);

                // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
                //  if ("MI 6".equals(Build.MODEL)) {
                //      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //      Uri uri = Uri.fromParts("package", getPackageName(), null);
                //      intent.setData(uri);
                //      // intent.setAction("com.android.settings/.SubSettings");
                //  }
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                Intent intent = new Intent();

                //下面这种方案是直接跳转到当前应用的设置界面。
                //https://blog.csdn.net/ysy950803/article/details/71910806
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }
}
