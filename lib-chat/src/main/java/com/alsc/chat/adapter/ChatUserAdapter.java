package com.alsc.chat.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.alsc.chat.R;
import com.alsc.chat.manager.ChatManager;;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.ChatBean;
import com.common.lib.bean.MessageType;
import com.common.lib.bean.UserBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatUserAdapter extends BaseQuickAdapter<ChatBean, BaseViewHolder> {

    private Context mContext;

    private UserBean mMyInfo;


    public ChatUserAdapter(Context context, UserBean myInfo) {
        super(R.layout.item_chat_user);
        mContext = context;
        mMyInfo = myInfo;
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatBean chatBean) {
        helper.getView(R.id.llParent).setBackgroundColor(ContextCompat.getColor(mContext, chatBean.isTopChat() ? R.color.color_f3_f7_fa : R.color.color_ff_ff_ff));
        int resId = 0;
        if (chatBean.chatUser != null) {
            helper.setText(R.id.tvName, (mMyInfo.isService() ? ("(" + chatBean.chatUser.getUserId() + ")") : "") + chatBean.chatUser.getNickName());
            resId = mContext.getResources().getIdentifier("chat_default_avatar_" + chatBean.chatUser.getUserId() % 6,
                    "drawable", mContext.getPackageName());
            Utils.loadImage(mContext, resId, chatBean.chatUser.getAvatarUrl(), helper.getView(R.id.ivAvatar));
        } else {
            helper.setText(R.id.tvName, chatBean.group.getName());
            Utils.displayAvatar(mContext, R.drawable.chat_default_group_avatar, chatBean.group.getIcon(), helper.getView(R.id.ivAvatar));
        }
        if (chatBean.isNotInterupt()) {
            helper.setVisible(R.id.tvUnReadMsgNum, false)
                    .setGone(R.id.ivNotInterupt, false)
                    .setGone(R.id.ivNotInteruptDot, chatBean.unReadNum == 0);
        } else {
            helper.setGone(R.id.ivNotInterupt, true)
                    .setGone(R.id.ivNotInteruptDot, true);
            if (chatBean.unReadNum > 0) {
                helper.setVisible(R.id.tvUnReadMsgNum, true)
                        .setText(R.id.tvUnReadMsgNum, String.valueOf(chatBean.unReadNum));
            } else {
                helper.setVisible(R.id.tvUnReadMsgNum, false);
            }
        }
        int msgType = chatBean.lastMsg.getMsgType();
        if (msgType == MessageType.TYPE_INVITE_PAY_IN_GROUP.ordinal()) {
            helper.setText(R.id.tvMessage, mContext.getString(R.string.chat_invite_you_pay_in_group));
        } else {
            helper.setText(R.id.tvMessage, ChatManager.getInstance().getContentByType(chatBean.lastMsg, mContext));
        }
        if (msgType == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()
                || msgType == MessageType.TYPE_REMOVE_FROM_GROUP.ordinal()
                || msgType == MessageType.TYPE_IN_GROUP_BY_QRCODE.ordinal()
                || msgType == MessageType.TYPE_UPDATE_GROUP_NAME.ordinal()
                || msgType == MessageType.TYPE_UPDATE_GROUP_NOTICE.ordinal()
                || msgType == MessageType.TYPE_ALL_FORBID_CHAT.ordinal()
                || msgType == MessageType.TYPE_ALL_REMOVE_FORBID_CHAT.ordinal()
                || msgType == MessageType.TYPE_FORBID_CHAT.ordinal()
                || msgType == MessageType.TYPE_REOMVE_FORBID_CHAT.ordinal()
                || msgType == MessageType.TYPE_INVITE_TO_GROUP.ordinal()
                || msgType == MessageType.TYPE_INVITE_PAY_IN_GROUP.ordinal()) {
            helper.setGone(R.id.ivResend, true);
        } else {
            helper.setGone(R.id.ivResend, chatBean.lastMsg.getSendStatus() >= 0);
        }
    }

    public void resortList() {
        List<ChatBean> list = getData();
        Collections.sort(list, new Comparator<ChatBean>() {
            @Override
            public int compare(ChatBean o1, ChatBean o2) {

                long time1 = o1.lastMsg.getCreateTime();
                long time2 = o2.lastMsg.getCreateTime();
                if (o1.isTopChat() && o2.isTopChat()) {
                    return Long.compare(time2, time1);
                }
                if (o1.isTopChat()) {
                    return -1;
                }
                if (o2.isTopChat()) {
                    return 1;
                }
                return Long.compare(time2, time1);
            }
        });
    }

    public void removeChatUser(ChatBean chatBean) {
        getData().remove(chatBean);
        notifyDataSetChanged();
    }

    public void removeGroupChat(long groupId) {
        List<ChatBean> list = getData();
        for (ChatBean bean : list) {
            if (bean.group != null && bean.group.getGroupId() == groupId) {
                list.remove(bean);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void receiveNewMsg(ChatBean chatBean) {
        List<ChatBean> list = getData();
        if (list.isEmpty()) {
            list.add(chatBean);
        } else if (chatBean.isTopChat()) {
            list.add(0, chatBean);
        } else {
            int index = 0;
            for (ChatBean bean : list) {
                if (!bean.isTopChat()) {
                    break;
                }
                ++index;
            }
            list.add(index, chatBean);
        }
        notifyDataSetChanged();
    }
}
