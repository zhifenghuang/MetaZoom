package com.meta.zoom.activity;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.CaptureActivity;
import com.common.lib.bean.TokenBean;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.common.lib.utils.PermissionUtil;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.meta.zoom.dialog.InputDialog;
import com.meta.zoom.wallet.BalanceUtils;
import com.meta.zoom.wallet.WalletManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class TransferActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private TokenBean mToken;
    private BigInteger mGasPrice;
    private BigInteger mGasLimit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_send);
        setViewsOnClickListener(R.id.tvSend, R.id.ivRight);
        setViewVisible(R.id.ivRight);
        Bundle bundle = getIntent().getExtras();
        mToken = (TokenBean) bundle.getSerializable(Constants.BUNDLE_EXTRA);
        int drawableId = 0;
        if (TextUtils.isEmpty(mToken.getContractAddress())) {
            try {
                drawableId = getResources().getIdentifier("app_symbol_" + mToken.getChainId(), "drawable", getPackageName());
            } catch (Exception e) {
            }
        }
        setImage(R.id.ivSymbol, drawableId == 0 ? R.drawable.app_unknow_symbol : drawableId);
        setText(R.id.tvName, mToken.getSymbol());
        setText(R.id.tvBalance, getString(R.string.app_balance_xxx, TextUtils.isEmpty(mToken.getBalance()) ? "0" : mToken.getBalance()));
        initInputListener();
        mGasLimit = new BigInteger(TextUtils.isEmpty(mToken.getContractAddress()) ? "21000" : "100000");
        WalletManager.getInstance().estimateGas().subscribeOn(Schedulers.io())
                .subscribe(this::getGas, this::onError);

    }

    private void getGas(BigInteger gas) {
        mGasPrice = gas;
        try {
            setText(R.id.tvFee, BalanceUtils.weiToEth(mGasPrice.multiply(mGasLimit), 4) + DataManager.getInstance().getCurrentChain().getSymbol());
        } catch (Exception e) {
            setText(R.id.tvFee, "0 " + DataManager.getInstance().getCurrentChain().getSymbol());
        }
    }


    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivRight:
                if (!PermissionUtil.INSTANCE.isGrantPermission(this, Manifest.permission.CAMERA)) {
                    requestPermission(null, Manifest.permission.CAMERA);
                    return;
                }
                openActivity(CaptureActivity.class);
                break;
            case R.id.tvSend:
                showProgressDialog();
                new InputDialog(this, new InputDialog.OnInputListener() {
                    @Override
                    public void checkInput(String password) {
                        if (TextUtils.isEmpty(mToken.getContractAddress())) {
                            WalletManager.getInstance().createEthTransaction(
                                            DataManager.getInstance().getCurrentWallet(),
                                            getTextById(R.id.etAddress),
                                            Convert.toWei(getTextById(R.id.etAmount), Convert.Unit.ETHER).toBigInteger(),
                                            mGasPrice, mGasLimit, password)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(TransferActivity.this::onCreateTransaction, TransferActivity.this::onError);

                        } else {
                            WalletManager.getInstance().createERC20Transfer(
                                            DataManager.getInstance().getCurrentWallet(),
                                            getTextById(R.id.etAddress), mToken.getContractAddress(),
                                            Convert.toWei(getTextById(R.id.etAmount), Convert.Unit.ETHER).toBigInteger(),
                                            mGasPrice, mGasLimit, password)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(TransferActivity.this::onCreateTransaction, TransferActivity.this::onError);
                        }

                    }
                });
                break;
        }
    }

    private void onCreateTransaction(String transaction) {
        dismissProgressDialog();
        showToast(R.string.app_send_success);
        HashMap<String, Object> map = new HashMap<>();
        map.put(EventBusEvent.REFRESH_ASSETS, "");
        EventBus.getDefault().post(map);
        finish();
    }

    private void onError(Throwable throwable) {
        dismissProgressDialog();
        LogUtil.LogE(throwable);
        showToast(R.string.app_send_failed);
    }

    private void initInputListener() {
        final TextView tvSend = findViewById(R.id.tvSend);
        tvSend.setEnabled(false);
        tvSend.setAlpha(0.25f);
        final EditText etAmount = findViewById(R.id.etAmount);
        final EditText etAddress = findViewById(R.id.etAddress);
        Observable.combineLatest(RxTextView.textChanges(etAddress).skip(1),
                RxTextView.textChanges(etAmount).skip(1),
                (charSequence, charSequence2) ->
                        !TextUtils.isEmpty(etAmount.getText().toString().trim())
                                && !TextUtils.isEmpty(etAddress.getText().toString().trim())).subscribe(aBoolean -> {
            if (aBoolean) {
                tvSend.setAlpha(1.0f);
                tvSend.setEnabled(true);
            } else {
                tvSend.setEnabled(false);
                tvSend.setAlpha(0.25f);
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap<String, Object> map) {
        if (map == null) {
            return;
        }
        if (map.containsKey(EventBusEvent.SCAN_RESULT)) {
            String address = (String) map.get(EventBusEvent.SCAN_RESULT);
            if (!TextUtils.isEmpty(address)) {
                setText(R.id.etAddress, address);
            }
        }
    }
}
