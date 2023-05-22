package com.meta.zoom.wallet.btc;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.meta.zoom.wallet.bean.BtcFee;
import com.meta.zoom.wallet.bean.BtcTransaction;
import com.meta.zoom.wallet.bean.JnWallet;
import com.meta.zoom.wallet.bean.UtxoBean;
import com.meta.zoom.wallet.bean.WalletType;
import com.meta.zoom.wallet.util.Arithmetic;
import com.meta.zoom.wallet.util.HexUtil;
import com.meta.zoom.wallet.util.HttpUtil;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionWitness;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

/**
 * 比特币钱包
 *
 * @author rainking
 */
public class BtcColdWallet {

    private static final String TAG = "BtcColdWallet";

    //    节点	服务器地址	rpcuser	rpcpassword	rpcport
//    btc	47.90.40.219	a1234567	a1234567	8332
//    omni	47.52.132.55	a1234567	a1234567	8332
    private static final String PROTOCOL = "http";
    private static final String BTC_HOST = "8.210.26.192";
    private static final String OMNI_HOST = "47.242.137.48";
    private static final String PORT = "8332";
    private static final String USER = "LuoGoToWord";
    private static final String PASSWORD = "2019GoodByGeyu";


    /**
     * 加载btc节点配置信息
     *
     * @return JsonRpcHttpClient
     */
    public BitcoinJSONRPCClient initBtcClient() throws Exception {
        return new BitcoinJSONRPCClient(new URL(PROTOCOL + "://" + USER + ':' + PASSWORD + "@" + BTC_HOST + ":" + PORT + "/"));
    }

    /**
     * 加载btc节点配置信息
     *
     * @return JsonRpcHttpClient
     */
    public BitcoinJSONRPCClient initOmniClient() throws Exception {
        return new BitcoinJSONRPCClient(new URL(PROTOCOL + "://" + USER + ':' + PASSWORD + "@" + OMNI_HOST + ":" + PORT + "/"));
    }


    /**
     * 创建BTC
     *
     * @param walletName 钱包名称
     * @param walletType 钱包类型 BTC、USDT_OMNI 只有这2个。非常重要
     * @return JnWallet 具体方法参考该类
     */
    public JnWallet createWallet(String walletName, WalletType walletType) {
        return createCommonWallet(walletName, false, walletType);
    }


    /**
     * 根据私钥导入钱包
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param mnemonicCode 助记词
     * @param walletName   钱包名称
     * @param walletType   钱包类型 BTC、USDT_OMNI 只有这2个。非常重要
     * @return 钱包
     */
    public JnWallet importMnemonic(String walletName, List<String> mnemonicCode, WalletType walletType) {
        return importCommonMnemonic(walletName, mnemonicCode, false, walletType);
    }

    /**
     * 创建BTC
     *
     * @param walletName 钱包名称
     * @param segWit     是否隔离见证 true 隔离 false 普通
     * @param walletType 钱包类型 BTC、USDT_OMNI 只有这2个。非常重要
     * @return JnWallet 具体方法参考该类
     */
    public JnWallet createWallet(String walletName, boolean segWit, WalletType walletType) {
        return createCommonWallet(walletName, segWit, walletType);
    }


    /**
     * 根据私钥导入钱包
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param mnemonicCode 助记词
     * @param walletName   钱包名称
     * @param walletType   钱包类型 BTC、USDT_OMNI 只有这2个。非常重要
     * @return 钱包
     */
    public JnWallet importMnemonic(String walletName, List<String> mnemonicCode, boolean segWit, WalletType walletType) {
        return importCommonMnemonic(walletName, mnemonicCode, segWit, walletType);
    }


