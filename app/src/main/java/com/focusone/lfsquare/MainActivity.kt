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

    lateinit var binding: ActivityMainBinding // ActivityMainBinding을 지연 초기화합니다.
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

        val mainWebView = binding.mainWebView
        val subWebView = binding.subWebView

        mainWebView.setActivity(this)
        mainWebView.setSubWebView(subWebView)

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
        when {
            // 서브 웹뷰가 보이는 경우
            keyCode == KeyEvent.KEYCODE_BACK && subWebViewContainer.visibility == View.VISIBLE -> {
                if (subWebView.canGoBack()) {
                    subWebView.goBack()// 서브 웹뷰에서 뒤로 가기
                } else {
                    hideSubWebView() // 서브 웹뷰 숨기기
                }
            }
            // 메인 웹뷰가 뒤로 갈 수 있는 경우
            keyCode == KeyEvent.KEYCODE_BACK && mainWebView.canGoBack() -> {
                val msg = ">>>>> canGoBack: [${mainWebView.url}]"
                Log.e(TAG, msg)
                val nIndex = 2
                val historyList = mainWebView.copyBackForwardList()
                var mallMainUrl = ""
                val webHistoryItem = historyList.getItemAtIndex(nIndex)
                if (webHistoryItem != null) {
                    mallMainUrl = webHistoryItem.url
                }
                if (mainWebView.url.equals(mallMainUrl, ignoreCase = true)) {
                    val backBtn: BackPressedForFinish = getBackPressedClass()
                    backBtn.onBackPressed()
                } else {
                    mainWebView.goBack() // 메인 웹뷰에서 뒤로 가기
                }
            }
            // 메인 웹뷰가 뒤로 갈 수 없는 경우
            keyCode == KeyEvent.KEYCODE_BACK && !mainWebView.canGoBack() -> {
                val backBtn: BackPressedForFinish = getBackPressedClass()
                backBtn.onBackPressed()
            }
            // 다른 경우
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }


    private fun getBackPressedClass(): BackPressedForFinish {
        return backPressedForFinish
    }

    private fun initView() = with(binding) {
        mainWebView.loadUrl(MAIN_URL)
        Log.d(TAG, "baseUrl: ${BuildConfig.MAIN_URL}")
    }

    private fun showBarcodePopup() {
        val jsCodeBarcodePopup = "$('.layerpop').show();"
        binding.mainWebView.evaluateJavascript(jsCodeBarcodePopup, null)
    }

    private fun setupButtons() = with(binding) {
        btnBack.setOnClickListener {
            if (mainWebView.canGoBack()) {
                mainWebView.goBack()
            } else {
                showToast(this@MainActivity, R.string.no_page_to_load)
            }
        }

        btnForward.setOnClickListener {
            if (mainWebView.canGoForward()) {
                Log.d(TAG, "Moving forward")
                mainWebView.goForward()
            } else {
                showToast(this@MainActivity, R.string.no_page_to_load)
                Log.d(TAG, "No next page to go forward to")
            }
        }

        btnRefresh.setOnClickListener {
            mainWebView.reload()
        }

        btnHome.setOnClickListener {
            mainWebView.loadUrl(MAIN_URL)
        }

        btnBarcode.setOnClickListener {
            showBarcodePopup()
        }
    }

    private fun setupScrollListener() = with(binding) {
        mainWebView.viewTreeObserver.addOnScrollChangedListener(object :
            ViewTreeObserver.OnScrollChangedListener {
            private var lastScrollY = 0
            private var lastScrollDirection = 0 // 1: up, -1: down

            override fun onScrollChanged() {
                val currentScrollY = mainWebView.scrollY
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

    fun showProgressBar() = with(binding) {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() = with(binding) {
        progressBar.visibility = View.GONE
    }

    private fun showSubWebView() = with(binding) {
        subWebViewContainer.visibility = View.VISIBLE
        mainWebView.visibility = View.GONE
    }

    private fun hideSubWebView() = with(binding) {
        subWebViewContainer.visibility = View.GONE
        mainWebView.visibility = View.VISIBLE
    }


    private fun showToast(context: Context, message: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.unregisterListener()

    }
}