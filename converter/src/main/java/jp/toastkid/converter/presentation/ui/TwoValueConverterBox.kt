/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import jp.toastkid.converter.domain.model.TwoStringConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoValueConverterBox(unixTimeConverterService: TwoStringConverter) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shadowElevation = 4.dp
    ) {
        val firstInput = remember { mutableStateOf(TextFieldValue(unixTimeConverterService.defaultFirstInputValue())) }
        val secondInput = remember { mutableStateOf(TextFieldValue(unixTimeConverterService.defaultSecondInputValue())) }
        val result = remember { mutableStateOf("") }
        Row {
            Column {
                Text(unixTimeConverterService.title(), modifier = Modifier.padding(8.dp))
                TextField(
                    firstInput.value,
                    maxLines = 1,
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    label = { Text(unixTimeConverterService.firstInputLabel()) },
                    onValueChange = {
                        firstInput.value = TextFieldValue(it.text, it.selection, it.composition)

                        unixTimeConverterService.firstInputAction(firstInput.value.text)
                            ?.let { newValue ->
                                secondInput.value = TextFieldValue(newValue)
                            }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                TextField(
                    secondInput.value,
                    maxLines = 1,
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    label = { Text(unixTimeConverterService.secondInputLabel()) },
                    onValueChange = {
                        secondInput.value = TextFieldValue(it.text, it.selection, it.composition)

                        unixTimeConverterService.secondInputAction(secondInput.value.text)?.let {
                            firstInput.value = TextFieldValue(it)
                        }
                    }
                )
            }
        }
    }
}