package com.alsc.chat.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.*;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class ServiceMessageAdapter extends MessageAdapter {


    public ServiceMessageAdapter(Context context, UserBean myInfo) {
        super(context, myInfo);
        mContext = context;
        mMyInfo = myInfo;
    }


    @Override
    protected void showUserInfo(BaseViewHolder helper, BasicMessage item) {
        if (item.isMySendMsg(mMyInfo.getUserId())) {
            Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, mMyInfo.getAvatarUrl(), helper.getView(R.id.ivRight));
        } else {
            ImageView ivLeft = helper.getView(R.id.ivLeft);
            if (mChatUser == null) {
                helper.setGone(R.id.tvNickLeft, true);
                ivLeft.setImageResource(R.drawable.chat_default_avatar);
            } else {
                helper.setVisible(R.id.tvNickLeft, true)
                        .setText(R.id.tvNickLeft, mChatUser == null ? "" : mChatUser.getNickName());
                Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, mChatUser == null ? "" : mChatUser.getAvatarUrl(), ivLeft);
            }
        }
    }

//    protected void setOnClickListener(View rootView, View view, int position) {
//        view.setTag(R.id.chat_id, position);
//        rootView.setTag(R.id.chat_id, position);
//        if (!isEditMode()) {
//            rootView.setOnClickListener(null);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
//                }
//            });
//
//        } else {
//            view.setOnClickListener(null);
//            view.setOnLongClickListener(null);
//            rootView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
//                }
//            });
//        }
//    }

    @Override
    protected void showServices(RecyclerView recyclerView, TextView tvNum, BasicMessage item) {
        SelectServiceAdapter adapter;
        if (recyclerView.getAdapter() == null) {
            adapter = new SelectServiceAdapter(mContext);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter.onAttachedToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (mChatUser != null) {
                        return;
                    }
                    mChatUser = ((SelectServiceAdapter) adapter).getItem(position);
                    HashMap<String, UserBean> map = new HashMap<>();
                    map.put(Constants.SELECT_SERVICE, mChatUser);
                    EventBus.getDefault().post(map);
                    ServiceMessageBean msg = new ServiceMessageBean();
                    msg.setCmd(2200);
                    msg.setMsgType(MessageType.TYPE_TEXT.ordinal());
                    msg.setFromId(mChatUser == null ? 0 : mChatUser.getUserId());
                    msg.setToId(mMyInfo.getUserId());
                    msg.setContent(mContext.getString(R.string.chat_service_first_msg));
                    ServiceMessageAdapter.this.addData(msg);
                }
            });
        } else {
            adapter = (SelectServiceAdapter) recyclerView.getAdapter();
        }
        adapter.setNewInstance(((ServiceMessageBean) item).getServices());
        adapter.notifyDataSetChanged();
        tvNum.setText(mContext.getString(R.string.chat_xxx_online, String.valueOf(adapter.getItemCount())));
    }

    @Override
    protected void showQuestion(RecyclerView recyclerView, BasicMessage item) {
        QuestionAdapter adapter;
        if (recyclerView.getAdapter() == null) {
            adapter = new QuestionAdapter();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter.onAttachedToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    ServiceMessageBean msg = new ServiceMessageBean();
                    msg.setCmd(2200);
                    msg.setMsgType(MessageType.TYPE_TEXT.ordinal());
                    msg.setFromId(mChatUser == null ? 0 : mChatUser.getUserId());
                    msg.setToId(mMyInfo.getUserId());
                    msg.setContent(((QuestionAdapter) adapter).getItem(position).getAnswers());
                    ServiceMessageAdapter.this.addData(msg);
                    RecyclerView recyclerView = ServiceMessageAdapter.this.getRecyclerView();
                    recyclerView.smoothScrollToPosition(ServiceMessageAdapter.this.getItemCount() - 1);
                }
            });
        } else {
            adapter = (QuestionAdapter) recyclerView.getAdapter();
        }
        adapter.setNewInstance(((ServiceMessageBean) item).getQuestions());
        adapter.notifyDataSetChanged();
    }

}
