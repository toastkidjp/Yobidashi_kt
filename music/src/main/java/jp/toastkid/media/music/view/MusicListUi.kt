/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.music.view

import android.content.ComponentName
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.media.R
import jp.toastkid.media.music.MediaPlayerService
import jp.toastkid.media.music.popup.MediaPlayerPopupViewModel
import jp.toastkid.media.music.popup.playback.speed.PlayingSpeed
import kotlinx.coroutines.launch

@Composable
fun MusicListUi() {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val viewModelProvider = ViewModelProvider(activity)
    val contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
    val mediaPlayerPopupViewModel = remember { MediaPlayerPopupViewModel() }

    var mediaBrowser: MediaBrowserCompat? = null

    val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                    mediaPlayerPopupViewModel.previous()?.let {
                        play(it, attemptToGetMediaController(activity), mediaPlayerPopupViewModel)
                    }
                }
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                    mediaPlayerPopupViewModel.next()?.let {
                        play(it, attemptToGetMediaController(activity), mediaPlayerPopupViewModel)
                    }
                }
                PlaybackStateCompat.STATE_PLAYING -> mediaPlayerPopupViewModel.playing = true
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_STOPPED -> mediaPlayerPopupViewModel.playing = false
                else -> Unit
            }
        }
    }

    val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            if (children.isEmpty()) {
                //hide()
                return
            }

            attemptToGetMediaController(activity)?.also {
                it.transportControls?.prepare()
            }

            mediaPlayerPopupViewModel.nextMusics(children)
        }
    }

    val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            val mediaBrowserNonNull = mediaBrowser ?: return
            val sessionToken: MediaSessionCompat.Token = mediaBrowserNonNull.sessionToken
            val mediaControllerCompat =
                MediaControllerCompat(activity, sessionToken)
            mediaControllerCompat.registerCallback(controllerCallback)
            MediaControllerCompat.setMediaController(activity, mediaControllerCompat)

            mediaBrowserNonNull.subscribe(mediaBrowserNonNull.root, subscriptionCallback)
        }
    }
    mediaBrowser = remember {
        MediaBrowserCompat(
            activity,
            ComponentName(activity, MediaPlayerService::class.java),
            connectionCallback,
            null
        )
    }

    if (mediaBrowser.isConnected.not()) {
        mediaBrowser.connect()
    }

    MusicList(
        {
            play(it, attemptToGetMediaController(activity), mediaPlayerPopupViewModel)
        },
        {
            contentViewModel.open("https://www.google.com/search?q=$it Lyrics".toUri())
        },
        {
            mediaPlayerPopupViewModel.previous()?.let {
                play(it, attemptToGetMediaController(activity), mediaPlayerPopupViewModel)
            }
        },
        {
            mediaPlayerPopupViewModel.next()?.let {
                play(it, attemptToGetMediaController(activity), mediaPlayerPopupViewModel)
            }
        },
        { stop(attemptToGetMediaController(activity), mediaPlayerPopupViewModel) },
        { switchState(attemptToGetMediaController(activity), mediaPlayerPopupViewModel) },
        {
            val random = mediaPlayerPopupViewModel.musics.random()
            play(random, attemptToGetMediaController(activity), mediaPlayerPopupViewModel)
        },
        mediaPlayerPopupViewModel.musics,
        mediaPlayerPopupViewModel.playing
    )
}

