package com.meta.zoom.wallet;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.TokenBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.manager.DataManager;
import com.common.lib.utils.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.meta.zoom.wallet.bean.NetworkInfo;

import com.upyun.library.common.LogInterceptor;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.meta.zoom.wallet.bean.TransactionBean;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class WalletManager {

    private static final String TAG = "WalletManager";

    private static WalletManager mWalletManager = null;
    private NetworkInfo mCurrentNetworkInfo;

    private OkHttpClient mHttpClient;
    private Web3j mWeb3j;
    private BlockExplorerClient mBlockExplorerClient;


    private WalletManager() {
        mHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor())
                .build();
        mBlockExplorerClient = new BlockExplorerClient(mHttpClient);
        resetNetwork(DataManager.getInstance().getCurrentChain());
    }

    public void resetNetwork(ChainBean chainBean) {
        mCurrentNetworkInfo = new NetworkInfo(chainBean.getChainName(), chainBean.getSymbol(),
                chainBean.getRpcUrl(),
                chainBean.getExplore(),
                chainBean.getExplore(), chainBean.getChainId(), false);
        mWeb3j = Web3j.build(new HttpService(mCurrentNetworkInfo.rpcServerUrl, mHttpClient, false));
        if (!TextUtils.isEmpty(mCurrentNetworkInfo.backendUrl)) {
            mBlockExplorerClient.buildApiClient(mCurrentNetworkInfo.backendUrl);
        }
    }

    public NetworkInfo getCurrentNetworkInfo() {
        return mCurrentNetworkInfo;
    }

    public static WalletManager getInstance() {
        if (mWalletManager == null) {
            synchronized (TAG) {
                mWalletManager = new WalletManager();
            }
        }
        return mWalletManager;
    }

    public Single<BigInteger> getChainId(String rpcUrl) {
        return Single.fromCallable(() -> {
                    Web3j web3j = Web3j.build(new HttpService(rpcUrl, mHttpClient, false));
                    EthChainId ethChainId = web3j.ethChainId().sendAsync().get();
                    BigInteger chainId = ethChainId.getChainId();
                    LogUtil.LogE("chainId: " + chainId);
                    return chainId;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    public String deriveKeystore(WalletBean wallet) {
        String keystore = null;
        WalletFile walletFile;
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        try {
            walletFile = objectMapper.readValue(new File(wallet.getKeystorePath()), WalletFile.class);
            keystore = objectMapper.writeValueAsString(walletFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keystore;
    }

    public Single<WalletBean> loadWalletByPrivateKey(final String walletName, final String privateKey, final String pwd) {
        return Single.fromCallable(() -> ETHWalletUtils.loadWalletByPrivateKey(walletName, privateKey, pwd)
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Single<WalletBean> loadWalletByMnemonic(final String walletName, final String bipPath, final String mnemonic, final String pwd) {
        return Single.fromCallable(() -> ETHWalletUtils.importMnemonic(walletName, bipPath
                        , Arrays.asList(mnemonic.split(" ")), pwd)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


    }

    public Observable<ArrayList<TransactionBean>> getTransactions(String walletAddress, String tokenAddress) {
        return fetchTransaction(walletAddress, tokenAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<ArrayList<TransactionBean>> fetchTransaction(String walletAddress, String tokenAddress) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(mCurrentNetworkInfo.backendUrl)) {
                e.onNext(new ArrayList<>());
            } else {
                ArrayList<TransactionBean> transactions = mBlockExplorerClient.fetchTransactions(walletAddress, tokenAddress).blockingFirst();
                e.onNext(transactions);
            }
            e.onComplete();
        });
    }

    public Observable<ArrayList<TokenBean>> getTokens(String walletAddress) {
        return fetchTokens(walletAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<BigInteger> estimateGas() {

        return getEstimateGas().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<BigInteger> estimateGasLimit(String from, String to, String data) {
        return getEstimateGasLimit(from, to, data).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<BigInteger> getEstimateGasLimit(String from, String to, String data) {
        return Observable.create(e -> {
            BigInteger gasPrice = mWeb3j.ethGasPrice().send().getGasPrice();
            EthGetTransactionCount ethGetTransactionCount = mWeb3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            Transaction t = new Transaction(from, nonce, gasPrice, (BigInteger) null, to, new BigInteger("0"), data);
            EthEstimateGas gas = mWeb3j.ethEstimateGas(t).send();
            if (null == gas.getError()) {
                e.onNext(new BigInteger(gas.getResult().substring(2), 16));
            } else {
                e.onError(new Throwable(gas.getError().getMessage()));
            }
        });
    }

    private Observable<BigInteger> getEstimateGas() {

        return Observable.create(e -> {
            e.onNext(mWeb3j.ethGasPrice().send().getGasPrice());
        });
    }

    private Observable<ArrayList<TokenBean>> fetchTokens(String walletAddress) {
        return Observable.create(e -> {
            ArrayList<TokenBean> tokens = DatabaseOperate.getInstance().getTokenList(mCurrentNetworkInfo.chainId, walletAddress);
            if (tokens.isEmpty()) {
                TokenBean bean = new TokenBean(mCurrentNetworkInfo.chainId, walletAddress, "", mCurrentNetworkInfo.symbol, 18);
                long id = DatabaseOperate.getInstance().insert(bean);
                bean.setId((int) id);
                tokens.add(bean);
            }
            e.onNext(tokens);
            ContentValues values = new ContentValues();
            for (TokenBean bean : tokens) {
                BigDecimal balance = null;
                try {
                    if (TextUtils.isEmpty(bean.getContractAddress())) {
                        balance = getBalance(walletAddress);
                    } else {
                        balance = getBalance(walletAddress, bean);
                    }
                } catch (Exception e1) {
                    LogUtil.LogE("Err" + e1.getMessage());
                }
                if (balance == null) {
                    continue;
                }
                if (balance.compareTo(BigDecimal.ZERO) == 0) {
                    bean.setBalance("0");
                } else {
                    BigDecimal decimalDivisor = new BigDecimal(Math.pow(10, bean.getTokenPrecision()));
                    BigDecimal ethBalance = balance.divide(decimalDivisor);
                    if (bean.getTokenPrecision() > 4) {
                        bean.setBalance(ethBalance.setScale(4, RoundingMode.CEILING).toPlainString());
                    } else {
                        bean.setBalance(ethBalance.setScale(bean.getTokenPrecision(), RoundingMode.CEILING).toPlainString());
                    }
                }
                values.put(bean.getPrimaryKeyName(), bean.getId());
                values.put("balance", bean.getBalance());
                DatabaseOperate.getInstance().update(bean, values);
            }
            e.onNext(tokens);
        });
    }

    private BigDecimal getBalance(String walletAddress) throws Exception {
        return new BigDecimal(mWeb3j
                .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance());
    }


    private BigDecimal getBalance(String walletAddress, TokenBean tokenInfo) throws Exception {
        Function function = balanceOf(walletAddress);
        String responseValue = callSmartContractFunction(function, tokenInfo.getContractAddress(), walletAddress);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        if (response.size() == 1) {
            return new BigDecimal(((Uint256) response.get(0)).getValue());
        } else {
            return null;
        }
    }

    private static Function balanceOf(String owner) {
        return new Function(
                "balanceOf",
                Collections.singletonList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }

    private String callSmartContractFunction(
            Function function, String contractAddress, String walletAddress) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response = mWeb3j.ethCall(
                        Transaction.createEthCallTransaction(walletAddress, contractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        return response.getValue();
    }


    public Single<String> createTransaction(WalletBean from, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, String data, String password) {
        return getLastTransactionNonce(mWeb3j, from.getAddress())
                .flatMap(nonce -> Single.fromCallable(() -> {
                            Credentials credentials = WalletUtils.loadCredentials(password, from.getKeystorePath());
                            RawTransaction rawTransaction;
                            BigInteger addr = Numeric.toBigInt(to);
                            LogUtil.LogE(addr);
                            if (addr.equals(BigInteger.ZERO)) {
                                rawTransaction = RawTransaction.createContractTransaction(nonce, gasPrice, gasLimit,
                                        amount == null ? new BigInteger("0") : amount, data);
                            } else {
                                rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to,
                                        amount == null ? new BigInteger("0") : amount, data);
                            }
                            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, mCurrentNetworkInfo.chainId, credentials);
                            String hexValue = Numeric.toHexString(signedMessage);
                            EthSendTransaction ethSendTransaction = mWeb3j.ethSendRawTransaction(hexValue).send();
                            LogUtil.LogE(ethSendTransaction.getTransactionHash());
                            return ethSendTransaction.getTransactionHash();
                        }).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    public Single<String> createEthTransfer(WalletBean from, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, String password) {
        return getLastTransactionNonce(mWeb3j, from.getAddress())
                .flatMap(nonce -> Single.fromCallable(() -> {
                            LogUtil.LogE("createEthTransaction");
                            Credentials credentials = WalletUtils.loadCredentials(password, from.getKeystorePath());
                            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount);
                            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, mCurrentNetworkInfo.chainId, credentials);
                            String hexValue = Numeric.toHexString(signedMessage);
                            EthSendTransaction ethSendTransaction = mWeb3j.ethSendRawTransaction(hexValue).send();
                            LogUtil.LogE("hash: " + ethSendTransaction.getTransactionHash());
                            return ethSendTransaction.getTransactionHash();
                        }).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread()));
    }


    public Single<String> createERC20Transfer(WalletBean from, String to, String contractAddress, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, String password) {

        String callFuncData = createTokenTransferData(to, amount);
        return getLastTransactionNonce(mWeb3j, from.getAddress())
                .flatMap(nonce -> Single.fromCallable(() -> {

                            Credentials credentials = WalletUtils.loadCredentials(password, from.getKeystorePath());
                            RawTransaction rawTransaction = RawTransaction.createTransaction(
                                    nonce, gasPrice, gasLimit, contractAddress, callFuncData);
                            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, mCurrentNetworkInfo.chainId, credentials);
                            String hexValue = Numeric.toHexString(signedMessage);
                            EthSendTransaction ethSendTransaction = mWeb3j.ethSendRawTransaction(hexValue).send();
                            return ethSendTransaction.getTransactionHash();

                        }).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    private Single<BigInteger> getLastTransactionNonce(Web3j web3j, String walletAddress) {

        return Single.fromCallable(() -> {
            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(walletAddress, DefaultBlockParameterName.LATEST)   // or DefaultBlockParameterName.LATEST
                    .send();
            return ethGetTransactionCount.getTransactionCount();
        });
    }

    private String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));

        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
        });

        Function function = new Function("transfer", params, returnTypes);
        return FunctionEncoder.encode(function);
    }

    public Single<TokenBean> getTokenByAddress(String contractAddress) {

        return Single.fromCallable(() -> {
                    String methodName = "symbol";
                    String symbol = null;
                    List<Type> inputParameters = new ArrayList<>();
                    List<TypeReference<?>> outputParameters = new ArrayList<>();
                    TypeReference<Utf8String> typeReferences = new TypeReference<Utf8String>() {
                    };
                    outputParameters.add(typeReferences);
                    Function function = new Function(methodName, inputParameters, outputParameters);
                    String data = FunctionEncoder.encode(function);
                    Transaction transaction = Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, data);
                    EthCall ethCall = mWeb3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
                    List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    symbol = results.get(0).getValue().toString();

                    methodName = "decimals";
                    int decimal = 0;
                    TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
                    };
                    outputParameters.clear();
                    outputParameters.add(typeReference);
                    function = new Function(methodName, inputParameters, outputParameters);
                    data = FunctionEncoder.encode(function);
                    transaction = Transaction.createEthCallTransaction(Address.DEFAULT.toString(), contractAddress, data);
                    ethCall = mWeb3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
                    results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    decimal = Integer.parseInt(results.get(0).getValue().toString());
                    TokenBean bean = new TokenBean();
                    bean.setSymbol(symbol);
                    bean.setTokenPrecision(decimal);
                    return bean;
                }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static boolean isValid(String mnemonic) {
        return mnemonic.split(" ").length >= 12;
    }

}
