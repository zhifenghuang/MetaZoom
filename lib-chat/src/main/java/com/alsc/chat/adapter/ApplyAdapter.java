package com.alsc.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;
import com.google.gson.Gson;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class ApplyAdapter extends BaseQuickAdapter<UserBean, BaseViewHolder> {

    private Context mContext;

    public ApplyAdapter(Context context) {
        super(R.layout.item_apply);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final UserBean item) {
        helper.setText(R.id.tvName, item.getMemo())
                .setText(R.id.tvApplyText, TextUtils.isEmpty(item.getRemark()) ? mContext.getString(R.string.chat_request_you_as_friend) : item.getRemark());
        Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, item.getAvatarUrl(), helper.getView(R.id.ivAvatar));
        TextView tvOperator = helper.getView(R.id.tvOperator);
        if (item.getStatus() == 0) {
            tvOperator.setBackgroundResource(R.drawable.bg_chat_apply);
            tvOperator.setText(R.string.chat_accespt);
            tvOperator.setTextColor(ContextCompat.getColor(mContext, R.color.color_3a_54_ff));
            tvOperator.setTag(item);
            tvOperator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserBean user = (UserBean) v.getTag();
                    addFriend(user);
                }
            });
        } else {
            tvOperator.setBackground(null);
            tvOperator.setOnClickListener(null);
            tvOperator.setText(R.string.chat_had_add);
            tvOperator.setTextColor(ContextCompat.getColor(mContext, R.color.color_8c_8c_8c));
        }

    }


    private void addFriend(final UserBean user) {
        ChatHttpMethods.getInstance().replayContact(String.valueOf(user.getUserId()), "1",
                "", new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        UserBean myInfo = DataManager.getInstance().getUser();
                        MessageBean bean = new MessageBean();
                        bean.setCmd(2000);
                        bean.setFromId(myInfo.getUserId());
                        bean.setToId(user.getUserId());
                        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                        list.add(myInfo.toMap());
                        list.add(user.toMap());
                        bean.setMsgType(MessageType.TYPE_TEXT.ordinal());
                        bean.setContent(mContext.getString(R.string.chat_add_friend_first_said));
                        bean.setExtra(new Gson().toJson(list));
                        if (WebSocketHandler.getDefault() != null) {
                            WebSocketHandler.getDefault().send(bean.toJson());
                        }
                        DatabaseOperate.getInstance().insert(bean);
                        user.setStatus(1);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.REDRESH_FRIENDS, "");
                        EventBus.getDefault().post(map);
                        notifyDataSetChanged();
                    }
                }, mContext, (ChatBaseActivity) mContext));
    }

}
