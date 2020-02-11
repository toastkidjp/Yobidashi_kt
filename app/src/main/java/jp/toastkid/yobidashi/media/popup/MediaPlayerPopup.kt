/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.popup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.media.session.PlaybackState
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.databinding.PopupMediaPlayerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.Adapter
import jp.toastkid.yobidashi.media.MediaPlayerPopupViewModel
import jp.toastkid.yobidashi.media.MediaPlayerService
import timber.log.Timber

/**
 * @author toastkidjp
 */
class MediaPlayerPopup(private val context: Context) {

    /**
     * Popup window.
     */
    private val popupWindow = PopupWindow(context)

    /**
     * View binding.
     */
    private val binding: PopupMediaPlayerBinding

    private var adapter: Adapter? = null

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val preferenceApplier = PreferenceApplier(context)

    private val heightPixels = context.resources.displayMetrics.heightPixels

    private val headerHeight = context.resources.getDimensionPixelSize(R.dimen.floating_preview_header_height)

    private val swipeLimit = heightPixels - headerHeight

    private val pauseBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pause)

    private val playBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_play_media)

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            adapter?.clear()

            attemptMediaController()?.also {
                it.transportControls?.prepare()
            }
            children.forEach { adapter?.add(it) }

            attemptExtractActivity()?.runOnUiThread { adapter?.notifyDataSetChanged() }
        }
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            attemptExtractActivity()?.also {
                MediaControllerCompat.setMediaController(
                        it,
                        MediaControllerCompat(context, mediaBrowser.sessionToken)
                )
            }

            mediaBrowser.subscribe(mediaBrowser.root, subscriptionCallback)
            attemptMediaController()?.registerCallback(controllerCallback)
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (attemptMediaController()?.playbackState?.state) {
                PlaybackState.STATE_PLAYING -> setPlayIcon()
                PlaybackState.STATE_PAUSED -> setPauseIcon()
                PlaybackState.STATE_STOPPED -> setPlayIcon()
                else -> Unit
            }
        }
    }

    private var mediaPlayerPopupViewModel: MediaPlayerPopupViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private val disposables = CompositeDisposable()

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.popup_media_player, null, false)
        binding.popup = this

        initializeViewModels()

        adapter = Adapter(
                LayoutInflater.from(context),
                preferenceApplier,
                context.resources,
                mediaPlayerPopupViewModel
        )

        observeViewModels()

        binding.mediaList.adapter = adapter
        binding.mediaList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        applyColors()

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
        (attemptExtractActivity() as? FragmentActivity)?.also {
            mediaPlayerPopupViewModel?.clickItem?.observe(it, Observer {
                attemptMediaController()
                        ?.transportControls
                        ?.playFromUri(it.description.mediaUri, bundleOf())
            })
            mediaPlayerPopupViewModel?.clickLyrics?.observe(it, Observer {
                browserViewModel?.preview("https://www.google.com/search?q=$it Lyrics".toUri())
            })
        }
    }

    private fun applyColors() {
        val colorPair = preferenceApplier.colorPair()
        binding.control.setBackgroundColor(colorPair.bgColor())

        val fontColor = colorPair.fontColor()
        binding.reset.setColorFilter(fontColor)
        binding.playSwitch.setColorFilter(fontColor)
        binding.shuffle.setColorFilter(fontColor)
        binding.hide.setColorFilter(fontColor)
    }

    private fun initializePopupWindow() {
        popupWindow.contentView = binding.root

        popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparent)))

        popupWindow.isClippingEnabled = false

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    fun switchState() {
        val mediaController = attemptMediaController() ?: return
        mediaController.metadata ?: return
        when (mediaController.playbackState.state) {
            PlaybackState.STATE_PLAYING -> pause()
            PlaybackState.STATE_PAUSED -> play()
            else -> Unit
        }
    }

    private fun play() {
        val mediaController = attemptMediaController() ?: return
        mediaController.metadata ?: return

        mediaController.transportControls.play()

        setPlayIcon()
    }

    private fun setPlayIcon() {
        binding.playSwitch.setImageBitmap(pauseBitmap)
        binding.playSwitch.setColorFilter(preferenceApplier.fontColor)
    }

    private fun pause() {
        val mediaController = attemptMediaController() ?: return
        mediaController.metadata ?: return

        mediaController.transportControls.pause()

        setPauseIcon()
    }

    private fun setPauseIcon() {
        binding.playSwitch.setImageBitmap(playBitmap)
        binding.playSwitch.setColorFilter(preferenceApplier.fontColor)
    }

    fun shuffle() {
        attemptMediaController()
                ?.transportControls
                ?.playFromUri(adapter?.random()?.description?.mediaUri, bundleOf())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSlidingListener() {
        val slidingTouchListener = SlidingTouchListener(binding.contentPanel)
        slidingTouchListener.setCallback(object : SlidingTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                if (y > swipeLimit) {
                    return
                }
                popupWindow.update(-1, -y.toInt(), -1, -1)
            }
        })
        binding.header.setOnTouchListener(slidingTouchListener)
    }

    fun show(parent: View) {
        if (popupWindow.isShowing) {
            hide()
            return
        }

        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, -600)

        val attemptExtractActivity = attemptExtractActivity() ?: return
        RxPermissions(attemptExtractActivity)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it) {
                                mediaBrowser.connect()
                                return@subscribe
                            }

                            Toaster.snackShort(
                                    parent,
                                    R.string.message_audio_file_is_not_found,
                                    PreferenceApplier(binding.root.context).colorPair()
                            )
                            popupWindow.dismiss()
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    private fun initializeViewModels() {
        val viewModelProvider = (attemptExtractActivity() as? FragmentActivity)
                ?.let { owner -> ViewModelProviders.of(owner) }
        browserViewModel = viewModelProvider?.get(BrowserViewModel::class.java)
        mediaPlayerPopupViewModel = viewModelProvider?.get(MediaPlayerPopupViewModel::class.java)
    }

    fun hide() {
        popupWindow.dismiss()

        attemptMediaController()?.also {
            it.unregisterCallback(controllerCallback)
            mediaBrowser.disconnect()
        }

        disposables.clear()
    }

    fun reset() {
        attemptMediaController()?.transportControls?.stop()
    }

    private fun attemptMediaController() =
            attemptExtractActivity()?.let { MediaControllerCompat.getMediaController(it) }

    private fun attemptExtractActivity() = (context as? Activity)

}