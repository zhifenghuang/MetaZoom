package com.meta.zoom.activity;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.Constants;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.interfaces.OnClickCallback;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.BaseUtils;
import com.meta.zoom.R;
import com.meta.zoom.dialog.InputDialog;
import com.meta.zoom.wallet.WalletManager;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

public class ManageWalletActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private WalletBean mWallet;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_manage_wallet;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        mWallet = (WalletBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        setText(R.id.tvTitle, R.string.app_manage);
        setViewsOnClickListener(R.id.tvAddress, R.id.tvModifyName, R.id.tvViewPrivateKey, R.id.tvViewKeystore, R.id.tvDelete);

        if (mWallet.getId().intValue() == DataManager.getInstance().getCurrentWallet().getId().intValue()) {
            setViewGone(R.id.tvDelete);
        }
        int drawableId = 0;
        try {
            drawableId = getResources().getIdentifier("app_symbol_" + mWallet.getChainId(), "drawable", getPackageName());
        } catch (Exception e) {
        }
        setImage(R.id.ivAvatar, drawableId == 0 ? R.drawable.app_unknow_symbol : drawableId);
        setText(R.id.tvName, mWallet.getWalletName());
        String address = mWallet.getAddress();
        setText(R.id.tvAddress, address.substring(0, 6) + "..." + address.substring(address.length() - 6));
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddress:
                BaseUtils.StaticParams.copyData(this, mWallet.getAddress());
                showToast(com.alsc.chat.R.string.chat_copy_successful);
                break;
            case R.id.tvModifyName:
                MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_modify_name_dialog);
                dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
                    @Override
                    public void initView(@Nullable View view) {
                        dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
                    }

                    @Override
                    public void onViewClick(int viewId) {
                        if (viewId == R.id.btn2) {
                            String newName = getTextBy(dialogFragment.getView().findViewById(R.id.etName));
                            mWallet.setWalletName(newName);
                            ContentValues values = new ContentValues();
                            values.put(mWallet.getPrimaryKeyName(), mWallet.getId());
                            values.put("walletName", newName);
                            DatabaseOperate.getInstance().update(mWallet, values);
                            showToast(R.string.app_modify_successful);
                            if (mWallet.getId().intValue() == DataManager.getInstance().getCurrentWallet().getId().intValue()) {
                                DataManager.getInstance().saveCurrentWallet(mWallet);
                            }
                            setText(R.id.tvName, newName);
                        }
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "MyDialogFragment");
                break;
            case R.id.tvViewPrivateKey:
                showInputPswDialog(0);
                break;
            case R.id.tvViewKeystore:
                showInputPswDialog(1);
                break;
            case R.id.tvDelete:
                showTwoBtnDialog(getString(R.string.app_are_you_sure_delete_wallet),
                        getString(R.string.app_cancel),
                        getString(R.string.app_confirm),
                        new OnClickCallback() {
                            @Override
                            public void onClick(int viewId) {
                                showInputPswDialog(2);
                            }
                        });
                break;
        }
    }

    private void showInputPswDialog(int type) {
        new InputDialog(this, new InputDialog.OnInputListener() {
            @Override
            public void checkInput(String password) {
                if (!password.equals(mWallet.getPassword())) {
                    showToast(R.string.app_invalid_password_provided);
                    return;
                }
                switch (type) {
                    case 0:
                        try {
                            Credentials credentials = WalletUtils.loadCredentials(password, mWallet.getKeystorePath());
                            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.BUNDLE_EXTRA, privateKey);
                            openActivity(ViewPrivateKeyActivity.class, bundle);
                        } catch (Exception e) {

                        }
                        break;
                    case 1:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, WalletManager.getInstance().deriveKeystore(mWallet));
                        openActivity(ViewKeystoreActivity.class, bundle);
                        break;
                    case 2:
                        DatabaseOperate.getInstance().delete(mWallet, String.valueOf(mWallet.getId()));
                        showToast(R.string.app_delete_successful);
                        finish();
                        break;
                }
            }
        });
    }
}
