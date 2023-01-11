package io.agora.livestreaming.tools

import android.content.Context
import android.content.res.Resources
import android.util.Size

/**
 * @author create by zhangwei03
 *
 */
object DeviceTools {

    @JvmStatic
    fun getDisplaySize(): Size {
        val metrics = Resources.getSystem().displayMetrics
        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    @JvmStatic
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }
}

