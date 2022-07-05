/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.number.factory.GameFileProvider
import jp.toastkid.number.repository.GameRepositoryImplementation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPlaceUi() {
    val fontSize = 32.sp

    val context = LocalContext.current

    val preferenceApplier = PreferenceApplier(LocalContext.current)
    val viewModel = viewModel(NumberPlaceViewModel::class.java)
    LaunchedEffect(key1 = viewModel, block = {
        val file = GameFileProvider().invoke(context.filesDir, preferenceApplier)
        if (file != null) {
            val game = GameRepositoryImplementation().load(file)
            if (game != null) {
                viewModel.setGame(game)
                return@LaunchedEffect
            }
        }
        withContext(Dispatchers.IO) {
            viewModel.initialize(preferenceApplier.getMaskingCount())
            viewModel.saveCurrentGame(context)
        }
    })

    val contentViewModel = (LocalContext.current as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

    val numberStates = mutableListOf<MutableState<String>>()

    Surface(
        elevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                HorizontalDivider(0)

                viewModel.masked().rows().forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        VerticalDivider(0)

                        row.forEachIndexed { columnIndex, cellValue ->
                            if (cellValue == -1) {
                                val open = remember { mutableStateOf(false) }
                                val number = remember { mutableStateOf("_") }
                                numberStates.add(number)

                                val solving = viewModel.pickSolving(rowIndex, columnIndex)
                                if (solving != -1) {
                                    number.value = "$solving"
                                }

                                MaskedCell(
                                    open,
                                    number,
                                    {
                                        number.value = "$it"
                                        open.value = false
                                        viewModel.place(rowIndex, columnIndex, it) { done ->
                                            showMessageSnackbar(context, contentViewModel, done)
                                        }
                                    },
                                    fontSize,
                                    modifier = Modifier
                                        .weight(1f)
                                        .combinedClickable(
                                            onClick = {
                                                open.value = true
                                            },
                                            onLongClick = {
                                                contentViewModel?.snackWithAction(
                                                    "Would you like to use hint?",
                                                    "Use"
                                                ) {
                                                    viewModel.useHint(
                                                        rowIndex,
                                                        columnIndex,
                                                        number
                                                    ) { done ->
                                                        showMessageSnackbar(context, contentViewModel, done)
                                                    }
                                                }
                                            }
                                        )
                                )
                            } else {
                                Text(
                                    cellValue.toString(),
                                    fontSize = fontSize,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            VerticalDivider(columnIndex)
                        }
                    }
                    HorizontalDivider(rowIndex)
                }
            }

            if (viewModel.loading().value) {
                CircularProgressIndicator(
                )
            }
        }
    }

    contentViewModel?.optionMenus(
        OptionMenu(
            titleId = R.string.menu_other_board,
            action = {
                deleteCurrentGame(context)
                contentViewModel.nextRoute("tool/number/place")
            }),
        OptionMenu(
            titleId = R.string.clear_all,
            action = {
                viewModel.initializeSolving()
                numberStates.forEach { it.value = "_" }
            })
    )

    (LocalContext.current as? ViewModelStoreOwner)?.let {
        viewModel(AppBarViewModel::class.java, it)
            .replace {
                AppBarContent(preferenceApplier, fontSize, contentViewModel)
            }
    }

    DisposableEffect(key1 = viewModel, effect = {
        onDispose {
            viewModel.saveCurrentGame(context)
        }
    })
}

private fun deleteCurrentGame(context: Context) {
    val preferenceApplier = PreferenceApplier(context)
    preferenceApplier.clearLastNumberPlaceGamePath()
    val file = GameFileProvider().invoke(context.filesDir, preferenceApplier)
    file?.let {
        GameRepositoryImplementation().delete(file)
    }
}

private fun showMessageSnackbar(
    context: Context,
    contentViewModel: ContentViewModel?,
    done: Boolean
) {
    contentViewModel?.snackWithAction(
        if (done) "Well done!" else "Incorrect...",
        if (done) "Next game" else ""
    ) {
        if (done) {
            deleteCurrentGame(context)
            contentViewModel.nextRoute("tool/number/place")
        }
    }
}

@Composable
private fun VerticalDivider(columnIndex: Int) {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .width(calculateThickness(columnIndex))
    )
}

@Composable
private fun HorizontalDivider(rowIndex: Int) {
    Divider(thickness = calculateThickness(rowIndex))
}

private fun calculateThickness(columnIndex: Int) = if (columnIndex % 3 == 2) 2.dp else 1.dp

@Composable
private fun AppBarContent(
    preferenceApplier: PreferenceApplier,
    fontSize: TextUnit,
    contentViewModel: ContentViewModel?
) {
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        val openMaskingCount = remember { mutableStateOf(false) }
        val maskingCount = remember { mutableStateOf("${preferenceApplier.getMaskingCount()}") }

        Text(
            "Masking count: ",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 8.dp)
        )

        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .clickable {
                    openMaskingCount.value = true
                }
        ) {
            Text(
                maskingCount.value,
                textAlign = TextAlign.Center,
                fontSize = fontSize
            )
            DropdownMenu(
                openMaskingCount.value,
                onDismissRequest = { openMaskingCount.value = false }) {
                (1..64).forEach {
                    DropdownMenuItem(onClick = {
                        maskingCount.value = "$it"
                        openMaskingCount.value = false
                        preferenceApplier.setMaskingCount(it)
                        deleteCurrentGame(context)
                        contentViewModel?.nextRoute("tool/number/place")
                    }) {
                        Text(
                            text = "$it",
                            fontSize = fontSize,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaskedCell(
    openState: MutableState<Boolean>,
    numberState: MutableState<String>,
    onMenuItemClick: (Int) -> Unit,
    fontSize: TextUnit,
    modifier: Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            numberState.value,
            color = Color(0xFFAA99FF),
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
        DropdownMenu(openState.value, onDismissRequest = { openState.value = false }) {
            (1..9).forEach {
                DropdownMenuItem(onClick = {
                    onMenuItemClick(it)
                }) {
                    Text(
                        text = "$it",
                        fontSize = fontSize,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
