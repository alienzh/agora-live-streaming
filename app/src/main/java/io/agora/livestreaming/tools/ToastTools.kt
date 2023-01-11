package io.agora.livestreaming.tools

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * @author create by zhangwei03
 */
object ToastTools {

    @JvmStatic
    fun toastShort(activity: Activity, @StringRes stringRes: Int) {
        toast(activity, stringRes, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun toastShort(activity: Activity, message: String) {
        toast(activity, message, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun toastLong(activity: Activity, @StringRes stringRes: Int) {
        toast(activity, stringRes, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun toastLong(activity: Activity, message: String) {
        toast(activity, message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    private fun toast(activity: Activity, @StringRes stringRes: Int, length: Int) {
        Toast.makeText(activity, stringRes, length).show()
    }

    @JvmStatic
    private fun toast(activity: Activity, message: String, length: Int) {
        Toast.makeText(activity, message, length).show()
    }
}