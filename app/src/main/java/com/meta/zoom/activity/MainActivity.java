package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alsc.chat.manager.ChatManager;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.UserBean;
import com.common.lib.fragment.BaseFragment;
import com.common.lib.manager.DataManager;
import com.meta.zoom.R;
import com.meta.zoom.contract.MainContract;
import com.meta.zoom.fragment.ChatMsgFragment;
import com.meta.zoom.fragment.WalletFragment;
import com.meta.zoom.presenter.MainPresenter;

import java.util.ArrayList;

public class MainActivity extends BaseActivity<MainContract.Presenter> implements MainContract.View {

    private ArrayList<BaseFragment> mBaseFragment;
    private int mCurrentItem;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        initFragments();
        initViews();
        resetBottomBar(0);
    }

    private void initFragments() {
        mBaseFragment = new ArrayList<>();
        mBaseFragment.add(new ChatMsgFragment());
        mBaseFragment.add(new WalletFragment());
        mBaseFragment.add(new WalletFragment());
        mBaseFragment.add(new WalletFragment());
    }

    private void initViews() {
        LinearLayout llBottom = findViewById(R.id.llBottom);
        int count = llBottom.getChildCount();
        View itemView;
        for (int i = 0; i < count; ++i) {
            itemView = llBottom.getChildAt(i);
            itemView.setTag(i);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tag = (int) view.getTag();
                    if (tag == mCurrentItem) {
                        return;
                    }
                    mBaseFragment.get(tag).onRefresh();
                    switchFragment(mBaseFragment.get(tag));
                    resetBottomBar(tag);
                }
            });
        }
    }

    private void resetBottomBar(int currentPos) {
        mCurrentItem = currentPos;
        LinearLayout llBottom = findViewById(R.id.llBottom);
        int count = llBottom.getChildCount();
        for (int i = 0; i < count; ++i) {
            (((ImageView) llBottom.getChildAt(i))).setImageResource(getResIdByIndex(i, currentPos == i));
        }
    }

    private int getResIdByIndex(int index, boolean isCheck) {
        int id = 0;
        switch (index) {
            case 0:
                id = isCheck ? R.drawable.app_message_on : R.drawable.app_message_off;
                break;
            case 1:
                id = isCheck ? R.drawable.app_website_on : R.drawable.app_website_off;
                break;
            case 2:
                id = isCheck ? R.drawable.app_wallet_on : R.drawable.app_wallet_off;
                break;
            case 3:
                id = isCheck ? R.drawable.app_setting_on : R.drawable.app_setting_off;
                break;
        }
        return id;
    }

    @NonNull
    @Override
    protected MainContract.Presenter onCreatePresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    public int getContainerViewId() {
        return R.id.fl;
    }

    @Override
    public void loginSuccess() {

    }
}
