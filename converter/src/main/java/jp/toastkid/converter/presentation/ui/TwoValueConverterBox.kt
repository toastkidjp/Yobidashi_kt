/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import jp.toastkid.converter.domain.model.TwoStringConverter
import jp.toastkid.lib.clip.Clipboard
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun TwoValueConverterBox(unixTimeConverterService: TwoStringConverter) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shadowElevation = 4.dp
    ) {
        val firstInput = remember { TextFieldState(unixTimeConverterService.defaultFirstInputValue()) }
        val secondInput = remember { TextFieldState(unixTimeConverterService.defaultSecondInputValue()) }

        Row {
            Column {
                TextField(
                    state = firstInput,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                    label = { Text(unixTimeConverterService.firstInputLabel()) },
                    trailingIcon = {
                        Icon(
                            painterResource(jp.toastkid.lib.R.drawable.ic_clip),
                            contentDescription = stringResource(jp.toastkid.lib.R.string.clip),
                            Modifier.clickable {
                                Clipboard.clip(context, firstInput.text.toString())
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                LaunchedEffect(firstInput) {
                    snapshotFlow { firstInput.text to (firstInput.composition != null) }
                        .distinctUntilChanged()
                        .collect {
                            if (it.second) {
                                return@collect
                            }

                            unixTimeConverterService.firstInputAction(it.first.toString())
                                ?.let { newValue ->
                                    secondInput.setTextAndPlaceCursorAtEnd(newValue)
                                }
                        }
                }

                TextField(
                    state = secondInput,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                    label = { Text(unixTimeConverterService.secondInputLabel()) },
                    trailingIcon = {
                        Icon(
                            painterResource(jp.toastkid.lib.R.drawable.ic_clip),
                            contentDescription = stringResource(jp.toastkid.lib.R.string.clip),
                            Modifier.clickable {
                                Clipboard.clip(context, secondInput.text.toString())
                            }
                        )
                    }
                )

                LaunchedEffect(secondInput) {
                    snapshotFlow { secondInput.text to (secondInput.composition != null) }
                        .distinctUntilChanged()
                        .collect {
                            if (it.second) {
                                return@collect
                            }

                            unixTimeConverterService.secondInputAction(it.first.toString())
                                ?.let { newValue ->
                                    firstInput.setTextAndPlaceCursorAtEnd(newValue)
                                }
                        }
                }
            }
        }

        LaunchedEffect(unixTimeConverterService) {
            firstInput.setTextAndPlaceCursorAtEnd(unixTimeConverterService.defaultFirstInputValue())
            secondInput.setTextAndPlaceCursorAtEnd(unixTimeConverterService.defaultSecondInputValue())
        }
    }
}