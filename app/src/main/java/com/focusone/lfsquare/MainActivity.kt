package com.focusone.lfsquare

import ShakeDetector
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.focusone.lfsquare.databinding.ActivityMainBinding
import com.focusone.lfsquare.util.BackPressedForFinish

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var backPressedForFinish: BackPressedForFinish
    private lateinit var mWebView: BaseWebView
    private lateinit var shakeDetector: ShakeDetector

    companion object {
        const val TAG = "MainActivity"
        const val MAIN_URL= BuildConfig.MAIN_URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        backPressedForFinish = BackPressedForFinish(this)

        initView()

        shakeDetector = ShakeDetector(this) {
            showBarcodePopup()
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = with(binding) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            val msg = ">>>>> canGoBack: [${webView.url}]"
            Log.e(TAG, msg)
            val nIndex = 2
            val historyList = webView.copyBackForwardList()
            var mallMainUrl = ""
            val webHistoryItem = historyList.getItemAtIndex(nIndex)
            if (webHistoryItem != null) {
                mallMainUrl = webHistoryItem.url
            }
            if (webView.url.equals(mallMainUrl, ignoreCase = true)) {
                val backBtn: BackPressedForFinish = getBackPressedClass()
                backBtn.onBackPressed()
            } else {
                webView.goBack() // 뒤로가기
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !webView.canGoBack()) {
            val backBtn: BackPressedForFinish = getBackPressedClass()
            backBtn.onBackPressed()
        } else {
            return super.onKeyDown(keyCode, event)
        }
        return true
    }

    private fun getBackPressedClass(): BackPressedForFinish {
        return backPressedForFinish
    }

    private fun initView() = with(binding) {
        webView.loadUrl(MAIN_URL)
        Log.d(TAG, "baseUrl: ${BuildConfig.MAIN_URL}")
    }

    private fun showBarcodePopup() {
        val jsCodeBarcodePopup = "$('.layerpop').show();"
        binding.webView.evaluateJavascript(jsCodeBarcodePopup, null)
    }


    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.unregisterListener()
    }
}