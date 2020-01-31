/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * @author toastkidjp
 */
class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var albumArtFinder: AlbumArtFinder

    private val mediaPlayer = MediaPlayer();

    private val audioNoisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

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
            mediaPlayer.setDataSource(this@MediaPlayerService, uri)
            mediaPlayer.prepare()

            mediaSession.setMetadata(
                    MusicFileFinder(contentResolver).invoke().firstOrNull { it.description?.mediaUri == uri }
            )
            mediaSession.isActive = true

            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            startService(Intent(baseContext, MediaPlayerService::class.java))
            notificationManager.notify(1, buildNotification())
            startForeground(1, buildNotification())
        }

        override fun onPlay() {
            registerReceiver(audioNoisyReceiver, audioNoisyFilter)

            mediaSession.isActive = true
            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            notificationManager.notify(1, buildNotification())
            startForeground(1, buildNotification())
        }

        override fun onPause() {
            unregisterReceiver(audioNoisyReceiver)

            mediaSession.isActive = false
            mediaPlayer.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
            notificationManager.notify(1, buildNotification())
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
        albumArtFinder = AlbumArtFinder(contentResolver)
        notificationManager = NotificationManagerCompat.from(baseContext)

        // TODO rewrite with also.
        mediaSession = MediaSessionCompat(this, javaClass.simpleName).apply {
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_STOP
            )
            setPlaybackState(stateBuilder.build())
            setCallback(callback)
            setSessionToken(sessionToken)
            setFlags(
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
        return BrowserRoot("media-root", null)
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

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val currentDescription = mediaSession.controller.metadata.description
        val bitmap = currentDescription.iconUri?.let { albumArtFinder(it) }
        val notificationBuilder = NotificationCompat.Builder(baseContext, CHANNEL_ID)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)
                )
                .setColor(preferenceApplier.color)
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(bitmap)
                .setContentTitle(currentDescription.title)
                .setContentText(currentDescription.subtitle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this,
                                PlaybackStateCompat.ACTION_STOP
                        )
                )

        val action = if (mediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            NotificationCompat.Action(
                    R.drawable.ic_pause,
                    getString(R.string.action_pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(baseContext, PlaybackStateCompat.ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                    R.drawable.ic_play_media,
                    getString(R.string.action_play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(baseContext, PlaybackStateCompat.ACTION_PLAY)
            )
        }
        notificationBuilder.addAction(action)
        return notificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.title_audio_player),
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.title_audio_player)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "music"
    }
}