package com.alsc.chat.adapter;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.fragment.RedPackageRecordFragment;
import com.alsc.chat.utils.Utils;
import com.common.lib.bean.*;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class RedPackageRecordAdapter extends BaseMultiItemQuickAdapter<RedPackageRecordFragment.RedPackageRecordItem, BaseViewHolder> {

    private Context mContext;
    private int mCurrentType;  //1我收到的红包2我发出的红包
    private UserBean mMyInfo;
    private EnvelopeRecordBean mRecordBean;


    public RedPackageRecordAdapter(Context context) {
        super(new ArrayList<>());
        addItemType(0, R.layout.item_red_package_record_0);
        addItemType(1, R.layout.item_red_package_record_1);
        mContext = context;
    }

    public void setEnvelope(UserBean myInfo, int type) {
        mMyInfo = myInfo;
        mCurrentType = type;
    }

    public void setRedPackageData(EnvelopeRecordBean bean, int type) {
        mRecordBean = bean;
        mCurrentType = type;
        List<RedPackageRecordFragment.RedPackageRecordItem> list = getData();
        list.clear();
        list.add(new RedPackageRecordFragment.RedPackageRecordItem());
        ArrayList<EnvelopeRecordBean.Session> sessions = bean.getList();
        if (sessions != null && !sessions.isEmpty()) {
            RedPackageRecordFragment.RedPackageRecordItem item;
            for (EnvelopeRecordBean.Session session : sessions) {
                item = new RedPackageRecordFragment.RedPackageRecordItem();
                item.setItemType(1);
                item.setSession(session);
                list.add(item);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    protected void convert(@NotNull BaseViewHolder helper, @Nullable RedPackageRecordFragment.RedPackageRecordItem item) {
        switch (getDefItemViewType(getItemPosition(item))) {
            case 0:
                Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, mMyInfo.getAvatarUrl(), helper.getView(R.id.ivAvatar));
                if (mCurrentType == 1) {
                    helper.setText(R.id.tvXXXRedPackage, mContext.getString(R.string.chat_xxx_total_receive, mMyInfo.getNickName()))
                            .setText(R.id.tvRedPackageTotalValue, mRecordBean == null ? "0.00" : String.format("%.8f", mRecordBean.getSum()))
                            .setGone(R.id.tvSendRedPackageNum, true)
                            .setGone(R.id.llReceiveRedPackageNum, false)
                            .setText(R.id.tvReceiveRedPackageNum, String.valueOf(mRecordBean == null ? 0 : mRecordBean.getTotalCount()))
                            .setText(R.id.tvMaxNum, String.valueOf(mRecordBean == null ? 0 : mRecordBean.getMaxCount()));
                } else {
                    helper.setText(R.id.tvXXXRedPackage, mContext.getString(R.string.chat_xxx_total_send, mMyInfo.getNickName()))
                            .setText(R.id.tvRedPackageTotalValue, mRecordBean == null ? "0.00" : String.format("%.8f", mRecordBean.getSum()))
                            .setGone(R.id.tvSendRedPackageNum, false)
                            .setGone(R.id.llReceiveRedPackageNum, true);
                    ((TextView) helper.getView(R.id.tvSendRedPackageNum)).setText(Html.fromHtml(mContext.getString(R.string.chat_total_send_num, String.valueOf(mRecordBean == null ? 0 : mRecordBean.getTotalCount()))));
                }
                break;
            case 1:
                EnvelopeRecordBean.Session session = item.getSession();
                if (mCurrentType == 1) {
                    helper.setText(R.id.tvName, session.getSenderName())
                            .setText(R.id.tvTime, session.getCreateTime())
                            .setText(R.id.tvValue, String.format("%.8f", session.getAmount()))
                            .setGone(R.id.tvMax, session.getMax() != 1)
                            .setGone(R.id.tvNum, true);
                } else {
                    helper.setText(R.id.tvName, session.getSenderName())
                            .setText(R.id.tvTime, session.getCreateTime())
                            .setText(R.id.tvValue, String.format("%.8f", session.getAmount()))
                            .setGone(R.id.tvMax, true)
                            .setGone(R.id.tvNum, false)
                            .setText(R.id.tvNum, (session.getStatus() == 3 ? mContext.getString(R.string.chat_had_expire) : "")
                                    + session.getSessionCount() + "/" + session.getTotalCount());
                }
                break;
        }
    }
}
