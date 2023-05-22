package com.meta.zoom.wallet;

import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.WalletBean;

import java.util.Arrays;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WalletManager {

    private static final String TAG = "WalletManager";

    private static WalletManager mManager = null;


    private WalletManager() {
    }

    public static WalletManager getInstance() {
        if (mManager == null) {
            synchronized (TAG) {
                mManager = new WalletManager();
            }
        }
        return mManager;
    }

    public Single<WalletBean> create(final String name, final String pwd, String confirmPwd, String pwdReminder) {
        return Single.fromCallable(() -> {
                    WalletBean ethWallet = ETHWalletUtils.generateMnemonic(name, pwd);
                    return ethWallet;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Single<WalletBean> loadWalletByKeystore(final String keystore, final String pwd) {
        return Single.fromCallable(() -> {
                    WalletBean ethWallet = ETHWalletUtils.loadWalletByKeystore(keystore, pwd);
                    return ethWallet;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<WalletBean> loadWalletByPrivateKey(final String privateKey, final String pwd) {
        return Single.fromCallable(() -> {

                            WalletBean ethWallet = ETHWalletUtils.loadWalletByPrivateKey(privateKey, pwd);
                            if (ethWallet != null) {
                                DatabaseOperate.getInstance().insert(ethWallet);
                            }
                            return ethWallet;
                        }
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Single<WalletBean> loadWalletByMnemonic(final String bipPath, final String mnemonic, final String pwd) {
        return Single.fromCallable(() -> {
                    WalletBean ethWallet = ETHWalletUtils.importMnemonic(bipPath
                            , Arrays.asList(mnemonic.split(" ")), pwd);
                    return ethWallet;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


    }

    public static boolean isValid(String mnemonic) {
        return mnemonic.split(" ").length >= 12;
    }

}
