package io.agora.livestreaming.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import io.agora.livestreaming.tools.LogTools

abstract class BaseUiActivity<B : ViewBinding> : AppCompatActivity(){
    lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = getViewBinding(layoutInflater)
        if (binding == null) {
            LogTools.e("Inflate Error")
            finish()
        } else {
            this.binding = binding
            super.setContentView(this.binding.root)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        initView()
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater): B?

    abstract fun initView()
}