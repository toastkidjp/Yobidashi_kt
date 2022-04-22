/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.media.music.popup

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.view.SlidingTapListener
import jp.toastkid.media.R
import jp.toastkid.media.music.MediaPlayerService
import kotlinx.coroutines.Job

/**
 * @author toastkidjp
 */
class MediaPlayerPopup(private val context: Context) {

    /**
     * Popup window.
     */
    private val popupWindow = PopupWindow(context)

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val resources = context.resources

    private val heightPixels = resources.displayMetrics.heightPixels

    private val headerHeight = resources.getDimensionPixelSize(R.dimen.media_player_popup_header_height)

    private val swipeLimit = heightPixels - (headerHeight / 2)

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            if (children.isEmpty()) {
                hide()
                return
            }

            attemptMediaController()?.also {
                it.transportControls?.prepare()
            }

            mediaPlayerPopupViewModel?.nextMusics(children)
        }
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            attemptExtractActivity()?.also {
                val mediaControllerCompat = MediaControllerCompat(context, mediaBrowser.sessionToken)
                mediaControllerCompat.registerCallback(controllerCallback)
                MediaControllerCompat.setMediaController(
                    it,
                    mediaControllerCompat
                )
            }

            mediaBrowser.subscribe(mediaBrowser.root, subscriptionCallback)
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> setPauseIcon()
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_STOPPED -> setPlayIcon()
                else -> Unit
            }
        }
    }

    private var mediaPlayerPopupViewModel: MediaPlayerPopupViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private val disposables: Job by lazy { Job() }

    init {
        initializeViewModels()

        observeViewModels()

        //TODO applyColors()

        initializePopupWindow()

        setSlidingListener()

        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MediaPlayerService::class.java),
            connectionCallback,
            null
        )
    }

    private fun observeViewModels() {
        (attemptExtractActivity())?.also { activity ->
            mediaPlayerPopupViewModel?.clickItem?.observe(activity, {
                attemptMediaController()
                    ?.transportControls
                    ?.playFromUri(it.description.mediaUri, bundleOf())
            })
            mediaPlayerPopupViewModel?.clickLyrics?.observe(activity, {
                browserViewModel?.preview("https://www.google.com/search?q=$it Lyrics".toUri())
            })
        }
    }

    /*private fun applyColors() {
        val colorPair = preferenceApplier.colorPair()
        binding.control.setBackgroundColor(colorPair.bgColor())

        val fontColor = colorPair.fontColor()
        binding.reset.setColorFilter(fontColor)
        binding.playSwitch.setColorFilter(fontColor)
        binding.shuffle.setColorFilter(fontColor)
        binding.hide.setColorFilter(fontColor)
    }*/

    private fun initializePopupWindow() {
        val composeView = ComposeView(context)
        val activity = context as? FragmentActivity ?: return

        ViewTreeLifecycleOwner.set(composeView, activity)
        ViewTreeSavedStateRegistryOwner.set(composeView, activity)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

        popupWindow.contentView = composeView.also {

        }

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow.isClippingEnabled = false

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        popupWindow.animationStyle = R.style.PopupWindowVisibilityAnimation
    }

    fun switchState() {
        val mediaController = attemptMediaController() ?: return
        mediaController.metadata ?: return
        when (mediaController.playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> pause()
            PlaybackStateCompat.STATE_PAUSED -> play()
            else -> Unit
        }
    }

    private fun play() {
        val mediaController = attemptMediaController() ?: return
        mediaController.metadata ?: return

        mediaController.transportControls.play()

        setPauseIcon()
    }

    private fun pause() {
        val mediaController = attemptMediaController() ?: return
        mediaController.metadata ?: return

        mediaController.transportControls.pause()

        setPlayIcon()
    }

    private fun setPauseIcon() {
        mediaPlayerPopupViewModel?.playing = true
    }

    private fun setPlayIcon() {
        mediaPlayerPopupViewModel?.playing = false
    }

    fun shuffle() {
        attemptMediaController()
            ?.transportControls
            //TODO ?.playFromUri(adapter?.random()?.description?.mediaUri, bundleOf())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSlidingListener() {
        val slidingTouchListener = SlidingTapListener(popupWindow.contentView)
        slidingTouchListener.setCallback(object : SlidingTapListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                if (y > swipeLimit) {
                    return
                }
                popupWindow.update(-1, -(y.toInt() - headerHeight), -1, -1)
            }
        })
        popupWindow.contentView?.setOnTouchListener(slidingTouchListener)
    }

    fun show(parent: View) {
        if (popupWindow.isShowing) {
            hide()
            return
        }

        if (mediaBrowser.isConnected.not()) {
            mediaBrowser.connect()
        }

        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, -600)
    }

    private fun initializeViewModels() {
        val viewModelProvider = attemptExtractActivity()
            ?.let { ViewModelProvider(it) }
        browserViewModel = viewModelProvider?.get(BrowserViewModel::class.java)
        mediaPlayerPopupViewModel = viewModelProvider?.get(MediaPlayerPopupViewModel::class.java)
    }

    fun hide() {
        popupWindow.dismiss()

        attemptMediaController()?.also {
            it.unregisterCallback(controllerCallback)
            mediaBrowser.disconnect()
        }

        disposables.cancel()
    }

    fun reset() {
        attemptMediaController()?.transportControls?.stop()
    }

    private fun attemptMediaController() =
        attemptExtractActivity()?.let { MediaControllerCompat.getMediaController(it) }

    private fun attemptExtractActivity() = (context as? FragmentActivity)

}