package com.meta.zoom.web3.provider

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.common.lib.manager.DataManager
import com.common.lib.utils.LogUtil
import com.meta.zoom.R
import com.meta.zoom.fragment.DappWebFragment
import com.meta.zoom.web3.OnSignTransactionListener
import org.jetbrains.annotations.NotNull
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.util.*


class DappWebViewAdapter(
    @NotNull val context: Context,
    @NotNull val webView: WebView,
    @NotNull val dappUrl: String,
    @NotNull val onSignTransactionListener: OnSignTransactionListener
) {

    fun init() {
        WebView.setWebContentsDebuggingEnabled(true)
        webView.settings.run {
            javaScriptEnabled = true
            domStorageEnabled = true
        }


        val provderJs = loadProviderJs()
        val initJs = loadInitJs(
            DataManager.getInstance().currentChain.chainId,//56//
            DataManager.getInstance().currentChain.rpcUrl //"https://bsc-dataseed1.binance.org"//
        )

        val address =
            DataManager.getInstance().currentWallet.address.lowercase(Locale.ROOT)

        WebAppInterface(
            context,
            webView,
            onSignTransactionListener,
        ).run {
            webView.addJavascriptInterface(this, "_tw_")
            val webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    //设置trust_min.js
                    webView.evaluateJavascript(provderJs, null)
                    //设置js初始化语句
                    webView.evaluateJavascript(initJs, null)
                    val setAddress = "window.ethereum.setAddress(\"$address\");"
                    webView.evaluateJavascript(setAddress, null)
                }


                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    // Ignore SSL certificate errors
                    handler?.proceed()
                    println(error.toString())
                }
            }
            webView.webViewClient = webViewClient
            webView.loadUrl(dappUrl)
        }
    }

    private fun loadProviderJs(): String {
        return context.resources.openRawResource(R.raw.trust).bufferedReader().use { it.readText() }
    }


    private fun loadInitJs(chainId: Int, rpcUrl: String): String {
        val source = """
        (function() {
            var config = {                
                ethereum: {
                    chainId: $chainId,
                    rpcUrl: "$rpcUrl"
                },
                solana: {
                    cluster: "mainnet-beta",
                },
                isDebug: true
            };
            trustwallet.ethereum = new trustwallet.Provider(config);
            trustwallet.solana = new trustwallet.SolanaProvider(config);
            trustwallet.postMessage = (json) => {
                window._tw_.postMessage(JSON.stringify(json));
            }
            window.ethereum = trustwallet.ethereum;
        })();
        """
        return source
    }

}