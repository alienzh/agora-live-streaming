package io.agora.livestreaming.base

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.livestreaming.databinding.DialogCenterFragmentAlertBinding
import io.agora.livestreaming.tools.DeviceTools

/**
 * 中间弹框，确认/取消按钮
 */
class CommonFragmentAlertDialog constructor() : BaseFragmentDialog<DialogCenterFragmentAlertBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): DialogCenterFragmentAlertBinding {
        return DialogCenterFragmentAlertBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        mBinding?.apply {
            setDialogSize(view)
            if (!TextUtils.isEmpty(titleText)) {
                mtTitle.text = titleText
            } else {
                mtTitle.isVisible = false
                // 更改间距
                val layoutParams: ConstraintLayout.LayoutParams = mbLeft.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.setMargins(layoutParams.marginStart, DeviceTools.dp2px(root.context,34f), layoutParams.marginEnd, layoutParams.bottomMargin)
                mbLeft.layoutParams = layoutParams
            }
            if (!TextUtils.isEmpty(contentText)) {
                mtContent.text = contentText
            }
            if (!TextUtils.isEmpty(leftText)) {
                mbLeft.text = leftText
            }
            if (!TextUtils.isEmpty(rightText)) {
                mbRight.text = rightText
            }
            mbLeft.setOnClickListener {
                clickListener?.onCancelClick()
                dismiss()
            }
            mbRight.setOnClickListener {
                clickListener?.onConfirmClick()
                dismiss()
            }
        }
    }

    private fun setDialogSize(view: View) {
        mBinding?.apply {
            val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
            layoutParams.width = DeviceTools.dp2px(root.context,300f)
            view.layoutParams = layoutParams
        }
    }

    private var titleText: String = ""
    private var contentText: String = ""
    private var leftText: String = ""
    private var rightText: String = ""
    private var clickListener: OnClickBottomListener? = null

    fun titleText(titleText: String) = apply {
        this.titleText = titleText
    }

    fun contentText(contentText: String) = apply {
        this.contentText = contentText
    }

    fun leftText(leftText: String) = apply {
        this.leftText = leftText
    }

    fun rightText(rightText: String) = apply {
        this.rightText = rightText
    }

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        fun onConfirmClick()

        /**
         * 点击取消按钮事件
         */
        fun onCancelClick() {}
    }
}