package com.meta.zoom.wallet.eth;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.common.lib.bean.WalletBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.zoom.wallet.bean.EthDealDetails;
import com.meta.zoom.wallet.bean.EthFee;
import com.meta.zoom.wallet.bean.EthSignature;
import com.meta.zoom.wallet.bean.JnWallet;
import com.meta.zoom.wallet.bean.WalletType;
import com.meta.zoom.wallet.util.HttpUtil;
import com.meta.zoom.wallet.util.Util;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

/**
 * ETH 离线钱包
 *
 * @author rainking
 */
public class EthColdWallet {


    private Web3j web3j;

    /**
     * 通用的以太坊基于bip44协议的助记词路径
     */
    public final static String ETH_PATH = "m/44'/60'/0'/0/0";

    private static final long ETH_GAS_LIMIT = 21000;
    private static final long ALSC_GAS_LIMIT = 60000;
    private static final long TOKEN_GAS_LIMIT = 100000;


//      try {
//       导入相关加这个验证
//    } catch (CipherException e) {
//        if ("Invalid password provided".equals(e.getMessage())) {
//            //密码错误
//        }
//        e.printStackTrace();
//    }

    public EthColdWallet(String rpcUrl) {
        web3j = Web3j.build(new HttpService(rpcUrl));
    }

    /**
     * 创建钱包
     *
     * @param walletName 钱包名称
     * @param password   密码
     * @param walletType 钱包类型 ETH、USDT-ERC20、ALSC 只有这3个。非常重要
     * @return JnWallet
     * @throws Exception 异常处理
     */
    public JnWallet createWallet(String walletName, String password, WalletType walletType) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        String[] pathArray = ETH_PATH.split("/");
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "");
        return createCommonWallet(walletName, ds, pathArray, password, walletType);
    }


    /**
     * 根据助记词导入钱包
     *
     * @param walletName   钱包名称
     * @param mnemonicCode 助记词
     * @param password     密码
     * @param walletType   钱包类型 ETH、USDT-ERC20、ALSC 只有这3个(地址都一样)。非常重要
     * @return JnWallet
     * @throws Exception 异常处理
     */
    public JnWallet importMnemonic(String walletName, List<String> mnemonicCode, String password, WalletType walletType) throws Exception {
        String[] pathArray = ETH_PATH.split("/");
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(mnemonicCode, null, passphrase, creationTimeSeconds);
        return createCommonWallet(walletName, ds, pathArray, password, walletType);
    }

    /**
     * 根据私钥导入钱包ETH
     *
     * @param privateKey 私钥
     * @param walletName 钱包名称
     *                   //   * @param walletType 钱包类型 ETH、USDT-ERC20、ALSC 只有这3个(地址都一样)。非常重要
     * @return 钱包
     */
    public WalletBean importPrivateKey(String walletName, String password, String privateKey) throws Exception {
        Credentials credentials = Credentials.create(privateKey);
        WalletFile walletFile = Wallet.createLight(password, credentials.getEcKeyPair());
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        String keystore = objectMapper.writeValueAsString(walletFile);
        ECKeyPair ecKeyPair = Wallet.decrypt(password, walletFile);
        String publicKey = ecKeyPair.getPublicKey().toString(16);
        return new WalletBean();
    }


    /**
     * 根据keystore 导入钱包
     *
     * @param walletName 钱包名称
     * @param password   密码
     * @param keystore   keystore
     *                   //   * @param walletType 钱包类型 ETH、USDT-ERC20、ALSC 只有这3个(地址都一样)。非常重要
     * @return 钱包
     * @throws Exception Exception
     */

    public WalletBean importKeystore(String walletName, String password, Object keystore) throws Exception {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        WalletFile walletFile = objectMapper.readValue(URLDecoder.decode(String.valueOf(keystore), "UTF-8"), WalletFile.class);
        ECKeyPair ecKeyPair = Wallet.decrypt(password, walletFile);
        String privateKey = ecKeyPair.getPrivateKey().toString(16);
        String publicKey = ecKeyPair.getPublicKey().toString(16);
        Credentials credentials = Credentials.create(Objects.requireNonNull(privateKey));

        return new WalletBean();
    }

    /**
     * 创建钱包通用
     *
     * @param walletName 钱包名称
     * @param ds         DeterministicSeed
     * @param pathArray  pathArray
     * @param password   密码
     * @param walletType 钱包类型 ETH、USDT-ERC20、ALSC 只有这3个(地址都一样)。非常重要
     * @return JnWallet
     * @throws Exception 异常处理
     */
    private JnWallet createCommonWallet(String walletName, DeterministicSeed ds, String[]
            pathArray, String password, WalletType walletType) throws Exception {
        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(Objects.requireNonNull(ds.getSeedBytes()));
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
        String privateKey = keyPair.getPrivateKey().toString(16);
        String publicKey = keyPair.getPublicKey().toString(16);
        WalletFile walletFile = Wallet.createLight(password, keyPair);
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        String keystore = objectMapper.writeValueAsString(walletFile);
        Credentials credentials = Credentials.create(Objects.requireNonNull(privateKey));
        return new JnWallet(walletName, ds.getMnemonicCode(), privateKey, publicKey, credentials.getAddress(), keystore, walletType);
    }


