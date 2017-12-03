package net.webdefine.gazautoclub.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import android.webkit.WebChromeClient
import android.webkit.WebView

fun View.showIf(show: Boolean) {
    if (show) {
        show()
    } else {
        hide()
    }
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

inline fun WebView.setProgressChangedListener(crossinline onProgressChanged: (Int) -> Unit) {
    this.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            onProgressChanged.invoke(newProgress)
        }
    }
}

fun View.circularReveal(backgroundColor: Int) {
    val showAndSetBackgroundColorFunction = {
        this.setBackgroundColor(backgroundColor)
        this.visibility = View.VISIBLE
    }

    this.post {
        val cx = this.width / 2
        val cy = this.height / 2
        val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

        try {
            val animator = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
            animator.startDelay = 50
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    showAndSetBackgroundColorFunction.invoke()
                }
            })
            animator.start()
        } catch(e: Exception) {
            //TODO
        }
    }
}
