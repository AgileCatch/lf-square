package com.focusone.lfsquare

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.focusone.lfsquare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()

    }

    companion object{
        const val TAG ="MainActivity"
    }

    private fun initView()= with(binding) {
//        var baseUrl = BuildConfig.MAIN_URL
        webView.loadUrl("https://www.lfsquare.co.kr/membership")
//        Log.d(TAG, "MAIN_URL: ${BuildConfig.MAIN_URL}")
    }
}