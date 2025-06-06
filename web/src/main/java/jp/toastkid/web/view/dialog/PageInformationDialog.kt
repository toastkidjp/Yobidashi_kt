/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.view.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.barcode.generator.BarcodeGenerator
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.intent.BitmapShareIntentFactory
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.web.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun PageInformationDialog(
    pageInformationBundle: Map<String, Any?>,
    onDismissRequest: () -> Unit
) {
    val favicon = pageInformationBundle.get("favicon") as? Bitmap
    val title = pageInformationBundle.get("title")?.toString() ?: return
    val url = pageInformationBundle.get("url")?.toString() ?: return
    val cookie = pageInformationBundle.get("cookie")?.toString() ?: ""

    val barcode = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(key1 = "generate_barcode") {
        withContext(Dispatchers.IO) {
            barcode.value = BarcodeGenerator().invoke(url, 400)
        }
    }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Surface(
                shadowElevation = 4.dp
            ) {
                Box {
                    Column(
                        modifier = Modifier.padding(bottom = 40.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (favicon != null) {
                                EfficientImage(
                                    model = favicon,
                                    contentDescription = stringResource(id = R.string.title_icon),
                                    modifier = Modifier.size(44.dp).padding(end = 8.dp)
                                )
                            }

                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                                    .clickable {
                                        onDismissRequest()
                                        clipText(context, title)
                                    }
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
                                modifier = Modifier.padding(top = 4.dp)
                                    .clickable {
                                        onDismissRequest()
                                        clipText(context, url)
                                    }
                                    .semantics {
                                        contentDescription = context.getString(R.string.button_clip_url)
                                    }
                            )

                            EfficientImage(
                                barcode.value,
                                contentDescription = stringResource(id = R.string.title_instant_barcode),
                                modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)
                                    .clickable {
                                        val bitmap = barcode.value ?: return@clickable
                                        context.startActivity(
                                            BitmapShareIntentFactory().invoke(context, bitmap)
                                        )
                                    }
                            )

                            Text(
                                text = "Cookie:${System.lineSeparator()}${
                                    cookie?.replace(
                                        ";",
                                        ";${System.lineSeparator()}"
                                    )
                                }",
                                fontSize = 16.sp,
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
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    onDismissRequest()
                                    clipText(context, cookie)
                                }
                                .padding(16.dp)
                        )

                        Text(
                            text = stringResource(id = jp.toastkid.lib.R.string.close),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable(onClick = onDismissRequest)
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

    (context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
            .snackShort("It has copied URL to clipboard.${System.lineSeparator()}$copyText")
    }
}
