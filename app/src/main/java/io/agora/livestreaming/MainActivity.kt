package io.agora.livestreaming

import android.text.Editable
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import io.agora.livestreaming.base.BaseUiActivity
import io.agora.livestreaming.databinding.ActivityMainBinding
import io.agora.livestreaming.tools.StatusBarCompat

class MainActivity : BaseUiActivity<ActivityMainBinding>() {

    override fun getViewBinding(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        StatusBarCompat.setLightStatusBar(this, false)
        binding.btSubmit.setOnClickListener {
            LivingActivity.start(
                this,
                binding.etChannel.text.toString(),
                binding.etFlv.text.toString(),
                binding.etHls.text.toString()
            )
        }
        binding.etChannel.doAfterTextChanged { editable ->
            if (isEtNullOrEmpty(editable)) {
                disableSubmit()
            } else {
                if (isEtNullOrEmpty(binding.etFlv.text) || isEtNullOrEmpty(binding.etHls.text)) {
                    disableSubmit()
                } else {
                    enableSubmit()
                }
            }
        }
        binding.etFlv.doAfterTextChanged { editable ->
            if (isEtNullOrEmpty(editable)) {
                disableSubmit()
            } else {
                if (isEtNullOrEmpty(binding.etChannel.text) || isEtNullOrEmpty(binding.etHls.text)) {
                    disableSubmit()
                } else {
                    enableSubmit()
                }
            }
        }
        binding.etHls.doAfterTextChanged { editable ->
            if (isEtNullOrEmpty(editable)) {
                disableSubmit()
            } else {
                if (isEtNullOrEmpty(binding.etChannel.text) || isEtNullOrEmpty(binding.etFlv.text)) {
                    disableSubmit()
                } else {
                    enableSubmit()
                }
            }
        }
    }

    private fun isEtNullOrEmpty(editable: Editable?): Boolean {
        return editable.isNullOrEmpty()
    }

    private fun disableSubmit() {
        binding.btSubmit.isEnabled = false
        binding.btSubmit.alpha = 0.3f
    }

    private fun enableSubmit() {
        binding.btSubmit.isEnabled = true
        binding.btSubmit.alpha = 1f
    }
}