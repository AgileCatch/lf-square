package com.focusone.lfsquare

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class BaseWebView : WebView {
    companion object {
        private const val TAG = "BaseWebView"
        var mContext: Context? = null
        lateinit var mWebView: BaseWebView

    }

    constructor(context: Context) : super(context) {
        mContext = context
        initializeOptions()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initializeOptions()
    }

    init {
        initializeOptions()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeOptions() {
//        if (BuildConfig.DEBUG) {
//            setWebContentsDebuggingEnabled(true)
//        }

        // WebView 설정
        val webSettings: WebSettings = this.settings
        webSettings.apply {
            loadsImagesAutomatically = true
            javaScriptEnabled = true    // 웹페이지 자바스크립트 허용 여부
            setSupportMultipleWindows(true) //멀티윈도우를 지원할지 여부
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true //컨텐츠가 웹뷰보다 클때 스크린 크기에 맞추기
            useWideViewPort = true  // 화면 사이즈 맞추기 허용 여부

            domStorageEnabled = true     //DOM 로컬 스토리지 사용여부
            databaseEnabled = true  //database storage API 사용 여부
            allowFileAccess = true  //파일 액세스 허용 여부
            allowContentAccess = true    //Content URL 에 접근 사용 여부

            textZoom = 100  // system 글꼴 크기에 의해 변하는 것 방지

            setSupportZoom(true)    // 화면 줌 허용 여부
            builtInZoomControls = true  // 줌 아이콘
            displayZoomControls = false // 웹뷰 화면에 보이는 (+/-) 줌 아이콘


//            // user-agent에 ",hazzys@LF" 등을 추가 하여 Web 에서 App 인지를 판단 하게 한다.
//            setUserAgent(webSettings)

            // https -> http 호출 허용
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING   // 컨텐츠 사이즈 자동 맞추기
            cacheMode = WebSettings.LOAD_DEFAULT

        }

        // 서드파티 쿠키 허용.
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)

//        // App <----> Javascript 통신객체 생성
//        addJavascriptInterface(AndroidScriptBridge(this), "skscms")

        // WebViewClient 설정
        webViewClient = MyWebViewClient()
        // WebChromeClient 설정
        webChromeClient = MyWebChromeClient()

    }

    private inner class MyWebChromeClient : WebChromeClient() {

    }

    private inner class MyWebViewClient : WebViewClient(){

    }
}