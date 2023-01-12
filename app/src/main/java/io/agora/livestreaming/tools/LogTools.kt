package io.agora.livestreaming.tools

import android.util.Log

object LogTools {

    private const val  TAG = "LiveStreaming"

    @JvmStatic
    fun d( message: String) {
        Log.d(TAG, message)
    }

    @JvmStatic
    fun e(message: String) {
        Log.e(TAG, message)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
}