@Composable
internal fun MusicList(
    onClickItem: (MediaBrowserCompat.MediaItem) -> Unit,
    onClickLyrics: (String) -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    stop: () -> Unit,
    switchState: () -> Unit,
    shuffle: () -> Unit,
    mediaItems: SnapshotStateList<MediaBrowserCompat.MediaItem>,
    playing: Boolean
) {
    val context = LocalContext.current
    val contentViewModel = (context as? ComponentActivity)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }
    var expanded by remember { mutableStateOf(false) }
    @StringRes var currentSpeed by remember { mutableStateOf(PlayingSpeed.getDefault().textId) }
    val sendSpeedBroadcast: (Float) -> Unit = { speed ->
        context.sendBroadcast(MediaPlayerService.makeSpeedIntent(speed))
    }

    val coroutineScope = rememberCoroutineScope()

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_stop),
                contentDescription = stringResource(id = R.string.action_stop),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { stop() }
            )
            Icon(
                painterResource(R.drawable.ic_previous_media),
                contentDescription = stringResource(id = R.string.action_skip_to_previous),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { previous() }
            )
            Icon(
                painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play_media),
                contentDescription = stringResource(id = if (playing) R.string.action_pause else R.string.action_play),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { switchState() }
            )
            Icon(
                painterResource(R.drawable.ic_next_media),
                contentDescription = stringResource(id = R.string.action_skip_to_next),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { next() }
            )
            Icon(
                painterResource(R.drawable.ic_shuffle),
                contentDescription = stringResource(id = R.string.action_shuffle),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { shuffle() }
            )

            Box(
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .clickable {
                        expanded = true
                    }
            ) {
                Text(
                    text = stringResource(id = currentSpeed),
                    style = TextStyle(MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold),
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val values = PlayingSpeed.values()
                    values.forEachIndexed { index, _ ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(id = values[index].textId),
                                    fontSize = 20.sp
                                )
                            },
                            onClick = {
                            currentSpeed = values[index].textId
                            sendSpeedBroadcast(values[index].speed)
                            expanded = false
                        })
                    }
                }
            }

            Icon(
                painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.close),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable {
                        coroutineScope.launch {
                            contentViewModel?.hideBottomSheet()
                        }
                    }
            )
        }
        LazyColumn {
            items(mediaItems, { it.description.mediaId ?: "" }) { music ->
                Surface(
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onClickItem(music) }
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            music.description.iconUri,
                            contentDescription = "TODO",
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center,
                            placeholder = painterResource(id = R.drawable.ic_music),
                            modifier = Modifier
                                .width(44.dp)
                                .fillMaxHeight()
                                .padding(end = 4.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = music.description.title.toString(),
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = music.description.subtitle.toString(),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            painterResource(id = R.drawable.ic_lyrics),
                            contentDescription = "TODO",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight()
                                .clickable {
                                    onClickLyrics(music.description.title?.toString() ?: "")
                                }
                        )
                    }
                }
            }
        }
    }
}

fun switchState(
    mediaController: MediaControllerCompat?,
    mediaPlayerPopupViewModel: MediaPlayerPopupViewModel
) {
    mediaController?.metadata ?: return
    when (mediaController.playbackState.state) {
        PlaybackStateCompat.STATE_PLAYING -> pause(mediaController, mediaPlayerPopupViewModel)
        PlaybackStateCompat.STATE_PAUSED -> play(mediaController, mediaPlayerPopupViewModel)
        else -> Unit
    }
}

private fun play(
    mediaItem: MediaBrowserCompat.MediaItem,
    mediaController: MediaControllerCompat?,
    mediaPlayerPopupViewModel: MediaPlayerPopupViewModel
) {
    val mediaUri = mediaItem.description.mediaUri
    if (mediaUri == null || mediaUri == Uri.EMPTY) {
        return
    }

    mediaPlayerPopupViewModel.current.value = mediaItem

    mediaController
        ?.transportControls
        ?.playFromUri(mediaUri, bundleOf())
}

private fun stop(
    mediaController: MediaControllerCompat,
    mediaPlayerPopupViewModel: MediaPlayerPopupViewModel
) {
    mediaController.metadata ?: return

    mediaController.transportControls.stop()

    mediaPlayerPopupViewModel.playing = false

    mediaPlayerPopupViewModel.current.value = null
}

private fun play(
    mediaController: MediaControllerCompat,
    mediaPlayerPopupViewModel: MediaPlayerPopupViewModel
) {
    mediaController.metadata ?: return

    mediaController.transportControls.play()

    mediaPlayerPopupViewModel.playing = true
}

private fun pause(
    mediaController: MediaControllerCompat,
    mediaPlayerPopupViewModel: MediaPlayerPopupViewModel
) {
    mediaController.metadata ?: return

    mediaController.transportControls.pause()

    mediaPlayerPopupViewModel.playing = false
}

private fun attemptToGetMediaController(activity: ComponentActivity) =
    MediaControllerCompat.getMediaController(activity)