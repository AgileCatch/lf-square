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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        backPressedForFinish = BackPressedForFinish(this)

        initView()

        shakeDetector = ShakeDetector(this) {
            showBarcodePopup()
        }

    }

    private fun showBarcodePopup() {
        val jsCode = "$('.layerpop').show();"
        binding.webView.evaluateJavascript(jsCode, null)
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

//    val mBarcodeLauncher = registerForActivityResult() { result: ScanIntentResult? ->
//        Log.e(TAG, "Barcode Scanner Callback is called with result: $result")
//        val jsCode = "$('.layerpop').show();"
//
//    }


    private fun initView()= with(binding) {
//        var baseUrl = BuildConfig.MAIN_URL
        webView.loadUrl("https://www.lfsquare.co.kr/membership")
//        Log.d(TAG, "MAIN_URL: ${BuildConfig.MAIN_URL}")
    }

    companion object{
        const val TAG ="MainActivity"
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.unregisterListener()
    }
}