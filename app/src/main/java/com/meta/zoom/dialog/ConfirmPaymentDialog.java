package com.meta.zoom.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.common.lib.activity.BaseActivity;
import com.common.lib.manager.DataManager;
import com.common.lib.utils.LogUtil;
import com.meta.zoom.R;
import com.meta.zoom.activity.TransferActivity;
import com.meta.zoom.wallet.BalanceUtils;
import com.meta.zoom.wallet.WalletManager;
import com.meta.zoom.web3.entity.Web3Transaction;

import org.web3j.utils.Convert;

import java.math.BigInteger;

import io.reactivex.schedulers.Schedulers;

public class ConfirmPaymentDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private OnConfirmListener mListener;
    private BigInteger mGasLimit;

    private Web3Transaction mTransaction;


    public ConfirmPaymentDialog(Context context, Web3Transaction transaction, OnConfirmListener listener) {
        this(context, com.alsc.chat.R.style.LoadingDialog, transaction);
        mListener = listener;
    }


    @SuppressLint("MissingInflatedId")
    public ConfirmPaymentDialog(Context context, int themeId, Web3Transaction transaction) {
        super(context, themeId);
        this.mContext = context;
        setContentView(R.layout.layout_confirm_payment);

        Window view = getWindow();
        WindowManager.LayoutParams lp = view.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        view.setGravity(Gravity.BOTTOM);

        findViewById(R.id.ivClose).setOnClickListener(this);
        findViewById(R.id.tvOk).setOnClickListener(this);
        ((TextView) findViewById(R.id.tvFrom)).setText(transaction.from.toString());
        ((TextView) findViewById(R.id.tvTo)).setText(transaction.recipient.toString());
        try {
            ((TextView) findViewById(R.id.tvValue)).setText((transaction.value == null ? "0" : BalanceUtils.weiToEth(transaction.value))
                    + " " + DataManager.getInstance().getCurrentChain().getSymbol());
        } catch (Exception e) {

        }
        mGasLimit = new BigInteger("21000");
        mTransaction = transaction;
        ((TextView) findViewById(R.id.tvGas)).setText("0 " + DataManager.getInstance().getCurrentChain().getSymbol());
        WalletManager.getInstance().estimateGasLimit(transaction.from.toString(), transaction.recipient.toString(),
                        transaction.payload).subscribeOn(Schedulers.io())
                .subscribe(this::getGasLimit, this::onError);

    }

    private void getGasLimit(BigInteger gasLimit) {
        LogUtil.LogE("getGasLimit");
        mGasLimit = gasLimit;
        resetFee();
    }

    private void onError(Throwable throwable) {
        resetFee();
    }

    private void resetFee() {
        String fee = "0";
        try {
            fee = BalanceUtils.weiToEth(mTransaction.gasPrice.multiply(mGasLimit), 4);
        } catch (Exception e) {

        }
        ((TextView) findViewById(R.id.tvGas)).setText(fee + " " + DataManager.getInstance().getCurrentChain().getSymbol());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvOk:
                WalletManager.getInstance().createTransaction(
                                DataManager.getInstance().getCurrentWallet(),
                                mTransaction.recipient.toString(),
                                mTransaction.value,
                                mTransaction.gasPrice, mGasLimit,
                                mTransaction.payload,
                                "h123456")
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::onCreateTransaction, this::onError);
             //   dismiss();
                break;
            case R.id.ivClose:
                dismiss();
                break;
        }
    }

    private void onCreateTransaction(String transaction) {
        LogUtil.LogE("onCreateTransaction: "+transaction);
    }

    public interface OnConfirmListener {
        public void onClick();
    }

}

