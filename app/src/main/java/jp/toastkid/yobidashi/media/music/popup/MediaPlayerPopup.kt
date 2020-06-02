/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.session.PlaybackState
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.databinding.PopupMediaPlayerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.permission.RuntimePermissions
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.music.MediaPlayerService
import jp.toastkid.yobidashi.media.music.popup.playback.speed.PlaybackSpeedAdapter
import jp.toastkid.yobidashi.media.music.popup.playback.speed.PlayingSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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

    private val resources = context.resources

    private val heightPixels = resources.displayMetrics.heightPixels

    private val headerHeight = resources.getDimensionPixelSize(R.dimen.media_player_popup_header_height)

    private val swipeLimit = heightPixels - (headerHeight / 2)

    private val pauseBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pause)

    private val playBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_play_media)

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            if (children.isEmpty()) {
                hide()
                return
            }

            adapter?.clear()

            attemptMediaController()?.also {
                it.transportControls?.prepare()
            }

            children.forEach { adapter?.add(it) }

            adapter?.notifyDataSetChanged()
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
                PlaybackStateCompat.STATE_PAUSED -> setPlayIcon()
                PlaybackStateCompat.STATE_STOPPED -> setPlayIcon()
                else -> Unit
            }
        }
    }

    private var mediaPlayerPopupViewModel: MediaPlayerPopupViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private val disposables: Job by lazy { Job() }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, null, false)
        binding.popup = this

        initializeViewModels()

        adapter = Adapter(
                LayoutInflater.from(context),
                preferenceApplier,
                resources,
                mediaPlayerPopupViewModel
        )

        observeViewModels()

        binding.mediaList.adapter = adapter
        binding.mediaList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        initializePlaybackSpeedChanger()

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

    private fun initializePlaybackSpeedChanger() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            binding.playingSpeed.isVisible = false
            return
        }

        binding.playingSpeed.isVisible = true

        binding.playingSpeed.adapter = PlaybackSpeedAdapter(
                LayoutInflater.from(binding.root.context),
                preferenceApplier.colorPair()
        )
        binding.playingSpeed.setSelection(PlayingSpeed.getDefault().findIndex())
        binding.playingSpeed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val speed = PlayingSpeed.findById(id).speed
                val intent = MediaPlayerService.makeSpeedIntent(speed)
                context.sendBroadcast(intent)
            }
        }
    }

    private fun observeViewModels() {
        (attemptExtractActivity())?.also {
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
            PlaybackState.STATE_PLAYING -> pause()
            PlaybackState.STATE_PAUSED -> play()
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
        binding.playSwitch.setImageBitmap(pauseBitmap)
        binding.playSwitch.setColorFilter(preferenceApplier.fontColor)
    }

    private fun setPlayIcon() {
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
        val slidingTouchListener = SlidingTouchListener(binding.popupBackground)
        slidingTouchListener.setCallback(object : SlidingTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                if (y > swipeLimit) {
                    return
                }
                popupWindow.update(-1, -(y.toInt() - headerHeight), -1, -1)
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
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            RuntimePermissions(attemptExtractActivity)
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect {
                        if (it.granted && !mediaBrowser.isConnected) {
                            mediaBrowser.connect()
                            return@collect
                        }

                        Toaster.snackShort(
                                parent,
                                R.string.message_requires_permission_storage,
                                PreferenceApplier(binding.root.context).colorPair()
                        )
                        popupWindow.dismiss()
                    }
        }
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

        disposables.cancel()
    }

    fun reset() {
        attemptMediaController()?.transportControls?.stop()
    }

    private fun attemptMediaController() =
            attemptExtractActivity()?.let { MediaControllerCompat.getMediaController(it) }

    private fun attemptExtractActivity() = (context as? FragmentActivity)

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.popup_media_player

    }
}