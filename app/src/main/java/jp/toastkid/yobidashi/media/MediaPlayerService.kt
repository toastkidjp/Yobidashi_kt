/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.notification.NotificationFactory
import timber.log.Timber

/**
 * @author toastkidjp
 */
class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var notificationFactory: NotificationFactory

    private val mediaPlayer = MediaPlayer()

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mediaSession.controller.transportControls.pause()
        }
    }

    private val callback = object : MediaSessionCompat.Callback() {
        @PlaybackStateCompat.State
        private var mediaState: Int = PlaybackStateCompat.STATE_NONE

        override fun onPrepare() {
            super.onPrepare()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            registerReceiver(audioNoisyReceiver, audioNoisyFilter)

            mediaPlayer.reset()
            if (uri != null) {
                mediaPlayer.setDataSource(this@MediaPlayerService, uri)
            }
            mediaPlayer.prepare()

            mediaSession.setMetadata(
                    MusicFileFinder(contentResolver).invoke().firstOrNull { it.description?.mediaUri == uri }
            )
            mediaSession.isActive = true

            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            startService(Intent(baseContext, MediaPlayerService::class.java))
            notificationManager.notify(NOTIFICATION_ID, notificationFactory())
            startForeground(NOTIFICATION_ID, notificationFactory())
        }

        override fun onPlay() {
            registerReceiver(audioNoisyReceiver, audioNoisyFilter)

            mediaSession.isActive = true
            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            notificationManager.notify(NOTIFICATION_ID, notificationFactory())
            startForeground(NOTIFICATION_ID, notificationFactory())
        }

        override fun onPause() {
            unregisterReceiver(audioNoisyReceiver)

            mediaSession.isActive = false
            mediaPlayer.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
            notificationManager.notify(NOTIFICATION_ID, notificationFactory())
            stopForeground(false)
        }

        override fun onStop() {
            super.onStop()
            try {
                unregisterReceiver(audioNoisyReceiver)
            } catch (e: IllegalArgumentException) {
                Timber.w(e)
            }
            mediaSession.isActive = false
            mediaPlayer.stop()
            setNewState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            mediaSession.setRepeatMode(repeatMode)
            when (repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    mediaPlayer.isLooping = true
                }
                else -> {
                    mediaPlayer.isLooping = false
                }
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val keyEvent = mediaButtonEvent.getParcelableExtra<KeyEvent?>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
                return false
            }
            return when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_NEXT,
                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> {
                    onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                    true
                }
                else -> super.onMediaButtonEvent(mediaButtonEvent)
            }
        }

        private fun setNewState(@PlaybackStateCompat.State newState: Int) {
            mediaState = newState
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder
                    .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                                    or PlaybackStateCompat.ACTION_STOP
                    )
                    .setState(newState, mediaPlayer.currentPosition.toLong(), 1.0f)
            mediaSession.setPlaybackState(stateBuilder.build())
        }
    }

    override fun onCreate() {
        super.onCreate()

        preferenceApplier = PreferenceApplier(this)
        notificationManager = NotificationManagerCompat.from(baseContext)
        notificationFactory = NotificationFactory(this) { mediaSession }

        mediaSession = MediaSessionCompat(this, javaClass.simpleName).also {
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_STOP
            )
            it.setPlaybackState(stateBuilder.build())
            it.setCallback(callback)
            setSessionToken(sessionToken)
            it.setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                            or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
        }
    }

    override fun onGetRoot(
            clientPackageName: String,
            clientUid: Int,
            rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(
            parentId: String,
            result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(
                MusicFileFinder(contentResolver).invoke()
                        .map { MediaBrowserCompat.MediaItem(it.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE) }
                        .toMutableList()
        )
    }

    companion object {
        private val audioNoisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        private const val ROOT_ID = "media-root"

        private const val NOTIFICATION_ID = 46
    }
}