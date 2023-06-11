package com.meta.zoom.web3.provider

import android.content.Context
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.common.lib.activity.BaseActivity
import com.common.lib.activity.db.DatabaseOperate
import com.common.lib.bean.ChainBean
import com.common.lib.constant.EventBusEvent
import com.common.lib.interfaces.OnClickCallback
import com.common.lib.manager.DataManager
import com.common.lib.utils.LogUtil
import com.meta.zoom.R
import com.meta.zoom.dialog.AddNetworkDialog
import com.meta.zoom.wallet.WalletManager
import com.meta.zoom.web3.OnSignTransactionListener
import com.meta.zoom.web3.entity.Address
import com.meta.zoom.web3.entity.Hex
import com.meta.zoom.web3.entity.Web3Transaction
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.okButton
import splitties.alertdialog.appcompat.title
import splitties.alertdialog.material.materialAlertDialog
import wallet.core.jni.CoinType
import wallet.core.jni.Curve
import wallet.core.jni.PrivateKey
import java.math.BigInteger
import java.util.*


class WebAppInterface(
    private val context: Context,
    private val webView: WebView,
    private val onSignTransactionListener: OnSignTransactionListener
) {
    val credentials: Credentials =
        WalletUtils.loadCredentials(
            DataManager.getInstance().currentWallet.password,
            DataManager.getInstance().currentWallet.keystorePath
        )
    private var privateKey =
        PrivateKey(credentials.ecKeyPair.privateKey.toString(16).toHexByteArray())

    private val address = CoinType.ETHEREUM.deriveAddress(privateKey).lowercase(Locale.ROOT)
    //  private val pubKey = CoinType.SOLANA.deriveAddress(privateKey)

    fun signTransaction(
        callbackId: Int,
        recipient: String?,
        value: String,
        nonce: String?,
        gasLimit: String?,
        gasPrice: String?,
        payload: String?,
        from: String?
    ) {
        var value = value
        var gasPrice = gasPrice
        if (value == "undefined") value = "0"
        if (gasPrice == null) gasPrice = "0"
        val transaction = Web3Transaction(
            if (TextUtils.isEmpty(recipient)) Address.EMPTY else Address(
                recipient!!
            ),
            null,
            Hex.hexToBigInteger(value),
            Hex.hexToBigInteger(gasPrice, BigInteger.ZERO),
            Hex.hexToBigInteger(gasLimit, BigInteger.ZERO),
            Hex.hexToLong(nonce, -1),
            payload,
            callbackId.toLong(),
            if (TextUtils.isEmpty(from)) Address.EMPTY else Address(
                from!!
            ),
        )
        webView.post {
            onSignTransactionListener.onSignTransaction(
                transaction,
                webView.url
            )
        }
    }

    @JavascriptInterface
    fun postMessage(json: String) {
        val obj = JSONObject(json)
        println(obj)
        val id = obj.getLong("id")
        val method = DAppMethod.fromValue(obj.getString("name"))
        val network = obj.getString("network")
        LogUtil.LogE(id.toString() + ", " + method + ", " + network)
        when (method) {
            DAppMethod.REQUESTACCOUNTS -> {
                val setAddress = "window.$network.setAddress(\"$address\");"
                val callback = "window.$network.sendResponse($id, [\"$address\"])"
                webView.post {
                    webView.evaluateJavascript(setAddress) {
                    }
                    webView.evaluateJavascript(callback) {
                    }
                }
            }
            DAppMethod.SIGNTRANSACTION -> {
                val id = obj.getInt("id")
                val jsonObject = obj.getJSONObject("object")
                var from = jsonObject.optString("from")
                val nonce = jsonObject.optString("nonce")
                val to = jsonObject.optString("to")
                val value = jsonObject.optString("value")
                val gasPrice = jsonObject.optString("gasPrice")
                val gas = jsonObject.optString("gas")
                val data = jsonObject.optString("data")
                signTransaction(id, to, value, nonce, gas, gasPrice, data, from)
            }
            DAppMethod.SIGNMESSAGE -> {
                val data = extractMessage(obj)
                if (network == "ethereum")
                    handleSignMessage(id, data, addPrefix = false)
                else
                    handleSignSolanaMessage(id, data)
            }
            DAppMethod.SIGNPERSONALMESSAGE -> {
                val data = extractMessage(obj)
                handleSignMessage(id, data, addPrefix = true)
            }
            DAppMethod.SIGNTYPEDMESSAGE -> {
                val data = extractMessage(obj)
                val raw = extractRaw(obj)
                handleSignTypedMessage(id, data, raw)
            }
            //添加
            DAppMethod.ADDETHEREUMCHAIN -> {
                LogUtil.LogE("addEthereumChain: " + obj)
                val callback = "window.$network.sendResponse($id)"
                webView.post {
                    val setAddress = "window.ethereum.setAddress(\"\");"
                    webView.evaluateJavascript(setAddress, null)

                    webView.evaluateJavascript(callback) {
                    }
                }
                val jsonObject = obj.getJSONObject("object")
                val chainId = jsonObject.optString("chainId").substring(2).toInt(16)
                val chainName = jsonObject.optString("chainName")
                val symbol = jsonObject.getJSONObject("nativeCurrency").optString("symbol")
                val rpcUrl = jsonObject.getJSONArray("rpcUrls")[0] as String
                val array = jsonObject.optJSONArray("blockExplorerUrls")
                var browser = ""
                if (array != null && array.length() > 0) {
                    browser = array[0] as String
                }

                val ch = DatabaseOperate.getInstance().getChain(chainId)
                if (ch != null) {
                    (context as BaseActivity<*>).showTwoBtnDialog(
                        context.getString(R.string.app_switch_network),
                        context.getString(R.string.app_cancel),
                        context.getString(R.string.app_confirm),
                        object : OnClickCallback {
                            override fun onClick(viewId: Int) {
                                when (viewId) {
                                    com.common.lib.R.id.btn2 -> {
                                        LogUtil.LogE("switchChainWallet")
                                        switchChainWallet(ch)
                                    }
                                }
                            }
                        }
                    )
                } else {
                    val chain = ChainBean(chainId, chainName, rpcUrl, symbol, browser, 0)
                    val dialog =
                        AddNetworkDialog(context, chain, AddNetworkDialog.OnAddClickListener {
                            DatabaseOperate.getInstance().insert(chain)
                            switchChainWallet(chain)
                        })
                    dialog.show()
                }
            }
            else -> {
                context.materialAlertDialog {
                    title = "Error"
                    message = "$method not implemented"
                    okButton {
                    }
                }.show()
            }
        }
    }

    private fun switchChainWallet(chain: ChainBean) {
        var bean = DataManager.getInstance().currentWallet
        val w = DatabaseOperate.getInstance().getWallet(
            bean.getAddress(),
            chain.getChainId()
        )
        if (w == null) {
            bean.setId(null)
            bean.setChainId(chain.getChainId())
            bean.setId(
                DatabaseOperate.getInstance().insert(bean).toInt()
            )
        } else {
            bean = w
        }
        DataManager.getInstance().saveCurrentWallet(bean)
        DataManager.getInstance().saveCurrentChain(chain)
        WalletManager.getInstance().resetNetwork(chain)
        val map = HashMap<String, Any>()
        map[EventBusEvent.REFRESH_NETWORK] = ""
        EventBus.getDefault().post(map)
    }

    private fun extractMessage(json: JSONObject): ByteArray {
        val param = json.getJSONObject("object")
        val data = param.getString("data")
        return Numeric.hexStringToByteArray(data)
    }

    private fun extractRaw(json: JSONObject): String {
        val param = json.getJSONObject("object")
        return param.getString("raw")
    }

    private fun handleSignMessage(id: Long, data: ByteArray, addPrefix: Boolean) {
        context.materialAlertDialog {
            title = "Sign Ethereum Message"
            message = if (addPrefix) String(data, Charsets.UTF_8) else Numeric.toHexString(data)
            cancelButton {
                webView.sendError("ethereum", "Cancel", id)
            }
            okButton {
                webView.sendResult("ethereum", signEthereumMessage(data, addPrefix), id)
            }
        }.show()
    }

    private fun handleSignSolanaMessage(id: Long, data: ByteArray) {
        context.materialAlertDialog {
            title = "Sign Solana Message"
            message = String(data, Charsets.UTF_8) ?: Numeric.toHexString(data)
            cancelButton {
                webView.sendError("solana", "Cancel", id)
            }
            okButton {
                webView.sendResult("solana", signSolanaMessage(data), id)
            }
        }.show()
    }

    private fun handleSignTypedMessage(id: Long, data: ByteArray, raw: String) {
        context.materialAlertDialog {
            title = "Sign Typed Message"
            message = raw
            cancelButton {
                webView.sendError("ethereum", "Cancel", id)
            }
            okButton {
                webView.sendResult("ethereum", signEthereumMessage(data, false), id)
            }
        }.show()
    }

    private fun signEthereumMessage(message: ByteArray, addPrefix: Boolean): String {
        var data = message
        if (addPrefix) {
            val messagePrefix = "\u0019Ethereum Signed Message:\n"
            val prefix = (messagePrefix + message.size).toByteArray()
            val result = ByteArray(prefix.size + message.size)
            System.arraycopy(prefix, 0, result, 0, prefix.size)
            System.arraycopy(message, 0, result, prefix.size, message.size)
            data = wallet.core.jni.Hash.keccak256(result)
        }

        val signatureData = privateKey.sign(data, Curve.SECP256K1)
            .apply {
                (this[this.size - 1]) = (this[this.size - 1] + 27).toByte()
            }
        return Numeric.toHexString(signatureData)
    }

    private fun signSolanaMessage(message: ByteArray): String {
        val signature = privateKey.sign(message, Curve.ED25519)
        return Numeric.toHexString(signature)

    }
}
