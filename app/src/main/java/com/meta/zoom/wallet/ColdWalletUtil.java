package com.meta.zoom.wallet;

import com.common.lib.bean.WalletBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



/**
 * 冷钱包方法统一入口
 *
 * @author rainking
 */
public final class ColdWalletUtil {

    /**
     * 通用的以太坊基于bip44协议的助记词路径
     */
    public final static String ETH_PATH = "m/44'/60'/0'/0/0";


    /**
     * 创建多链钱包（第一次使用调用返回5个币）
     *
     * @param walletName 钱包名称
     * @param pwd        钱包密码
     * @return 钱包列表
     * @throws Exception 异常处理
     */
    public static WalletBean createColdWallet(String walletName, String pwd) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "");
        return importMnemonic(walletName, ds.getMnemonicCode(), pwd);
    }

    /**
     * 创建多链钱包（第一次使用调用返回5个币）
     *
     * @param walletName 钱包名称
     * @param segWit     是否隔离见证 true 隔离 false 普通
     * @param pwd        钱包密码
     * @return 钱包列表
     * @throws Exception 异常处理
     */
    public static WalletBean createColdWallet(String walletName, boolean segWit, String pwd) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "");
        return importMnemonic(walletName, ds.getMnemonicCode(), segWit, pwd);
    }


    /**
     * 恢复钱包 （第一次使用调用返回5个币）
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param walletName   钱包名称
     * @param mnemonicCode 助记词
     * @param pwd          钱包密码
     * @return 钱包列表
     * @throws Exception 异常处理
     */
    public static WalletBean importMnemonic(String walletName, List<String> mnemonicCode, String pwd) throws Exception {
        return importMnemonic(walletName, mnemonicCode, false, pwd);
    }


    /**
     * 恢复钱包 （第一次使用调用返回5个币）
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param walletName   钱包名称
     * @param mnemonicCode 助记词
     * @param segWit       是否隔离见证 true 隔离 false 普通
     * @param pwd          钱包密码
     * @return 钱包列表
     * @throws Exception 异常处理
     */
    public static WalletBean importMnemonic(String walletName, List<String> mnemonicCode, boolean segWit, String pwd) throws Exception {


        DeterministicSeed deterministicSeed = new DeterministicSeed(Objects.requireNonNull(mnemonicCode), null, "", 0);
        //btc
        DeterministicKeyChain dk = DeterministicKeyChain.builder().seed(deterministicSeed).build();

        // btc 普通地址bip44路径
        List<ChildNumber> btcPath1 = ImmutableList.of(
                new ChildNumber(44, true),
                new ChildNumber(0, true),
                new ChildNumber(0, true),
                new ChildNumber(0, false),
                new ChildNumber(0, false));

        // btc 隔离间见证地址bip49路径
        List<ChildNumber> btcPath3 = ImmutableList.of(
                new ChildNumber(49, true),
                new ChildNumber(0, true),
                new ChildNumber(0, true),
                new ChildNumber(0, false),
                new ChildNumber(0, false));


        DeterministicKey keyByPath = dk.getKeyByPath(segWit ? btcPath3 : btcPath1, true);
        ECKey ecKey = ECKey.fromPrivate(keyByPath.getPrivKey());
        Script script = ScriptBuilder.createP2WPKHOutputScript(keyByPath.getPubKeyHash());


        //eth
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(Objects.requireNonNull(deterministicSeed.getSeedBytes()));
        String[] pathArray = ETH_PATH.split("/");
        for (int i = 1; i < pathArray.length; i++) {
            ChildNumber childNumber;
            if (pathArray[i].endsWith("'")) {
                int number = Integer.parseInt(pathArray[i].substring(0,
                        pathArray[i].length() - 1));
                childNumber = new ChildNumber(number, true);
            } else {
                int number = Integer.parseInt(pathArray[i]);
                childNumber = new ChildNumber(number, false);
            }
            dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
        }
        ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes());
        WalletFile walletFile = Wallet.createLight(pwd, keyPair);
        String privateKeyEth = keyPair.getPrivateKey().toString(16);
        String publicKeyEth = keyPair.getPublicKey().toString(16);
        String keystore = objectMapper.writeValueAsString(walletFile);
        Credentials credentials = Credentials.create(Objects.requireNonNull(privateKeyEth));

        List<WalletBean> jnWallets = new ArrayList<>();

        //公钥生成地址，验证发送交易的地址是否和该公钥生成的地址一致
        //公钥验证私钥的签名，用来验证该交易是否使用了正确的私钥签名
        //私钥生成公钥是成对出现，公钥可以生成对应的唯一地址，这样就能确认了该地址发送的交易是否使用了对应的私钥
//        jnWallets.add(new JnWallet(walletName, segWit ? ecKey.getPrivateKeyEncoded(MainNetParams.get()).toBase58() : ecKey.getPrivateKeyAsWiF(MainNetParams.get()),
//                ecKey.getPublicKeyAsHex(), segWit ? LegacyAddress.fromScriptHash(MainNetParams.get(), Utils.sha256hash160(script.getProgram())).toBase58()
//                : LegacyAddress.fromKey(MainNetParams.get(), ecKey).toBase58()
//                , WalletType.BTC));
//        //       jnWallets.add(new JnWallet(walletName, privateKeyEth, publicKeyEth, credentials.getAddress(), keystore, WalletType.UTG));
//        jnWallets.add(new JnWallet(walletName, privateKeyEth, publicKeyEth, credentials.getAddress(), keystore, WalletType.ETH));
//        jnWallets.add(new JnWallet(walletName, segWit ? ecKey.getPrivateKeyEncoded(MainNetParams.get()).toBase58() : ecKey.getPrivateKeyAsWiF(MainNetParams.get()),
//                ecKey.getPublicKeyAsHex(), segWit ? LegacyAddress.fromScriptHash(MainNetParams.get(), Utils.sha256hash160(script.getProgram())).toBase58()
//                : LegacyAddress.fromKey(MainNetParams.get(), ecKey).toBase58()
//                , WalletType.USDT_OMNI));
//        jnWallets.add(new JnWallet(walletName, privateKeyEth, publicKeyEth, credentials.getAddress(), keystore, WalletType.USDT_ERC20));
//        jnWallets.add(new JnWallet(walletName, privateKeyEth, publicKeyEth, credentials.getAddress(), keystore, WalletType.UTG));

        return new WalletBean();
    }

}
