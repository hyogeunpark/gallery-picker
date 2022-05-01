package com.babitalk.android.gallerypicker.ui.common

import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.babitalk.android.gallerypicker.GalleryPickerApplication

fun Int.toColor() = ContextCompat.getColor(GalleryPickerApplication.sContext, this)

fun Int.dpToPx(): Int {
    val resources = GalleryPickerApplication.sContext.resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics).toInt()
}

fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun ImageView.loadUri(uri: Uri?) {
    val requestUri = uri ?: return
    com.bumptech.glide.Glide.with(this)
        .load(requestUri)
        .thumbnail(0.1f)
        .format(com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888)
        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.RESOURCE)
        .into(this)
}

// TODO : 문제점은 expand 할 때 깜빡거림
fun View.expand() {
    if (this@expand.visibility == View.VISIBLE) return
    try {
        this@expand.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = this@expand.measuredHeight

        // Older versions of Android (pre API 21) cancel animations for views with a height of 0.
        this@expand.layoutParams.height = 1
        this@expand.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                this@expand.layoutParams.height = if (interpolatedTime == 1f) targetHeight else (targetHeight * interpolatedTime).toInt()
                this@expand.requestLayout()
            }

            override fun willChangeBounds(): Boolean = true
        }

        // Expansion speed of 1dp/ms
        a.duration = (targetHeight / this@expand.context.resources.displayMetrics.density).toLong() * 4
        this@expand.startAnimation(a)
    } catch (ignored: Exception) { }
}

fun View.collapse() {
    if (this@collapse.visibility == View.GONE) return
    val initialHeight = this@collapse.measuredHeight
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                this@collapse.visibility = View.GONE
            } else {
                this@collapse.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                this@collapse.requestLayout()
            }
        }
        override fun willChangeBounds(): Boolean = true
    }

    // Collapse speed of 1dp/ms
    a.duration = (initialHeight / this@collapse.context.resources.displayMetrics.density).toLong() * 4
    this@collapse.startAnimation(a)
}

fun View.changeBoundsAnimation() {
    TransitionManager.beginDelayedTransition(this.rootView as ViewGroup, ChangeBounds())
}