package com.babitalk.android.gallerypicker.common

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.babitalk.android.gallerypicker.R
import com.babitalk.android.gallerypicker.ui.common.toColor

abstract class BaseActivity<T : ViewBinding>: AppCompatActivity() {
    protected val binding: T by lazy { createViewBinding() }
    abstract fun createViewBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.R.color.black.toColor()
        initViews()
        subscribeUi()
        loadData()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setToolbar()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setToolbar()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        setToolbar()
    }

    private fun setToolbar() {
        val toolbar = findViewById<Toolbar?>(R.id.toolbar) ?: return
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    protected abstract fun initViews()
    protected abstract fun subscribeUi()
    protected abstract fun loadData()
}