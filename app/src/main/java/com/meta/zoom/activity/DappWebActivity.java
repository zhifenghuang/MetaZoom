package com.meta.zoom.activity;

import static com.meta.zoom.web3.entity.CryptoFunctions.sigFromByteArray;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.google.gson.Gson;
import com.meta.zoom.R;
import com.meta.zoom.dialog.ConfirmPaymentDialog;
import com.meta.zoom.dialog.SignMessageDialog;
import com.meta.zoom.fragment.WalletFragment;
import com.meta.zoom.wallet.BalanceUtils;
import com.meta.zoom.wallet.WalletManager;
import com.meta.zoom.web3.OnSignMessageListener;
import com.meta.zoom.web3.OnSignPersonalMessageListener;
import com.meta.zoom.web3.OnSignTransactionListener;
import com.meta.zoom.web3.OnSignTypedMessageListener;
import com.meta.zoom.web3.entity.DAppFunction;
import com.meta.zoom.web3.entity.Message;
import com.meta.zoom.web3.entity.TypedData;
import com.meta.zoom.web3.entity.Web3Transaction;
import com.meta.zoom.web3.provider.DappWebViewAdapter;
import com.meta.zoom.web3.provider.SignTransactionInterface;

import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.util.HashMap;

import io.reactivex.schedulers.Schedulers;

public class DappWebActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View, OnSignTransactionListener, OnSignPersonalMessageListener, OnSignTypedMessageListener, OnSignMessageListener, SignTransactionInterface {
    private static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    private static final String JS_PROTOCOL_CANCELLED = "cancelled";
    private static final String JS_PROTOCOL_ON_SUCCESSFUL = "onSignSuccessful(%1$s, \"%2$s\")";
    private static final String JS_PROTOCOL_ON_FAILURE = "onSignError(%1$s, \"%2$s\")";

    private WebView mWeb3;
    private DappWebViewAdapter mDappWebViewAdapter;
    private SignMessageDialog dialog;

