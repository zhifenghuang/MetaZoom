package com.alsc.chat.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.alsc.chat.R;
import com.alsc.chat.fragment.RedPackageResultFragment;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


public class RedPackageResultAdapter extends BaseMultiItemQuickAdapter<RedPackageResultFragment.RedPackageResultItem, BaseViewHolder> {

    private Context mContext;

    private EnvelopeBean mEnvelope;
    private UserBean mMyInfo;
    private boolean mIsMy;
    private EnvelopeBean.Session mMySession = null;

    public RedPackageResultAdapter(Context context) {
        super(new ArrayList<>());
        addItemType(0, R.layout.item_red_package_result_0);
        addItemType(1, R.layout.item_red_package_result_1);
        addItemType(2, R.layout.item_red_package_result_2);
        mContext = context;
    }

    public void setEnvelope(UserBean myInfo, EnvelopeBean envelope) {
        mMyInfo = myInfo;
        mIsMy = myInfo.getUserId() == envelope.getUserId();
        mEnvelope = envelope;
        ArrayList<EnvelopeBean.Session> list = mEnvelope.getSession();
        if (list != null) {
            for (EnvelopeBean.Session session : list) {
                if (session.getUserId() == mMyInfo.getUserId()) {
                    mMySession = session;
                    break;
                }
            }
        }
    }


    @Override
    protected void convert(@NotNull BaseViewHolder helper, @Nullable RedPackageResultFragment.RedPackageResultItem item) {
        switch (getDefItemViewType(getItemPosition(item))) {
            case 0:
                helper.setText(R.id.tvXXXRedPackage, mContext.getString(R.string.chat_xxx_red_package, mEnvelope.getNickName()));
                Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, mEnvelope.getAvatarUrl(), helper.getView(R.id.ivAvatar));

                int status = mEnvelope.getStatus();
                if (mEnvelope.getType() == 5) {
                    helper.setImageResource(R.id.ivTopBg, R.drawable.icon_new_member_red_package_result_top_bg);
                    if (mEnvelope.getStatus() == 3) {
                        helper.setText(R.id.tvRedPackageDes,
                                mContext.getString(R.string.chat_new_member_special_red_package)
                                        + "\n" + mContext.getString(R.string.chat_had_unlock));
                    } else {
                        helper.setText(R.id.tvRedPackageDes, mContext.getString(R.string.chat_new_member_special_red_package));
                    }
                } else {
                    helper.setImageResource(R.id.ivTopBg, R.drawable.icon_red_package_result_top_bg);
                    helper.setText(R.id.tvRedPackageDes,
                            TextUtils.isEmpty(mEnvelope.getRemark()) ? mContext.getString(R.string.chat_red_package_default_remark) : mEnvelope.getRemark());
                }
                if (mEnvelope.getType() == 4) {
                    if (mIsMy) {
                        if (status == 1) {
                            helper.setText(R.id.tvRedPackageStatus,
                                    mContext.getString(R.string.chat_wait_peer_to_red_pacage));
                        } else if (status == 2) {
                            helper.setText(R.id.tvRedPackageStatus,
                                    mContext.getString(R.string.chat_peer_had_get_red_pacage));
                        } else {
                            helper.setText(R.id.tvRedPackageStatus,
                                    mContext.getString(R.string.chat_red_package_had_expire));
                        }
                    } else {
                        helper.setText(R.id.tvRedPackageStatus,
                                mContext.getString(status == 2 ? R.string.chat_had_get_red_pacage : R.string.chat_red_package_had_expire));
                    }
                    helper.setText(R.id.tvRedPackageValue, String.format("%.8f", mEnvelope.getAmount()));
                } else {
                    if (mMySession != null) {
                        helper.setVisible(R.id.tvRedPackageValue, true)
                                .setText(R.id.tvRedPackageValue, String.format("%.8f", mMySession.getAmount()));
                    } else {
                        helper.setVisible(R.id.tvRedPackageValue, false);
                    }
                    if (mIsMy) {
                        float rest = 0.0f;
                        ArrayList<EnvelopeBean.Session> list = mEnvelope.getSession();
                        if (list != null) {
                            for (EnvelopeBean.Session session : list) {
                                rest += session.getAmount();
                            }
                        }
                        String str = mContext.getString(R.string.chat_group_red_pacage_status, mEnvelope.getSession().size() + "/" + mEnvelope.getTotalCount())
                                + mContext.getString(R.string.chat_group_red_pacage_total,
                                String.format("%.8f", rest + 0.0000000001) + "/" + mEnvelope.getAmount());
                        helper.setText(R.id.tvRedPackageStatus, str);
                    } else {
                        String str = mContext.getString(R.string.chat_group_red_pacage_status, mEnvelope.getSession().size() + "/" + mEnvelope.getTotalCount());
                        helper.setText(R.id.tvRedPackageStatus, str);
                    }
                }
                break;
            case 1:
                EnvelopeBean.Session session = item.getSession();
                helper.setText(R.id.tvName, session.getNickName())
                        .setText(R.id.tvTime, session.getCreateTime())
                        .setText(R.id.tvValue, String.format("%.8f", session.getAmount()));
                break;
        }
    }
}
