package com.alsc.chat.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;
import com.zhangke.websocket.WebSocketHandler;

public class MsgFilterAdapter extends BaseQuickAdapter<FilterMsgBean, BaseViewHolder> {

    private Context mContext;
    private GroupBean mGroup;

    public MsgFilterAdapter(Context context, GroupBean group) {
        super(R.layout.item_msg_filter);
        mContext = context;
        mGroup = group;
    }

    @Override
    protected void convert(BaseViewHolder helper, final FilterMsgBean item) {
        helper.setText(R.id.tvFilter, item.getContent());
        ImageView ivRemoveMsgFilter = helper.getView(R.id.ivRemoveMsgFilter);
        ivRemoveMsgFilter.setTag(getItemPosition(item));
        ivRemoveMsgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFilterMsg((int) v.getTag());
            }
        });
    }


    private void removeFilterMsg(final int position) {
        ChatHttpMethods.getInstance().groupBlockDelete(String.valueOf(mGroup.getGroupId()), String.valueOf(getItem(position).getBlockId()),
                new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        GroupMessageBean messageBean = GroupMessageBean.getGroupSystemMsg(DataManager.getInstance().getUserId(), mGroup.getGroupId(),
                                Constants.REFRESH_FORBID_LETTER, mGroup.getGroupId());
                        if (WebSocketHandler.getDefault() != null) {
                            WebSocketHandler.getDefault().send(messageBean.toJson());
                        }
                        remove(position);
                        DataManager.getInstance().setGroupFilterMsg(mGroup.getGroupId(), getData());
                    }
                }, mContext, (ChatBaseActivity) mContext));
    }

}
