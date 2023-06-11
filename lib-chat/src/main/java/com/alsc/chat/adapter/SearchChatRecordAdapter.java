package com.alsc.chat.adapter;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.fragment.SearchFragment;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.*;

public class SearchChatRecordAdapter extends BaseQuickAdapter<BasicMessage, BaseViewHolder> {

    private Context mContext;

    private UserBean mChatUser;
    private GroupBean mGroup;
    private int mSearchType;
    private String mSearchText;

    public SearchChatRecordAdapter(Context context) {
        super(R.layout.item_search_record);
        mContext = context;
    }

    public void setSearchObject(int searchType, Object object) {
        mSearchType = searchType;
        if (mSearchType == SearchFragment.SEARCH_CHAT_RECORD) {
            mChatUser = (UserBean) object;
        } else {
            mGroup = (GroupBean) object;
        }
    }

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    @Override
    protected void convert(BaseViewHolder helper, BasicMessage item) {
        if (mSearchType == SearchFragment.SEARCH_CHAT_RECORD) {
            helper.setText(R.id.tvName, mChatUser.getNickName());
            int resId = mContext.getResources().getIdentifier("chat_default_avatar_" + mChatUser.getUserId() % 6,
                    "drawable", mContext.getPackageName());
            Utils.displayAvatar(mContext, resId, mChatUser.getAvatarUrl(), helper.getView(R.id.ivAvatar));
        } else {
            helper.setText(R.id.tvName, mGroup.getName());
            Utils.displayAvatar(mContext, R.drawable.chat_default_group_avatar, mGroup.getIcon(), helper.getView(R.id.ivAvatar));
        }
        String content = item.getContent();
        content = content.replaceAll(mSearchText, "<font color=\"#7A5BD0\">" + mSearchText + "</font>");
        content = Utils.longToDate(item.getCreateTime()) + " " + content;
        ((TextView) helper.getView(R.id.tvMessage)).setText(Html.fromHtml(content));
    }
}
