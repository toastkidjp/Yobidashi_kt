/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import jp.toastkid.barcode.generator.BarcodeGenerator
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PageInformationDialog(
    openState: MutableState<Boolean>,
    pageInformationBundle: Bundle
) {
    val favicon = pageInformationBundle.getParcelable<Bitmap>("favicon") ?: return
    val title = pageInformationBundle.getString("title") ?: return
    val url = pageInformationBundle.getString("url") ?: return
    val cookie = pageInformationBundle.getString("cookie") ?: return

    val barcode = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(key1 = "generate_barcode") {
        withContext(Dispatchers.IO) {
            barcode.value = BarcodeGenerator().invoke(url, 400)
        }
    }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = { openState.value = false },
        content = {
            Surface(
                color = colorResource(id = R.color.soft_background),
                elevation = 4.dp
            ) {
                Box() {
                    Column() {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AsyncImage(
                                model = favicon,
                                contentDescription = stringResource(id = R.string.title_icon),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                title,
                                style = MaterialTheme.typography.h5,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "URL: $url",
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.black),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            AsyncImage(
                                barcode.value,
                                contentDescription = stringResource(id = R.string.title_instant_barcode),
                                modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)
                            )

                            Text(
                                text = "Cookie:${System.lineSeparator()}${
                                    cookie?.replace(
                                        ";",
                                        ";${System.lineSeparator()}"
                                    )
                                }",
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.black),
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_clip_cookie),
                            color = colorResource(id = R.color.colorPrimary),
                            modifier = Modifier
                                .clickable {
                                    openState.value = false
                                    clipText(context, cookie)
                                }
                                .padding(16.dp)
                        )

                        Text(
                            text = stringResource(id = R.string.close),
                            color = colorResource(id = R.color.colorPrimary),
                            modifier = Modifier
                                .clickable {
                                    openState.value = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}


/**
 * Copy URL to Clipboard.
 *
 * @param copyText text for clipping (Nullable)
 * @param d [DialogInterface]
 */
private fun clipText(context: Context, copyText: String?) {
    copyText?.also { Clipboard.clip(context, it) }

    Toaster.tShort(
        context,
        "It has copied URL to clipboard.${System.lineSeparator()}$copyText"
    )
}
