package com.app.uxis.lf

import ShakeDetector
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.uxis.lf.databinding.ActivityMainBinding
import com.app.uxis.lf.util.BackPressedForFinish
import com.google.firebase.messaging.FirebaseMessaging
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.gun0912.tedpermission.provider.TedPermissionProvider

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding // ActivityMainBinding을 지연 초기화합니다.
    private lateinit var backPressedForFinish: BackPressedForFinish
    private lateinit var shakeDetector: ShakeDetector


    companion object {
        const val TAG = "MainActivity"
        const val MAIN_URL = BuildConfig.MAIN_URL
        private const val REQUEST_CODE_NOTIFICATION = 2000

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        initFireBase()
        //쉐이크 하면 바코드 팝업
        shakeDetector = ShakeDetector(this) {
            showBarcodePopup()
        }
        //btn 셋업
        setupButtons()
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initFireBase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                token?.let {
//                    tedShowToast("토큰: $it")
                    Log.e(TAG, "토큰 값: $it")

                }
                permissionNotification()//알림권한요청
            } else {
                // 토큰을 가져오는 데 실패한 경우
                tedShowToast("토큰을 가져오는 데 실패했습니다.")
            }
        }
    }

    //     알림 접근 권한
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun permissionNotification() {
        TedPermission.create()
            .setDeniedMessage(R.string.string_common_notification_alert)
            .setPermissions(Manifest.permission.POST_NOTIFICATIONS)
            .setPermissionListener(object : PermissionListener {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onPermissionGranted() {
                    //이미 권한이 있거나 사용자가 권한을 허용했을 때 호출
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            REQUEST_CODE_NOTIFICATION
                        )
                    }
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    //요청이 거부 되었을 때 호출
                }
            }).check()

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
        btnBarcode.setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity,R.drawable.ic_open_barcode))
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

    private fun tedShowToast(message: String) {
        Toast.makeText(TedPermissionProvider.context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.unregisterListener()

    }
}