    /**
     * 根据私钥导入钱包BTC
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param privateKey 私钥
     * @param walletName 钱包名称
     * @param walletType 钱包类型 BTC、USDT_OMNI 只有这2个。非常重要
     * @return 钱包
     */
    public JnWallet importPrivateKey(String walletName, String privateKey, WalletType walletType) {
        String s = Arithmetic.encodeHex(Base58.decode(privateKey));
        String prk = s.substring(2, s.length() - 10);
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(prk, 16));
        LegacyAddress legacyAddress = LegacyAddress.fromKey(MainNetParams.get(), ecKey);
        return new JnWallet(walletName, privateKey, ecKey.getPublicKeyAsHex(), legacyAddress.toBase58(), walletType);
    }

    /**
     * 根据私钥导入钱包BTC(隔离见证导入)
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param privateKey 私钥
     * @param walletName 钱包名称
     * @param walletType 钱包类型 BTC、USDT_OMNI 只有这2个。非常重要
     * @return 钱包
     */
    public JnWallet importPrivate3Key(String walletName, String privateKey, WalletType walletType) {
        String s = Arithmetic.encodeHex(Base58.decode(privateKey));
        String prk = s.substring(2, s.length() - 10);
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(prk, 16));
        Script script = ScriptBuilder.createP2WPKHOutputScript(ecKey.getPubKeyHash());
        return new JnWallet(walletName, privateKey, ecKey.getPublicKeyAsHex(), LegacyAddress.fromScriptHash(MainNetParams.get(), Utils.sha256hash160(script.getProgram())).toBase58(), walletType);
    }

    /**
     * 获取余额 此方法是获取BTC的余额
     *
     * @param utxos Utxos 是针对BTC
     * @return 余额
     */
    public BigDecimal getBalance(List<UTXO> utxos) throws Exception {
        if (utxos == null || utxos.size() == 0) {
            return new BigDecimal("0");
        }
        long balance = 0L;
        for (UTXO bean : utxos) {
            balance += bean.getValue().getValue();
        }
        BigDecimal value = new BigDecimal(balance);
        return value.divide(new BigDecimal("100000000"), 8, RoundingMode.HALF_UP);
    }


    /**
     * 获取余额 此方法是获取BTC 代币的余额 获取OMNI 余额
     *
     * @param fromAddress 查询地址 是针对USDT-OMNI
     * @return 余额
     */
    public BigDecimal getTokenBalance(String fromAddress) throws Exception {
        Map<String, Object> map = (Map<String, Object>) initOmniClient().query("omni_getbalance", fromAddress, 31);
        return new BigDecimal(Objects.requireNonNull(map.get("balance")).toString());
    }


    /**
     * @param walletName 钱包名称
     * @param segWit     是否隔离见证 true 隔离 false 普通
     * @param walletType 钱包类型
     * @return 钱包
     */
    private JnWallet createCommonWallet(String walletName, boolean segWit, WalletType walletType) {
        // 种子
        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "");
        //助记词
        return importCommonMnemonic(walletName, ds.getMnemonicCode(), segWit, walletType);
    }


    /**
     * 在 IMToken 使用时地址类型 选择普通地址
     *
     * @param mnemonicCode 助记词
     * @param segWit       是否隔离见证 true 隔离 false 普通
     * @param walletName   钱包名称
     * @return 钱包
     */
    private JnWallet importCommonMnemonic(String walletName, List<String> mnemonicCode, boolean segWit, WalletType walletType) {
        DeterministicSeed deterministicSeed = new DeterministicSeed(Objects.requireNonNull(mnemonicCode), null, "", 0);
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

        return new JnWallet(walletName, mnemonicCode, segWit ? ecKey.getPrivateKeyEncoded(MainNetParams.get()).toBase58() : ecKey.getPrivateKeyAsWiF(MainNetParams.get())
                , ecKey.getPublicKeyAsHex(), segWit ? LegacyAddress.fromScriptHash(MainNetParams.get(), Utils.sha256hash160(script.getProgram())).toBase58()
                : LegacyAddress.fromKey(MainNetParams.get(), ecKey).toBase58(), walletType);
    }


    /**
     * 验证BTC 、OMNI地址是否正确
     * 用于转账时验证输入BTC 地址是否正确
     *
     * @param address    地址
     * @param walletType 钱包地址
     * @return 是否正确
     */
    public boolean isValidateAddress(String address, WalletType walletType) throws Exception {
        BitcoindRpcClient.AddressValidationResult addressValidationResult = null;
        if (WalletType.USDT_OMNI.getType().equals(walletType.getType())) {
            addressValidationResult = initBtcClient().validateAddress(address);
        } else if (WalletType.BTC.getType().equals(walletType.getType())) {
            addressValidationResult = initOmniClient().validateAddress(address);
        }
        return Objects.requireNonNull(addressValidationResult).isValid();
    }

    /***
     * 第三方获取未消费列表
     * @param model ：地址
     * @return List<UTXO>
     */
    public List<UTXO> getUnspentTransactionOutput(List<UtxoBean> model, String script) {
        List<UTXO> utxos = Lists.newArrayList();
        if (model != null && model.size() > 0) {
            for (UtxoBean bean : model) {
                BigDecimal decimal = new BigDecimal(bean.getValue());
                long value = decimal.multiply(new BigDecimal("100000000")).longValue();
                UTXO utxo = new UTXO(Sha256Hash.wrap(bean.getTxid()), bean.getOutput_no(),
                        Coin.valueOf(value),
                        0, false, new Script(Hex.decode(script)));
                utxos.add(utxo);
            }
        }
        return utxos;

    }


    /***
     * 第三方获取未消费列表
     * @param address ：地址
     * @return List<UTXO>
     */
    public List<UTXO> getUnspentTransactionOutput(String address) throws Exception {
        List<UTXO> utxos = Lists.newArrayList();
        String httpGet = HttpUtil.getInstance().getRandom("https://blockchain.info/zh-cn/unspent?active=" + address);
        if (Objects.equals("No free outputs to spend", httpGet)) {
            return utxos;
        }
        JSONObject jsonObject = JSON.parseObject(httpGet);
        JSONArray unspentOutputs = jsonObject.getJSONArray("unspent_outputs");
        List<Map> outputs = JSONObject.parseArray(unspentOutputs.toJSONString(), Map.class);
        if (outputs == null || outputs.size() == 0) {
            System.out.println("交易异常，余额不足");
        }
        for (int i = 0; i < outputs.size(); i++) {
            Map outputsMap = outputs.get(i);
            System.out.println(JSON.toJSONString(outputsMap));
            String tx_hash = Objects.requireNonNull(outputsMap.get("tx_hash")).toString();
            String tx_hash_big_endian = Objects.requireNonNull(outputsMap.get("tx_hash_big_endian")).toString();
            String tx_index = Objects.requireNonNull(outputsMap.get("tx_index")).toString();
            String tx_output_n = Objects.requireNonNull(outputsMap.get("tx_output_n")).toString();
            String script = Objects.requireNonNull(outputsMap.get("script")).toString();
            String value = Objects.requireNonNull(outputsMap.get("value")).toString();
            String value_hex = Objects.requireNonNull(outputsMap.get("value_hex")).toString();
            String confirmations = Objects.requireNonNull(outputsMap.get("confirmations")).toString();
            UTXO utxo = new UTXO(Sha256Hash.wrap(tx_hash_big_endian), Long.parseLong(tx_output_n), Coin.valueOf(Long.parseLong(value)),
                    0, false, new Script(Hex.decode(script)));
            utxos.add(utxo);
        }
        return utxos;

    }

    /***
     * 第三方获取未消费列表
     * @param address ：地址
     * @return List<UTXO>
     */
    public List<UTXO> getUnspentTransactionOutput1(String address) throws Exception {
        List<UTXO> utxos = Lists.newArrayList();
        JSONObject object = new JSONObject();
//        object.put("coinAddress", address);
//        object.put("coinName", "BTC");
        //TODO 临时固定的
 //       address = "39dk7jM6HzQ8aYr8CwC4XSTNke8kXDJaQY";
        String httpGet = HttpUtil.getInstance().get("https://blockchain.info/unspent?active=" + address);
        if (Objects.equals("No free outputs to spend", httpGet)) {
            return utxos;
        }
        JSONObject jsonObject = JSON.parseObject(httpGet);
        JSONArray unspentOutputs = jsonObject.getJSONArray("unspent_outputs");
        if (unspentOutputs == null) {
            return utxos;
        }
        List<Map> outputs = JSONObject.parseArray(unspentOutputs.toJSONString(), Map.class);
        if (outputs == null || outputs.size() == 0) {
            Log.e("aaaaaaaa", "交易异常，余额不足");
        }
        for (int i = 0; i < outputs.size(); i++) {
            Map outputsMap = outputs.get(i);
            Log.e("aaaaaaaa",JSON.toJSONString(outputsMap));
            String tx_hash_big_endian = Objects.requireNonNull(outputsMap.get("tx_hash_big_endian")).toString();
            String tx_output_n = Objects.requireNonNull(outputsMap.get("tx_output_n")).toString();
            String script = Objects.requireNonNull(outputsMap.get("script")).toString();
            String value = Objects.requireNonNull(outputsMap.get("value")).toString();
            UTXO utxo = new UTXO(Sha256Hash.wrap(tx_hash_big_endian), Long.parseLong(tx_output_n), Coin.valueOf(Long.parseLong(value)),
                    0, false, new Script(Hex.decode(script)));
            utxos.add(utxo);
        }
        return utxos;

    }


    /**
     * 转换余额
     *
     * @param amount 金额
     * @return 金额(Long)
     */
    private Long signAmount(String amount) {
        BigDecimal bigDecimalAmount = new BigDecimal(amount).multiply(new BigDecimal("100000000"));
        return bigDecimalAmount.longValue();
    }

    /**
     * 离线签名
     *
     * @param fromAddress 发送地址
     * @param toAddress   接收地址
     * @param privateKey  私钥
     * @param amount      转账金额(转账多少BTC)
     * @param fee         手续费
     * @param utXoList    未花费输出
     * @param segWit      是否隔离见证 true 隔离 false 普通
     * @param walletType  类型
     * @return BtcTransaction
     */
    private BtcTransaction sign(String fromAddress, String toAddress, String privateKey, String amount, Long fee, List<UTXO> utXoList, boolean segWit, WalletType walletType) throws Exception {
        Long transferAmount = signAmount(amount);
        if (utXoList == null || utXoList.size() == 0) {
            throw new Exception("utXoList为空(未消费列表为空)");
        }
        Transaction tran = new Transaction(MainNetParams.get());
        Long miniBtc = 546L;
        if (WalletType.USDT_OMNI.getType().equals(walletType.getType())) {
            //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
            tran.addOutput(Coin.valueOf(miniBtc), LegacyAddress.fromBase58(MainNetParams.get(), toAddress));
            //构建usDt的输出脚本 注意这里的金额是要乘10的8次方  propertyId ：  usDt默认为31即可
            String usDtHex = "6a146f6d6e69" + String.format("%016x", 31) + String.format("%016x", transferAmount);
            tran.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usDtHex)));
        }
        //如果有找零就添加找零
        long utXoAmount = 0L;
        long changeAmount = 0L;
        List<UTXO> needUtXo = new ArrayList<>();
        if (WalletType.USDT_OMNI.getType().equals(walletType.getType())) {
            for (UTXO utxo : utXoList) {
                if (utXoAmount > (fee + miniBtc)) {
                    break;
                } else {
                    needUtXo.add(utxo);
                    utXoAmount += utxo.getValue().value;
                }
            }
            changeAmount = utXoAmount - (fee + miniBtc);
        } else if (WalletType.BTC.getType().equals(walletType.getType())) {
            //遍历未花费列表，组装合适的item
            for (UTXO utxo : utXoList) {
                if (utXoAmount >= (transferAmount + fee)) {
                    break;
                } else {
                    utXoList.add(utxo);
                    utXoAmount += utxo.getValue().value;
                }
            }
            // 添加未输出列表到交易中
            tran.addOutput(Coin.valueOf(transferAmount), LegacyAddress.fromBase58(MainNetParams.get(), toAddress));
            //消费列表总金额 - 已经转账的金额 - 手续费 就等于需要返回给自己的金额了
            changeAmount = utXoAmount - (transferAmount + fee);

        }
        //余额判断
        if (changeAmount < 0) {
            throw new Exception("余额不足");
        }

        if (changeAmount > 0) {
            //输出-转给自己(找零) 默认为from地址 可改
            tran.addOutput(Coin.valueOf(changeAmount), LegacyAddress.fromBase58(MainNetParams.get(), fromAddress));
        }


        if (segWit) {
            ECKey ecKey = ECKey.fromPrivate(Numeric.hexStringToByteArray(privateKey));
            Script script = ScriptBuilder.createP2WPKHOutputScript(ecKey);
            System.out.println("script:" + script.toString());
            if (WalletType.USDT_OMNI.getType().equals(walletType.getType())) {
                //输入未消费列表项
                for (UTXO utxo : needUtXo) {
                    //私钥转换为ECKey密钥对
                    tran.addInput(utxo.getHash(), utxo.getIndex(), script);
                }
                //签名交易，构建脚本
                for (int i = 0; i < tran.getInputs().size(); i++) {
                    Script redeemScript = ScriptBuilder.createP2WPKHOutputScript(ecKey);
                    Script scriptCode = new ScriptBuilder().data(ScriptBuilder.createP2PKHOutputScript(ecKey).getProgram()).build();
                    TransactionSignature txSig = tran.calculateWitnessSignature(i,
                            ecKey,
                            scriptCode,
                            Coin.valueOf(utXoAmount),
                            Transaction.SigHash.ALL, false);
                    tran.getInput(i).setWitness(TransactionWitness.redeemP2WPKH(txSig, ecKey));
                    tran.getInput(i).setScriptSig(new ScriptBuilder().data(redeemScript.getProgram()).build());
                }
            } else if (WalletType.BTC.getType().equals(walletType.getType())) {
                //输入未消费列表项
                for (UTXO utxo : needUtXo) {
                    //私钥转换为ECKey密钥对
                    tran.addInput(utxo.getHash(), utxo.getIndex(), script);
                }
                //签名交易，构建脚本
                for (int i = 0; i < tran.getInputs().size(); i++) {
                    //私钥转换为ECKey密钥对
                    Script redeemScript = ScriptBuilder.createP2WPKHOutputScript(ecKey);
                    Script scriptCode = new ScriptBuilder().data(ScriptBuilder.createP2PKHOutputScript(ecKey).getProgram()).build();
                    TransactionSignature txSig = tran.calculateWitnessSignature(i,
                            ecKey,
                            scriptCode,
                            Coin.valueOf(utXoAmount),
                            Transaction.SigHash.ALL, false);
                    tran.getInput(i).setWitness(TransactionWitness.redeemP2WPKH(txSig, ecKey));
                    tran.getInput(i).setScriptSig(new ScriptBuilder().data(redeemScript.getProgram()).build());
                }
            }
        } else {
            ECKey ecKey = DumpedPrivateKey.fromBase58(MainNetParams.get(), privateKey).getKey();
            if (WalletType.USDT_OMNI.getType().equals(walletType.getType())) {
                //先添加未签名的输入，也就是utXo
                for (UTXO utxo : needUtXo) {
                    tran.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript()).setSequenceNumber(TransactionInput.NO_SEQUENCE - 2);
                }
                //下面就是签名
                for (int i = 0; i < needUtXo.size(); i++) {
                    TransactionInput transactionInput = tran.getInput(i);
                    Script scriptPubKey = ScriptBuilder.createOutputScript(LegacyAddress.fromBase58(MainNetParams.get(), fromAddress));
                    Sha256Hash hash = tran.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
                    ECKey.ECDSASignature ecSig = ecKey.sign(hash);
                    TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
                    transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
                }
            } else if (WalletType.BTC.getType().equals(walletType.getType())) {
                //输入未消费列表项
                for (UTXO utxo : needUtXo) {
                    TransactionOutPoint outPoint = new TransactionOutPoint(MainNetParams.get(), utxo.getIndex(), utxo.getHash());
                    tran.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
                }
            }
        }
        System.out.println("手续费:" + fee);
        System.out.println("utXoAmount:" + utXoAmount);
        System.out.println("changeAmount:" + changeAmount);

        //这是签名之后的原始交易，直接去广播就行了
        String signedHex = HexUtil.encodeHexStr(tran.bitcoinSerialize());
        System.out.println("signedHex:" + changeAmount);
        //这是交易的hash
        String txHash = HexUtil.encodeHexStr(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(tran.bitcoinSerialize()))));
        System.out.println("signedHex:" + txHash);
        // sendRawTransaction广播方法 自己节点 用这个广播
        // 付费节点用这个广播 https://wallet.tokenview.com/onchainwallet/{币种简称小写}
        //再调用光广播交易接口广播sing接口，广播后会返回txId
        String hash = null;
        if (WalletType.USDT_OMNI.getType().equals(walletType.getType())) {
            hash = initOmniClient().sendRawTransaction(signedHex);
        } else if (WalletType.BTC.getType().equals(walletType.getType())) {
            hash = initBtcClient().sendRawTransaction(signedHex);
        }
        return new BtcTransaction(txHash, hash);
    }


    /**
     * 获取矿工费用
     * 148 * 输入数额 + 34 * 输出数额 + 10
     *
     * @param utXos   余额
     * @param feeRate 手续费
     * @return Long
     */
    public Long getFee(List<UTXO> utXos, Long feeRate) throws Exception {
        if (utXos == null || utXos.size() == 0) {
            throw new Exception("utXos为空(未消费列表为空)");
        }
        long miniBtc = 546L;
        long utXoAmount = 0L;
        long fee = 0L;
        long utXoSize = 0L;
        for (UTXO output : utXos) {
            utXoSize++;
            if (utXoAmount > (fee + miniBtc)) {
                break;
            } else {
                utXoAmount += output.getValue().value;
                //btc手续费的公式
                fee = (148 * utXoSize + 34 * 2 + 10) * feeRate;
            }
        }
        return fee;
    }


    /**
     * 获取btc费率
     *
     * @return Long
     */
    public BtcFee getBtcFee() throws Exception {
        String data = HttpUtil.getInstance().get("https://bitcoinfees.earn.com/api/v1/fees/recommended");
        return JSON.parseObject(data, BtcFee.class);
    }


    /**
     * 根据行情计算价格
     *
     * @param sat    旷工费
     * @param price  行情价格（BTC、OMNI） 人民币或者美元
     * @param remark $ ￥ 2种符号
     * @return 行情价格
     * @throws Exception 异常
     */
    public String getBtcPrecisionPrice(List<UTXO> utXos, long sat, float price, String remark) throws Exception {
        BigDecimal bi1 = new BigDecimal(getFee(utXos, sat));
        BigDecimal bi2 = new BigDecimal("100000000");
        BigDecimal value = bi1.divide(bi2, 8, RoundingMode.HALF_UP);
        float money = (value.multiply(new BigDecimal(price))).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return value + " BTC ≈ " + remark + money;
    }


    /**
     * @param fromAddress 发送地址
     * @param toAddress   接收地址
     * @param privateKey  私钥
     * @param amount      转账金额(转账多少BTC)
     * @param fee         手续费(1-21 sat/b)
     * @param utXos       未花费输出
     * @return BtcTransaction
     * @throws Exception
     */
    public BtcTransaction sendTokenTransfer(String fromAddress, String toAddress, Long fee, String privateKey, String amount, List<UTXO> utXos) throws Exception {
        return sign(fromAddress, toAddress, privateKey, amount, getFee(utXos, fee), utXos, false, WalletType.USDT_OMNI);
    }

    /**
     * @param fromAddress 发送地址
     * @param toAddress   接收地址
     * @param privateKey  私钥
     * @param amount      转账金额(转账多少BTC)
     * @param fee         手续费(1-21 sat/b)
     * @param utXos       未花费输出
     * @return BtcTransaction
     * @throws Exception
     */
    public BtcTransaction sendBtcTransfer(String fromAddress, String toAddress, Long fee, String privateKey, String amount, List<UTXO> utXos) throws Exception {
        return sign(fromAddress, toAddress, privateKey, amount, getFee(utXos, fee), utXos, false, WalletType.BTC);
    }


    /**
     * @param fromAddress 发送地址
     * @param toAddress   接收地址
     * @param privateKey  私钥
     * @param amount      转账金额(转账多少BTC)
     * @param fee         手续费(1-21 sat/b)
     * @param segWit      是否隔离见证 true 隔离 false 普通
     * @param utXos       未花费输出
     * @return BtcTransaction
     * @throws Exception
     */
    public BtcTransaction sendTokenTransfer(String fromAddress, String toAddress, Long fee, String privateKey, String amount, boolean segWit, List<UTXO> utXos) throws Exception {
        return sign(fromAddress, toAddress, privateKey, amount, getFee(utXos, fee), utXos, segWit, WalletType.USDT_OMNI);
    }

    /**
     * @param fromAddress 发送地址
     * @param toAddress   接收地址
     * @param privateKey  私钥
     * @param amount      转账金额(转账多少BTC)
     * @param fee         手续费(1-21 sat/b)
     * @param segWit      是否隔离见证 true 隔离 false 普通
     * @param utXos       未花费输出
     * @return BtcTransaction
     * @throws Exception
     */
    public BtcTransaction sendBtcTransfer(String fromAddress, String toAddress, Long fee, String privateKey, String amount, boolean segWit, List<UTXO> utXos) throws Exception {
        return sign(fromAddress, toAddress, privateKey, amount, getFee(utXos, fee), utXos, segWit, WalletType.BTC);
    }


}
