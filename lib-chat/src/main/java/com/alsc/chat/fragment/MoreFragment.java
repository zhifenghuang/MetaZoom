package com.alsc.chat.fragment;

import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.utils.Constants;
import com.common.lib.bean.UserBean;

public class MoreFragment extends ChatBaseFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_more;
    }

    @Override
    protected void onViewCreated(View view) {
        UserBean user = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_more);
        setText(R.id.tvAddType, user.getAddType() == 0 ? R.string.chat_by_search : R.string.chat_by_qrcode);
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {

    }

}
