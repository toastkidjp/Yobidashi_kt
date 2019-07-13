/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.core.content.ContextCompat
import jp.toastkid.yobidashi.R
import timber.log.Timber

/**
 * @author toastkidjp
 */
class CustomViewSwitcher(
        private val contextSupplier: () -> Context,
        private val currentWebViewSupplier: () -> View?
) {

    private var customViewContainer: FrameLayout? = null

    private var videoView: VideoView? = null

    private var originalOrientation: Int = 0

    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private var customView: View? = null

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        val activity = contextSupplier() as? Activity ?: return
        activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        originalOrientation = activity.requestedOrientation

        activity.requestedOrientation = activity.requestedOrientation

        customViewContainer = FrameLayout(activity)
        customViewContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.filter_white_aa))
        if (view is FrameLayout) {
            val child = view.focusedChild
            if (child is VideoView) {
                videoView = child
                videoView?.setOnErrorListener(VideoCompletionListener())
                videoView?.setOnCompletionListener(VideoCompletionListener())
            }
        } else if (view is VideoView) {
            videoView = view
            videoView?.setOnErrorListener(VideoCompletionListener())
            videoView?.setOnCompletionListener(VideoCompletionListener())
        }

        customViewCallback = callback
        customView = view

        val decorView = activity.window.decorView as FrameLayout
        decorView.addView(customViewContainer, customViewParams)
        customViewContainer?.addView(customView, customViewParams)
        decorView.requestLayout()

        currentWebViewSupplier()?.visibility = View.INVISIBLE
    }

    fun onHideCustomView() {
        val activity = contextSupplier() as? Activity ?: return

        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (customViewCallback != null) {
            try {
                customViewCallback?.onCustomViewHidden()
            } catch (e: Exception) {
                Timber.w("Error hiding custom view", e)
            }

            customViewCallback = null
        }

        currentWebViewSupplier()?.visibility = View.VISIBLE

        if (customViewContainer != null) {
            val parent = customViewContainer?.parent as? ViewGroup
            parent?.removeView(customViewContainer)
            customViewContainer?.removeAllViews()
        }

        customViewContainer?.removeAllViews()
        customViewContainer?.setBackgroundColor(Color.TRANSPARENT)

        customViewContainer = null
        customView = null

        videoView?.stopPlayback()
        videoView?.setOnErrorListener(null)
        videoView?.setOnCompletionListener(null)
        videoView = null

        try {
            customViewCallback?.onCustomViewHidden()
        } catch (e: Exception) {
            Timber.w(e)
        }

        customViewCallback = null
        activity.requestedOrientation = originalOrientation
    }

    private inner class VideoCompletionListener
        : MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = false

        override fun onCompletion(mp: MediaPlayer) = onHideCustomView()

    }

    companion object {
        private val customViewParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        )
    }
}