/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.ui.parts.SingleLineText
import jp.toastkid.web.R

@Composable
internal fun AnchorLongTapDialog(
    title: String?,
    anchor: String?,
    imageUrl: String?,
    close: () -> Unit
) {
    val context = LocalContext.current
    val contentViewModel = (context as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

    Dialog(
        onDismissRequest = {
            close()
        },
        content = {
            Surface(shadowElevation = 4.dp) {
                Box {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (imageUrl != null && Urls.isValidUrl(imageUrl)) {
                                AsyncImage(
                                    imageUrl,
                                    stringResource(id = R.string.image),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                            Text(
                                makeTitleText(title, anchor, imageUrl),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (anchor != null && Urls.isValidUrl(anchor)) {
                                SingleLineText(R.string.row_dialog_open_new) {
                                    contentViewModel?.open(anchor.toUri())
                                    close()
                                }
                                SingleLineText(R.string.row_dialog_open_background) {
                                    contentViewModel?.openBackground(anchor.toUri())
                                    close()
                                }
                                SingleLineText(R.string.row_dialog_preview) {
                                    contentViewModel?.preview(anchor)
                                    close()
                                }
                            }

                            /*TODO if (imageUrl != null && Urls.isValidUrl(imageUrl)) {
                                SingleLineText(R.string.row_dialog_image_search) {
                                    contentViewModel?.open(ImageSearchUrlGenerator()(imageUrl))
                                    close()
                                }
                            }*/

                            if (imageUrl != null && Urls.isValidUrl(imageUrl)) {
                                SingleLineText(R.string.row_dialog_download) {
                                    if (Urls.isInvalidUrl(imageUrl)) {
                                        (context as? ViewModelStoreOwner)?.let {
                                            ViewModelProvider(it).get(ContentViewModel::class.java)
                                        }?.snackShort(R.string.message_cannot_downloading_image)

                                        close()
                                        return@SingleLineText
                                    }

                                    contentViewModel?.download(imageUrl)
                                    close()
                                }
                            }

                            val clipLink = when {
                                anchor != null && Urls.isValidUrl(anchor) -> anchor
                                imageUrl != null -> imageUrl
                                else -> null
                            }
                            if (clipLink != null) {
                                SingleLineText(R.string.row_dialog_copy) {
                                    Clipboard.clip(context, clipLink)
                                    close()
                                }
                            }

                            if (title != null && title.isNotBlank()) {
                                SingleLineText(R.string.row_dialog_copy_text) {
                                    Clipboard.clip(context, title)
                                    close()
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {

                        Text(
                            text = stringResource(id = R.string.close),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    close()
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}

private fun makeTitleText(title: String?, anchor: String?, imageUrl: String?) =
    when {
        title != null && title.isNotBlank() -> "$title"
        anchor != null && anchor.isNotBlank() -> "Link to: $anchor"
        else -> "Image: $imageUrl"
    }
