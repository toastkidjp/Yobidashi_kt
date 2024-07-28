/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.about.view

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.licence.model.License
import jp.toastkid.ui.parts.InsetDivider

@Composable
internal fun LicensesUi(licenses: List<License>) {
    val viewModel = (LocalView.current.context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }
    LazyColumn {
        items(licenses) { license ->
            Column {
                Text(license.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(license.copyright)
                Text(
                    license.url,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable { viewModel?.open(license.url.toUri()) }
                )
                val lineCount = remember { mutableStateOf(3) }
                val maxLines = animateIntAsState(targetValue = lineCount.value)
                Text(
                    license.text,
                    maxLines = maxLines.value,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(8.dp)
                        .padding(bottom = 8.dp)
                        .clickable {
                            lineCount.value = if (lineCount.value == 3) 1000 else 3
                        })
                InsetDivider()
            }
        }
    }
}