/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.AlbumArtFinder

/**
 * @author toastkidjp
 */
class NotificationFactory(
        private val context: Context,
        private val mediaSessionSupplier: () -> MediaSessionCompat
) {

    private val preferenceApplier = PreferenceApplier(context)

    private val albumArtFinder = AlbumArtFinder(context.contentResolver)

    operator fun invoke(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val currentDescription = mediaSessionSupplier().controller.metadata.description
        val bitmap = currentDescription.iconUri?.let { albumArtFinder(it) }
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionSupplier().sessionToken)
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
                                context,
                                PlaybackStateCompat.ACTION_STOP
                        )
                )

        val action = if (mediaSessionSupplier().controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            NotificationCompat.Action(
                    R.drawable.ic_pause,
                    context.getString(R.string.action_pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                    R.drawable.ic_play_media,
                    context.getString(R.string.action_play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
            )
        }
        notificationBuilder.addAction(action)
        return notificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.title_audio_player),
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.title_audio_player)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "music"
    }
}