package com.app.uxis.lf.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "token"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "token : $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.
    }
}