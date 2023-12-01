package com.game.awesa.ui

import android.content.res.Configuration
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.utils.SFProgress
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityWebviewBinding


class WebViewActivity : BaseActivity() {
    lateinit var binding: ActivityWebviewBinding
    var mType = 0
    var mUrl = ""


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = DataBindingUtil.setContentView(this@WebViewActivity, R.layout.activity_webview)
        try {
            SFProgress.showProgressDialog(this@WebViewActivity, true)
        } catch (e: Exception) {
        }

        binding.imgBack.setOnClickListener {
            finish()
        }
        if (intent.hasExtra("type")) {
            mType = intent.getIntExtra("type", 0) as Int;
        } else {
            finish()
            return
        }
        if (mType==1){
            mUrl +=Tags.SB_PRIVACY_POLICY
        }else if (mType==2){
            mUrl +=Tags.SB_ABOUT_US
        }else if (mType==3){
            mUrl +=Tags.SB_TERMS_CONDITION
        }
        binding.webview.setWebViewClient(object : WebViewClient() {
            override fun onReceivedError(webView: WebView, i: Int, str: String, str2: String) {}
            override fun onReceivedSslError(
                webView: WebView,
                sslErrorHandler: SslErrorHandler,
                sslError: SslError
            ) {
                try {
                    SFProgress.hideProgressDialog(this@WebViewActivity)
                } catch (e: Exception) {
                }
            }

            override fun onPageFinished(webView: WebView, str: String) {
                super.onPageFinished(webView, str)
                try {
                    SFProgress.hideProgressDialog(this@WebViewActivity)
                } catch (e: Exception) {
                }

            }

            override fun shouldOverrideUrlLoading(webView: WebView, str: String): Boolean {
                //  Log.d("shouldOverrideUrlLoa", str);
                return super.shouldOverrideUrlLoading(webView, str)
            }
        })
        binding.webview.getSettings().setJavaScriptEnabled(true)
        binding.webview.loadUrl(mUrl)

    }
}