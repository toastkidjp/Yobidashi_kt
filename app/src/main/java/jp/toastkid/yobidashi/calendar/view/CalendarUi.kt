/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.view

import android.os.Build
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.calendar.DateSelectedActionUseCase
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import java.util.Calendar

@Composable
fun CalendarUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val browserViewModel = viewModel(BrowserViewModel::class.java, context)
    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val datePicker = DatePicker(context)
    val today = Calendar.getInstance()
    var year = today.get(Calendar.YEAR)
    var monthOfYear = today.get(Calendar.MONTH)
    var dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
    datePicker.init(year, monthOfYear, dayOfMonth) { _, y, m, d ->
        year = y
        monthOfYear = m
        dayOfMonth = d
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        && PreferenceApplier(context).useDarkMode()) {
        datePicker.setBackgroundColor(MaterialTheme.colors.onSurface.toArgb())
    }

    Surface(elevation = 4.dp) {
        Column {
            AndroidView(
                factory = { datePicker },
                modifier = Modifier
                    .padding(8.dp)
            )

            InsetDivider()

            SingleLineMenu(R.string.menu_what_happened_today) {
                val url = DateArticleUrlFactory()(
                    context,
                    monthOfYear,
                    dayOfMonth
                )
                if (Urls.isValidUrl(url)) {
                    browserViewModel.open(url.toUri())
                }
            }

            InsetDivider()

            SingleLineMenu(R.string.title_article_viewer) {
                DateSelectedActionUseCase(
                    AppDatabase
                        .find(context)
                        .articleRepository(),
                    contentViewModel
                ).invoke(year, monthOfYear, dayOfMonth)
            }
        }

    }
}

@Composable
private fun SingleLineMenu(
    titleId: Int,
    onClick: () -> Unit
) {
    Text(
        stringResource(id = titleId),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    )
}