//// ETH地址合法校验
//if (!(preg_match('/^(0x)?[0-9a-fA-F]{40}$/', $address))) {
//        return false; //满足if代表地址不合法
//    }
//
//// BTC地址合法校验
//if (!(preg_match('/^(1|3)[a-zA-Z\d]{24,33}$/', $address) && preg_match('/^[^0OlI]{25,34}$/', $address))) {
//        return false; //满足if代表地址不合法
//    }

    /**
     * 验证ETH 、USDT-ERC20、ALSC地址是否正确
     * 用于转账时验证输入eth 地址是否正确
     *
     * @param input 地址
     * @return 是否正确
     */
    public boolean isValidateAddress(String input) {
        if (Util.isEmpty(input) || !input.startsWith("0x")) {
            return false;
        }
        String cleanInput = Numeric.cleanHexPrefix(input);
        try {
            Numeric.toBigIntNoPrefix(cleanInput);
        } catch (NumberFormatException e) {
            return false;
        }
        return cleanInput.length() == 40;
    }


    /**
     * 获取余额 此方法是获取以太坊的余额
     *
     * @param fromAddress 查询地址 是针对ETH
     * @return 余额
     */
    public BigDecimal getBalance(String fromAddress) throws Exception {
        if (!isValidateAddress(fromAddress)) {
            throw new Exception("钱包地址错误");
        }
        EthGetBalance ethGetBalance = web3j.ethGetBalance(fromAddress, DefaultBlockParameterName.LATEST).send();
        BigInteger balance = ethGetBalance.getBalance();
        System.out.println(balance);
        BigDecimal fromWei = Convert.fromWei(Objects.requireNonNull(balance).toString(), Convert.Unit.ETHER);
        System.out.println("address " + fromAddress + " balance " + balance + "wei");
        System.out.println(fromWei);
        return fromWei;
    }


    /**
     * 查询代币余额
     *
     * @param fromAddress     查询地址
     * @param contractAddress 合约地址 参考这个 ContractType 是针对这个2个钱包 USDT-ERC20、ALSC
     * @return 余额
     */
    public BigDecimal getTokenBalance(String fromAddress, String contractAddress) throws Exception {
        if (!isValidateAddress(fromAddress)) {
            throw new Exception("钱包地址错误");
        }
        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address address = new Address(fromAddress);
        inputParameters.add(address);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);
        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        BigInteger balanceValue;
        if (results != null && results.size() > 0) {
            balanceValue = (BigInteger) results.get(0).getValue();
        } else {
            balanceValue = BigInteger.valueOf(0);
        }
        //根据合约地址查询代币精度
        int tokenDecimals = getTokenDecimals(contractAddress, fromAddress);
        //得到余额


        BigDecimal fromWei = getDeci(tokenDecimals, balanceValue);

        Log.i("ETHColdWallet", "address " + address + " balance " + balanceValue + "wei " + fromWei);
        return fromWei;
    }


    /**
     * ETH转账
     *
     * @param privateKey    私钥
     * @param fromAddress   发送地址
     * @param toAddress     接收地址
     * @param gasPriceParam 手续费
     * @param amount        金额
     * @return {\"txhash\":\"交易hash\",\"nonce\":\"交易随机数\"}
     * @throws Exception 异常
     */
    public EthSignature sendEthTransfer(String privateKey, String fromAddress, String toAddress, String gasPriceParam, String amount) throws Exception {

        if (Util.isEmpty(privateKey)) {
            throw new Exception("privateKey不能为空");
        }
        if (Util.isEmpty(fromAddress)) {
            throw new Exception("fromAddress不能为空");
        }
        if (Util.isEmpty(toAddress)) {
            throw new Exception("toAddress不能为空");
        }
        if (Util.isEmpty(gasPriceParam)) {
            throw new Exception("gasPrice不能为空");
        }
        if (Util.isEmpty(amount)) {
            throw new Exception("amount不能为空");
        }

        // 下面开始转账的操作流程
        // 交易序号 这个交易序号需要自己维护，如果交易待打包状态，或者需要重新发起，那么需要nonce +1 不要问为什么，规定
        BigInteger nonce;
        // 交易对象

        // 初始化交易对象 参数是转出地址
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).send();
        if (ethGetTransactionCount == null) {
            throw new Exception("交易失败");
        }
        //获取交易序号
        nonce = ethGetTransactionCount.getTransactionCount();
        //设置交易条件
        BigInteger gasPrice = new BigInteger(gasPriceParam + "000000000");
        // 消耗总量限制，用来限制区块能包含的交易信息总和

        BigInteger gasLimit = BigInteger.valueOf(ETH_GAS_LIMIT);

        //收款人地址
        String to = toAddress.toLowerCase();
        //设置wei 设置转账金额
        BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
        String data = "";
        //匹配对应的网络类型//测试网络 可以直接为none
        //byte chainId = ChainId.ROPSTEN;
        byte chainId = ChainId.NONE;

        String transactionHash = null;
        try {
            //加密数据
            String signedData = signTransaction(nonce, gasPrice, gasLimit, to, value, data, chainId, privateKey);
            System.out.println("signedData-:" + signedData);
            if (!Util.isEmpty(signedData)) {
                //把交易结果广播出去
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                Response.Error error = ethSendTransaction.getError();
                if (error != null) {
                    throw new Exception(error.getMessage());
                }
                transactionHash = ethSendTransaction.getTransactionHash();
                System.out.println("transactionHash-:" + transactionHash);
            } else {
                throw new Exception("交易失败-签名失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EthSignature(transactionHash, String.valueOf(nonce));
    }


    /**
     * 代币转账
     *
     * @param privateKey      私钥
     * @param fromAddress     发送地址
     * @param toAddress       接收地址
     * @param gasPriceParam   手续费
     * @param amount          金额
     * @param contractAddress 合约地址
     * @return {\"txhash\":\"交易hash\",\"nonce\":\"交易随机数\"}
     * @throws Exception 异常
     */
    public EthSignature sendTokenTransfer(String privateKey, String fromAddress, String toAddress, String gasPriceParam, String amount, String contractAddress) throws Exception {

        if (Util.isEmpty(privateKey)) {
            throw new Exception("privateKey不能为空");
        }
        if (Util.isEmpty(fromAddress)) {
            throw new Exception("fromAddress不能为空");
        }
        if (Util.isEmpty(toAddress)) {
            throw new Exception("toAddress不能为空");
        }
        if (Util.isEmpty(gasPriceParam)) {
            throw new Exception("gasPrice不能为空");
        }
        if (Util.isEmpty(amount)) {
            throw new Exception("amount不能为空");
        }
        if (Util.isEmpty(contractAddress)) {
            throw new Exception("contractAddress不能为空");
        }

        //下面开始转账的操作流程
        BigInteger nonce;

        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).send();

        if (ethGetTransactionCount == null) {
            throw new Exception("交易失败");
        }
        nonce = ethGetTransactionCount.getTransactionCount();


        BigInteger gasPrice = new BigInteger(gasPriceParam + "000000000");

        BigInteger gasLimit = BigInteger.valueOf(TOKEN_GAS_LIMIT);

        BigInteger value = BigInteger.ZERO;
        //token转账参数
        String methodName = "transfer";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address tAddress = new Address(toAddress);
        int decimals = getTokenDecimals(contractAddress, fromAddress);//精度 根据合约地址查询精度
        Uint256 tokenValue = new Uint256(new BigDecimal(amount).multiply(BigDecimal.TEN.pow(decimals)).toBigInteger());
        inputParameters.add(tAddress);
        inputParameters.add(tokenValue);
        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);
        // inputParameters  outputParameters
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        byte chainId = ChainId.NONE;
        String signedData;
        String transactionHash = null;
        try {
            signedData = signTransaction(nonce, gasPrice, gasLimit, contractAddress, value, data, chainId, privateKey);
            System.out.println("signedData-:" + signedData);
            if (!Util.isEmpty(signedData)) {
                //这步就是把我们的节点服务器上产生的交易广播出去
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                //交易成功！，返回交易ID hash值
                Response.Error error = ethSendTransaction.getError();
                if (error != null) {
                    throw new Exception(error.getMessage());
                }
                transactionHash = ethSendTransaction.getTransactionHash();
                System.out.println("transactionHash-:" + transactionHash);
            } else {
                throw new Exception("交易失败-签名失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EthSignature(transactionHash, String.valueOf(nonce));
    }


    /**
     * 签名交易
     */
    private String signTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
                                   BigInteger value, String data, byte chainId, String privateKey) {
        byte[] signedMessage;
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        //利用私钥进行加密
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);
        if (chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }
        return Numeric.toHexString(signedMessage);
    }


    /**
     * 通过txhash判断交易是否成功
     *
     * @return 是否交易成功
     * @throws Exception 异常
     */

    public boolean isTransactionSuccess(String txHash) throws Exception {
        boolean result = false;
        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();
        if (receipt != null && receipt.getResult() != null) {
            String statusStr = receipt.getResult().getStatus();
            if (!Util.isEmpty(statusStr)) {
                int status = Integer.parseInt(statusStr.substring(statusStr.length() - 1));
                if (status == 1) {
                    result = true;
                }
            }
        } else {
            throw new Exception("EthGetTransactionReceipt null");
        }
        System.out.println(result);
        return result;
    }


    /***
     查询旷工费范围的接口,可有fastest、fast、average、safeLow选择
     * https://docs.ethgasstation.info/
     * 注意:要将提供的值转换为gwei,请除以10
     * @return EthgasAPI
     * @throws Exception 异常
     */
    public EthFee getEthFee() throws Exception {
        String data = HttpUtil.getInstance().get("https://ethgasstation.info/json/ethgasAPI.json");
        return JSON.parseObject(data, EthFee.class);
    }


    /**
     * 获取当前以太坊网络中最近一笔交易的gasPrice (最近一次手续费-旷工费)
     *
     * @return BigDecimal
     * @throws Exception 异常
     */
    public BigDecimal latelyCurrentGasPrice() throws Exception {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        BigDecimal v1 = new BigDecimal(String.valueOf(ethGasPrice.getGasPrice()));
        BigDecimal v2 = new BigDecimal("1000000000");
        return v1.divide(v2, 0, RoundingMode.HALF_UP);
    }

    /**
     * 交易监听
     */
    public interface ListeningTransaction {
        /**
         * 错误
         *
         * @param throwable Throwable
         */
        void onError(Throwable throwable);

        /**
         * 交易数据
         *
         * @param ethDealDetails EthDealDetails
         */
        void onData(EthDealDetails ethDealDetails);
    }

    /**
     * 交易监听
     *
     * @param listeningTransaction ListeningTransaction
     */
    public void setOnListeningTransaction(ListeningTransaction listeningTransaction) throws Exception {
        if (listeningTransaction != null) {

            Disposable transactionDisposable = web3j.transactionFlowable().subscribe(transaction -> {
                setTransaction(transaction, listeningTransaction);
            }, listeningTransaction::onError);
            Disposable pendingTransactionDisposable = web3j.pendingTransactionFlowable().subscribe(transaction -> {
                setTransaction(transaction, listeningTransaction);
            }, listeningTransaction::onError);

            Disposable blockDisposable = web3j.blockFlowable(false).subscribe(block -> {

            });
        }
    }


    private void setTransaction(org.web3j.protocol.core.methods.response.Transaction transaction, ListeningTransaction listeningTransaction) {
        String hash = transaction.getHash();
        BigInteger nonce = transaction.getNonce();
        String blockHash = transaction.getBlockHash();
        BigInteger blockNumber = transaction.getBlockNumber();
        BigInteger transIndex = new BigInteger(transaction.getTransactionIndex().toString());
        String from = transaction.getFrom();
        String to = transaction.getTo();
        BigInteger value = transaction.getValue();
        BigInteger gasPrice = transaction.getGasPrice();
        BigInteger gas = transaction.getGas();


        EthGetTransactionReceipt receipt = null;
        try {
            receipt = web3j.ethGetTransactionReceipt(hash).send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean resultInfo = false;
        String statusStr = Objects.requireNonNull(receipt).getResult().getStatus();
        if (!Util.isEmpty(statusStr)) {
            int status = Integer.parseInt(statusStr.substring(statusStr.length() - 1));
            if (status == 1) {
                resultInfo = true;
            }
        }
        EthDealDetails ethDealDetails = new EthDealDetails();
        ethDealDetails.setResult(resultInfo);
        ethDealDetails.setHash(hash);
        ethDealDetails.setNonce(nonce.intValue());
        ethDealDetails.setBlockHash(blockHash);
        ethDealDetails.setBlockNumber(blockNumber.intValue());
        ethDealDetails.setTransIndex(transIndex.intValue());
        ethDealDetails.setFrom(from);
        ethDealDetails.setTo(to);
        ethDealDetails.setValue(value.intValue());
        ethDealDetails.setGasPrice(gasPrice.intValue());
        ethDealDetails.setGas(gas.intValue());

        listeningTransaction.onData(ethDealDetails);
    }

    /**
     * 查询交易信息
     *
     * @param txHash txHash
     * @return EthDealDetails 交易详情
     */
    public EthDealDetails getTransactionInfo(String txHash) throws Exception {

        if (Util.isEmpty(txHash)) {
            throw new Exception("txhash不能为空");
        }

        EthTransaction ethTransaction = web3j.ethGetTransactionByHash(txHash).send();
        org.web3j.protocol.core.methods.response.Transaction result = ethTransaction.getResult();

        if (result == null) {
            throw new Exception("暂无该订单");
        }
        String hash = ethTransaction.getResult().getHash();
        BigInteger nonce = ethTransaction.getResult().getNonce();
        String blockHash = ethTransaction.getResult().getBlockHash();
        BigInteger blockNumber = ethTransaction.getResult().getBlockNumber();
        BigInteger transIndex = new BigInteger(ethTransaction.getResult().getTransactionIndex().toString());
        String from = ethTransaction.getResult().getFrom();
        String to = ethTransaction.getResult().getTo();
        BigInteger value = ethTransaction.getResult().getValue();
        BigInteger gasPrice = ethTransaction.getResult().getGasPrice();
        BigInteger gas = ethTransaction.getResult().getGas();


        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(hash).send();

        boolean resultInfo = false;
        String statusStr = receipt.getResult().getStatus();
        if (!Util.isEmpty(statusStr)) {
            int status = Integer.parseInt(statusStr.substring(statusStr.length() - 1));
            if (status == 1) {
                resultInfo = true;
            }
        }
        EthDealDetails ethDealDetails = new EthDealDetails();
        ethDealDetails.setResult(resultInfo);
        ethDealDetails.setHash(hash);
        ethDealDetails.setNonce(nonce.intValue());
        ethDealDetails.setBlockHash(blockHash);
        ethDealDetails.setBlockNumber(blockNumber.intValue());
        ethDealDetails.setTransIndex(transIndex.intValue());
        ethDealDetails.setFrom(from);
        ethDealDetails.setTo(to);
        ethDealDetails.setValue(value.intValue());
        ethDealDetails.setGasPrice(gasPrice.intValue());
        ethDealDetails.setGas(gas.intValue());

        System.out.println(ethDealDetails.toString());
        return ethDealDetails;
    }


    /**
     * 查询代币精度
     *
     * @param fromAddress     查询地址
     * @param contractAddress 合约地址
     * @return 精度
     */
    private int getTokenDecimals(String contractAddress, String fromAddress) throws Exception {
        String methodName = "decimals";
        int decimal = 0;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);
        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
        List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        if (results != null && results.size() > 0) {
            decimal = Integer.parseInt(results.get(0).getValue().toString());
        } else {
            decimal = 0;
        }
        return decimal;
    }

    /**
     * 获取精度的方法
     *
     * @param tokenBalance 余额
     * @return WEI(" wei ", 0),
     * KWEI("kwei", 3),
     * MWEI("mwei", 6),
     * GWEI("gwei", 9),
     * SZABO("szabo", 12),
     * FINNEY("finney", 15),
     * ETHER("ether", 18),
     * KETHER("kether", 21),
     * METHER("mether", 24),
     * GETHER("gether", 27);
     */
    public BigDecimal getDeci(Convert.Unit unit, BigInteger tokenBalance) throws Exception {
        BigDecimal fromWei = null;
        if (unit == Convert.Unit.WEI) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.WEI);
        } else if (unit == Convert.Unit.KWEI) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.KWEI);
        } else if (unit == Convert.Unit.MWEI) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.MWEI);
        } else if (unit == Convert.Unit.GWEI) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.GWEI);
        } else if (unit == Convert.Unit.SZABO) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.SZABO);
        } else if (unit == Convert.Unit.FINNEY) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.FINNEY);
        } else if (unit == Convert.Unit.ETHER) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.ETHER);
        } else if (unit == Convert.Unit.KETHER) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.KETHER);
        } else if (unit == Convert.Unit.METHER) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.METHER);
        } else if (unit == Convert.Unit.GETHER) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.GETHER);
        }

        return fromWei;
    }


    /**
     * 获取精度的方法
     *
     * @param decimals
     * @param tokenBalance
     * @return WEI(" wei ", 0),
     * KWEI("kwei", 3),
     * MWEI("mwei", 6),
     * GWEI("gwei", 9),
     * SZABO("szabo", 12),
     * FINNEY("finney", 15),
     * ETHER("ether", 18),
     * KETHER("kether", 21),
     * METHER("mether", 24),
     * GETHER("gether", 27);
     */
    private BigDecimal getDeci(Integer decimals, BigInteger tokenBalance) {
        BigDecimal fromWei = null;
        if (decimals == 0) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.WEI);
        } else if (decimals == 3) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.KWEI);
        } else if (decimals == 6) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.MWEI);
        } else if (decimals == 9) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.GWEI);
        } else if (decimals == 12) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.SZABO);
        } else if (decimals == 15) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.FINNEY);
        } else if (decimals == 18) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.ETHER);
        } else if (decimals == 21) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.KETHER);
        } else if (decimals == 24) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.METHER);
        } else if (decimals == 27) {
            fromWei = Convert.fromWei(tokenBalance.toString(), Convert.Unit.ETHER);
        }
        return fromWei;
    }


    /**
     * @param gWei 旷工费
     * @return ether
     * @throws Exception 异常
     */
    public String getEthPrecision(int gWei) throws Exception {
        BigInteger gasPrice = new BigInteger(ETH_GAS_LIMIT * gWei + "000000000");
        BigDecimal decimal = getDeci(Convert.Unit.ETHER, gasPrice);
        return decimal + " " + Convert.Unit.ETHER.name();
    }

    /**
     * @param gWei 旷工费alsc
     * @return ether
     * @throws Exception 异常
     */
    public String getAlscPrecision(int gWei) throws Exception {
        BigInteger gasPrice = new BigInteger(ALSC_GAS_LIMIT * gWei + "000000000");
        BigDecimal decimal = getDeci(Convert.Unit.ETHER, gasPrice);
        return decimal + " " + Convert.Unit.ETHER.name();
    }


    /**
     * 根据行情计算价格
     *
     * @param gWei   旷工费
     * @param price  行情价格（ETH） 人民币或者美元
     * @param remark $ ￥ 2种符号
     * @return 行情价格
     * @throws Exception 异常
     */
    public String getEthPrecisionPrice(int gWei, float price, String remark) throws Exception {
        BigInteger gasPrice = new BigInteger(ETH_GAS_LIMIT * gWei + "000000000");
        BigDecimal decimal = getDeci(Convert.Unit.ETHER, gasPrice);
        return decimal + " " + Convert.Unit.ETHER.name().toLowerCase() + " ≈ " + remark + decimal.multiply(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 根据行情计算价格
     *
     * @param gWei   旷工费
     * @param price  行情价格（ETH） 人民币或者美元
     * @param remark $ ￥ 2种符号
     * @return 行情价格
     * @throws Exception 异常
     */
    public String getAlscPrecisionPrice(int gWei, float price, String remark) throws Exception {
        BigInteger gasPrice = new BigInteger(ALSC_GAS_LIMIT * gWei + "000000000");
        BigDecimal decimal = getDeci(Convert.Unit.ETHER, gasPrice);
        return decimal + " " + Convert.Unit.ETHER.name().toLowerCase() + " ≈ " + remark + decimal.multiply(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }


    /**
     * @param gWei 旷工费
     * @return 计算公式
     * @throws Exception 异常
     */
    public String getEthPrecisionRemark(int gWei) throws Exception {
        return "Gas (" + ETH_GAS_LIMIT + ") + Gas Price (" + gWei + " " + Convert.Unit.GWEI.name().toLowerCase() + ")";
    }

    /**
     * @param gWei 旷工费
     * @return ether
     * @throws Exception 异常
     */
    public String getTokenPrecision(int gWei) throws Exception {
        BigInteger gasPrice = new BigInteger(TOKEN_GAS_LIMIT * gWei + "000000000");
        BigDecimal decimal = getDeci(Convert.Unit.ETHER, gasPrice);
        return decimal + " " + Convert.Unit.ETHER.name().toLowerCase();
    }

    /**
     * 根据行情计算价格
     *
     * @param gWei   旷工费
     * @param price  行情价格（ETH） 人民币或者美元
     * @param remark $ ￥ 2种符号
     * @return 行情价格
     * @throws Exception 异常
     */
    public String getTokenPrecisionPrice(int gWei, float price, String remark) throws Exception {
        BigInteger gasPrice = new BigInteger(TOKEN_GAS_LIMIT * gWei + "000000000");
        BigDecimal decimal = getDeci(Convert.Unit.ETHER, gasPrice);
        return decimal + " " + Convert.Unit.ETHER.name().toLowerCase() + " ≈ " + remark + decimal.multiply(new BigDecimal(price)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * @param gWei 旷工费
     * @return 计算公式
     * @throws Exception 异常
     */
    public String getTokenPrecisionRemark(int gWei) throws Exception {
        return "Gas (" + TOKEN_GAS_LIMIT + ") + Gas Price (" + gWei + " " + Convert.Unit.GWEI.name().toLowerCase() + ")";
    }


}
