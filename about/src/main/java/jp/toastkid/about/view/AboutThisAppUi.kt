/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.about.view

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.about.R
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.GooglePlayIntentFactory
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.licence.usecase.LoadLicenseUseCase

@Composable
fun AboutThisAppUi(versionName: String) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val scrollState = rememberScrollState()

    val openLicense = remember { mutableStateOf(false) }

    Surface(shadowElevation = 4.dp) {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.message_about_this_app),
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(onClick = {
                        val packageName =
                            context.applicationContext?.packageName ?: return@clickable
                        context.startActivity(GooglePlayIntentFactory()(packageName))
                    })
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_store_black),
                    contentDescription = stringResource(R.string.title_go_google_play)
                )
                Text(
                    text = stringResource(R.string.title_go_google_play),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(onClick = {
                        val browserViewModel =
                            ViewModelProvider(context).get(BrowserViewModel::class.java)

                        browserViewModel.open(
                            context
                                .getString(R.string.link_privacy_policy)
                                .toUri()
                        )
                    })
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_privacy),
                    contentDescription = stringResource(R.string.privacy_policy)
                )
                Text(
                    text = stringResource(R.string.privacy_policy),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(onClick = {
                        openLicense.value = true
                    })
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_license_black),
                    contentDescription = stringResource(R.string.title_licenses)
                )
                Text(
                    text = stringResource(R.string.title_licenses),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_app_version) + versionName,
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW)
                                .also { it.data = Uri.parse("market://search?q=pub:toastkidjp") }
                        )
                    })
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.copyright),
                    fontSize = 16.sp
                )
            }
        }
    }

    if (openLicense.value) {
        val licenses = remember { LoadLicenseUseCase().invoke(context.assets) }
        Dialog(
            onDismissRequest = { openLicense.value = false }
        ) {
            Surface(shadowElevation = 4.dp) {
                LicensesUi(licenses)
            }
        }
    }

    val contentViewModel = ViewModelProvider(context).get(ContentViewModel::class.java)
    ScrollerUseCase(contentViewModel, scrollState).invoke(LocalLifecycleOwner.current)
}

@Composable
private fun InsetDivider() {
    Divider(
        color = colorResource(id = R.color.gray_500_dd),
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 16.dp, end = 16.dp)
    )
}
