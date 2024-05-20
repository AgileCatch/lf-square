package com.focusone.lfsquare

import ShakeDetector
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.focusone.lfsquare.databinding.ActivityMainBinding
import com.focusone.lfsquare.util.BackPressedForFinish

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // ActivityMainBinding을 지연 초기화합니다.
    private lateinit var backPressedForFinish: BackPressedForFinish
    private lateinit var shakeDetector: ShakeDetector

    companion object {
        const val TAG = "MainActivity"
        const val MAIN_URL = BuildConfig.MAIN_URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView= binding.webView
        webView.setActivity(this)

        backPressedForFinish = BackPressedForFinish(this)


        initView()
        //쉐이크 하면 바코드 팝업
        shakeDetector = ShakeDetector(this) {
            showBarcodePopup()
        }
        //버튼 셋업
        setupButtons()
        //bottom app bar 셋업
        setupScrollListener()
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

    private fun setupButtons() = with(binding) {
        btnBack.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
//                showProgressBar()
            }else{
                showToast(this@MainActivity, R.string.no_page_to_load)
//                hideProgressBar()
            }
        }

        btnForward.setOnClickListener {
            if (webView.canGoForward()) {
                Log.d(TAG, "Moving forward")
                webView.goForward()
//                showProgressBar()
            } else {
                showToast(this@MainActivity,R.string.no_page_to_load)
                Log.d(TAG, "No next page to go forward to")
//                hideProgressBar()
            }
        }

        btnRefresh.setOnClickListener {
            webView.reload()
        }

        btnHome.setOnClickListener {
            webView.loadUrl(MAIN_URL)
        }

        btnBarcode.setOnClickListener {
            showBarcodePopup()
        }
    }

    private fun setupScrollListener() = with(binding) {
        webView.viewTreeObserver.addOnScrollChangedListener(object :
            ViewTreeObserver.OnScrollChangedListener {
            private var lastScrollY = 0
            private var lastScrollDirection = 0 // 1: up, -1: down

            override fun onScrollChanged() {
                val currentScrollY = webView.scrollY
                val scrollDelta = currentScrollY - lastScrollY

                if (scrollDelta > 0 && lastScrollDirection != -1) {
                    lastScrollDirection = -1
                    hideBottomAppBar()
                } else if (scrollDelta < 0 && lastScrollDirection != 1) {
                    lastScrollDirection = 1
                    showBottomAppBar()
                }

                lastScrollY = currentScrollY
            }
        })
    }

    private fun hideBottomAppBar() = with(binding) {
        apply {
            bottomAppBar.animate()
                .translationY(bottomAppBar.height.toFloat())
                .setDuration(300)
                .start()
            btnBarcode.animate()
                .translationY(bottomAppBar.height.toFloat())
                .setDuration(300)
                .start()
        }
    }

    private fun showBottomAppBar() = with(binding) {
        apply {
            bottomAppBar.animate()
                .translationY(0f)
                .setDuration(300)
                .start()
            btnBarcode.animate()
                .translationY(0f)
                .setDuration(300)
                .start()
        }
    }

    fun showProgressBar()= with(binding) {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar()= with(binding) {
        progressBar.visibility = View.GONE
    }

    private fun showToast(context: Context, message: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.unregisterListener()
    }
}