    private WalletBean wallet;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dapp_web;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        String url = getIntent().getExtras().getString(Constants.BUNDLE_EXTRA);
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        setText(R.id.tvTitle, url);
        wallet = DataManager.getInstance().getCurrentWallet();
        mWeb3 = findViewById(R.id.web3view);
        mDappWebViewAdapter = new DappWebViewAdapter(this, mWeb3, url, this);
        mDappWebViewAdapter.init();
        setupWeb3();
    }

    @Override
    public void onReceive(HashMap map) {
        if (isFinish()) {
            return;
        }
        if (map.containsKey(EventBusEvent.REFRESH_NETWORK)) {
            mDappWebViewAdapter.init();
        } else {
            super.onReceive(map);
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
        }
    }

    private void setupWeb3() {
        mWeb3.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setText(R.id.tvTitle, title);
            }
        });
    }


    @Override
    public void onSignMessage(Message<String> message) {

        DAppFunction dAppFunction = new DAppFunction() {
            @Override
            public void DAppError(Throwable error, Message<String> message) {
//                web3.onSignCancel(message);
                dialog.dismiss();
            }

            @Override
            public void DAppReturn(byte[] data, Message<String> message) {
                String signHex = Numeric.toHexString(data);
                Log.d(TAG, "Initial Msg: " + message.value);
//                web3.onSignMessageSuccessful(message, signHex);
                dialog.dismiss();
            }
        };

        dialog = new SignMessageDialog(this, message);
        dialog.setAddress(wallet.getAddress());
        dialog.setOnApproveListener(v -> {
            //ensure we generate the signature correctly:
            byte[] signRequest = message.value.getBytes();
            if (message.value.substring(0, 2).equals("0x")) {
                signRequest = Numeric.hexStringToByteArray(message.value);
            }
            //viewModel.signMessage(signRequest, dAppFunction, message, "123456");
            showToast("onSignMessage");
        });
        dialog.setOnRejectListener(v -> {
//            web3.onSignCancel(message);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onSignPersonalMessage(Message<String> message) {
        DAppFunction dAppFunction = new DAppFunction() {
            @Override
            public void DAppError(Throwable error, Message<String> message) {
//                web3.onSignCancel(message);
                dialog.dismiss();
            }

            @Override
            public void DAppReturn(byte[] data, Message<String> message) {
                String signHex = Numeric.toHexString(data);
                Log.d(TAG, "Initial Msg: " + message.value);
//                web3.onSignPersonalMessageSuccessful(message, signHex);
                //Test Sig
                testRecoverAddressFromSignature(hexToUtf8(message.value), signHex);
                dialog.dismiss();
            }
        };

        dialog = new SignMessageDialog(this, message);
        dialog.setAddress(wallet.getAddress());
        dialog.setMessage(hexToUtf8(message.value));
        dialog.setOnApproveListener(v -> {
            String convertedMessage = hexToUtf8(message.value);
            String signMessage = PERSONAL_MESSAGE_PREFIX
                    + convertedMessage.length()
                    + convertedMessage;
            //viewModel.signMessage(signMessage.getBytes(), dAppFunction, message, "123456");

            showToast("onSignPersonalMessage");
        });
        dialog.setOnRejectListener(v -> {
//            web3.onSignCancel(message);
            dialog.dismiss();
        });
        dialog.show();
    }


    @Override
    public void onSignTypedMessage(Message<TypedData[]> message) {
        showToast(new Gson().toJson(message));
    }

    @Override
    public void onSignTransaction(Web3Transaction transaction, String url) {
        LogUtil.LogE("onSignTransaction " + transaction);

        if (transaction.payload == null || transaction.payload.length() < 1) {
            //display transaction error
            onInvalidTransaction();
            onSignCancel(transaction);
        } else {
            // 打开确认窗口， 输入密码
            //viewModel.openConfirmation(getContext(), transaction, url);
            ConfirmPaymentDialog dialog = new ConfirmPaymentDialog(this, transaction, new ConfirmPaymentDialog.OnConfirmListener() {
                @Override
                public void onClick() {

                }
            });
            dialog.show();


        }
    }


    public void onSignCancel(Web3Transaction transaction) {
        long callbackId = transaction.leafPosition;
        callbackToJS(callbackId, JS_PROTOCOL_ON_FAILURE, JS_PROTOCOL_CANCELLED);
    }

    public void onSignCancel(Message message) {
        long callbackId = message.leafPosition;
        callbackToJS(callbackId, JS_PROTOCOL_ON_FAILURE, JS_PROTOCOL_CANCELLED);
    }

    private void callbackToJS(long callbackId, String function, String param) {
        String callback = String.format(function, callbackId, param);
        mWeb3.post(() -> mWeb3.evaluateJavascript(callback, value -> Log.d("WEB_VIEW", value)));
    }

    public static String hexToUtf8(String hex) {
        hex = Numeric.cleanHexPrefix(hex);
        ByteBuffer buff = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 2) {
            buff.put((byte) Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);
        return cb.toString();
    }

    private void onInvalidTransaction() {
        showToast("DApp transaction contains no data");
    }

    private boolean loadUrl(String urlText) {

        mWeb3.loadUrl(urlText);
        mWeb3.requestFocus();


        return true;
    }

    public void testRecoverAddressFromSignature(String message, String sig) {
        String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
        byte[] msgHash = (prefix + message).getBytes(); //Hash.sha3((prefix + message3).getBytes());

        byte[] signatureBytes = Numeric.hexStringToByteArray(sig);
        Sign.SignatureData sd = sigFromByteArray(signatureBytes);
        String addressRecovered;

        try {
            BigInteger recoveredKey = Sign.signedMessageToKey(msgHash, sd);
            addressRecovered = "0x" + Keys.getAddress(recoveredKey);
            System.out.println("Recovered: " + addressRecovered);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void signTransaction(Web3Transaction transaction, String txHex, boolean success) {
        if (success) {
            //           mWeb3.onSignTransactionSuccessful(transaction, txHex);
        } else {
//            web3.onSignCancel(transaction);
        }
    }
}
