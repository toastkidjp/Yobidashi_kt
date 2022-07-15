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
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.media.R
import jp.toastkid.media.music.MediaPlayerService
import jp.toastkid.media.music.popup.MediaPlayerPopupViewModel
import jp.toastkid.media.music.popup.playback.speed.PlayingSpeed
import kotlinx.coroutines.launch

@Composable
fun MusicListUi() {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val viewModelProvider = ViewModelProvider(activity)
    val browserViewModel = viewModelProvider.get(BrowserViewModel::class.java)
    val mediaPlayerPopupViewModel = viewModelProvider.get(MediaPlayerPopupViewModel::class.java)

    var mediaBrowser: MediaBrowserCompat? = null

    val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
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

            mediaPlayerPopupViewModel?.nextMusics(children)
        }
    }

    val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            val mediaBrowser = mediaBrowser ?: return
            activity?.also {
                val mediaControllerCompat =
                    MediaControllerCompat(activity, mediaBrowser?.sessionToken)
                mediaControllerCompat.registerCallback(controllerCallback)
                MediaControllerCompat.setMediaController(
                    it,
                    mediaControllerCompat
                )
            }

            mediaBrowser?.subscribe(mediaBrowser.root, subscriptionCallback)
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
            val mediaUri = it.description.mediaUri
            if (mediaUri == null || mediaUri == Uri.EMPTY) {
                return@MusicList
            }
            attemptToGetMediaController(activity)
                ?.transportControls
                ?.playFromUri(mediaUri, bundleOf())
        },
        {
            browserViewModel.preview("https://www.google.com/search?q=$it Lyrics".toUri())
        },
        { stop(attemptToGetMediaController(activity), mediaPlayerPopupViewModel) },
        { switchState(attemptToGetMediaController(activity), mediaPlayerPopupViewModel) },
        {
            val mediaUri = mediaPlayerPopupViewModel.musics.random()?.description?.mediaUri
            if (mediaUri == null || mediaUri == Uri.EMPTY) {
                return@MusicList
            }

            attemptToGetMediaController(activity)
                ?.transportControls
                ?.playFromUri(
                    mediaUri,
                    bundleOf()
                )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MusicList(
    onClickItem: (MediaBrowserCompat.MediaItem) -> Unit,
    onClickLyrics: (String) -> Unit,
    stop: () -> Unit,
    switchState: () -> Unit,
    shuffle: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = (context as? ComponentActivity)?.let {
        ViewModelProvider(it).get(MediaPlayerPopupViewModel::class.java)
    } ?: return
    val contentViewModel = (context as? ComponentActivity)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }
    val preferenceApplier = PreferenceApplier(context)
    val iconColor = preferenceApplier.colorPair().fontColor()
    var expanded by remember { mutableStateOf(false) }
    @StringRes var currentSpeed by remember { mutableStateOf(PlayingSpeed.getDefault().textId) }
    val sendSpeedBroadcast: (Float) -> Unit = { speed ->
        context.sendBroadcast(MediaPlayerService.makeSpeedIntent(speed))
    }

    val coroutineScope = rememberCoroutineScope()

    //TODO: AsyncImage -> Icon

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colors.primary)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_stop),
                contentDescription = stringResource(id = R.string.action_stop),
                tint = Color(iconColor),
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { stop() }
            )
            Icon(
                painterResource(if (viewModel?.playing == true) R.drawable.ic_pause else R.drawable.ic_play_media),
                contentDescription = stringResource(id = R.string.action_pause),
                tint = Color(iconColor),
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { switchState() }
            )
            Icon(
                painterResource(R.drawable.ic_shuffle),
                contentDescription = stringResource(id = R.string.action_shuffle),
                tint = Color(iconColor),
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
                    style = TextStyle(Color(iconColor), fontWeight = FontWeight.Bold),
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val values = PlayingSpeed.values()
                    values.forEachIndexed { index, s ->
                        DropdownMenuItem(onClick = {
                            currentSpeed = values[index].textId
                            sendSpeedBroadcast(values[index].speed)
                            expanded = false
                        }) {
                            Text(
                                stringResource(id = values[index].textId),
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            AsyncImage(
                R.drawable.ic_close,
                contentDescription = stringResource(id = R.string.close),
                colorFilter = ColorFilter.tint(Color(iconColor), BlendMode.SrcIn),
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
            items(viewModel.musics, { it.description.mediaId ?: "" }) { music ->
                Surface(
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Row(
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
                            tint = Color(iconColor),
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

private fun stop(
    mediaController: MediaControllerCompat,
    mediaPlayerPopupViewModel: MediaPlayerPopupViewModel
) {
    mediaController.metadata ?: return

    mediaController.transportControls.stop()

    mediaPlayerPopupViewModel.playing = false
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