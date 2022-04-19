/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.settings.color.SavedColor
import jp.toastkid.yobidashi.settings.view.ColorPaletteUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun ColorSettingUi() {
    val context = LocalContext.current
    val preferenceApplier = PreferenceApplier(context)

    val repository = DatabaseFinder().invoke(context).savedColorRepository()

    val colorPair = preferenceApplier.colorPair()
    val initialBgColor = colorPair.bgColor()
    val initialFontColor = colorPair.fontColor()

    val coroutineScope = rememberCoroutineScope()

    val contentViewModel = (context as? FragmentActivity)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }

    val currentBackgroundColor =
        remember { mutableStateOf(Color(preferenceApplier.color)) }

    val currentFontColor =
        remember { mutableStateOf(Color(preferenceApplier.fontColor)) }

    MaterialTheme() {
        LazyColumn(
            modifier = Modifier
                .nestedScroll(rememberViewInteropNestedScrollConnection())
                .padding(start = 8.dp, end = 8.dp)
        ) {
            item {
                ColorPaletteUi(
                    currentBackgroundColor,
                    currentFontColor,
                    initialBgColor,
                    initialFontColor,
                    onCommit = {
                        val bgColor = currentBackgroundColor.value
                        val fontColor = currentFontColor.value

                        commitNewColor(
                            preferenceApplier,
                            currentBackgroundColor,
                            currentFontColor,
                            bgColor,
                            fontColor
                        )
                        contentViewModel?.snackShort(R.string.settings_color_done_commit)

                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                val savedColor =
                                    SavedColor.make(
                                        bgColor.toArgb(),
                                        fontColor.toArgb()
                                    )
                                repository.add(savedColor)
                                //adapter?.reload()
                            }
                        }
                    },
                    onReset = {
                        commitNewColor(
                            preferenceApplier,
                            currentBackgroundColor,
                            currentFontColor,
                            Color(initialBgColor),
                            Color(initialFontColor)
                        )

                        //activity?.let { Updater().update(it) }
                        contentViewModel?.snackShort(R.string.settings_color_done_reset)
                    }
                )
            }

            coroutineScope.launch {
                val savedColors = withContext(Dispatchers.IO) { repository.findAll().windowed(3, 3) }
                if (savedColors.isEmpty()) {
                    return@launch
                }

                item {
                    Surface(
                        elevation = 4.dp,
                        modifier = Modifier
                            .height(44.dp)
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(id = R.string.settings_color_saved_title),
                                fontSize = 18.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                items(savedColors) { savedColorRow ->
                    Row {
                        savedColorRow.forEach { savedColor ->
                            Surface(
                                elevation = 4.dp,
                                modifier = Modifier
                                    .clickable {
                                        commitNewColor(
                                            preferenceApplier,
                                            currentBackgroundColor,
                                            currentFontColor,
                                            Color(savedColor.bgColor),
                                            Color(savedColor.fontColor)
                                        )
                                        contentViewModel?.snackShort(R.string.settings_color_done_commit)
                                    }
                                    .weight(1f)
                                    .padding(8.dp)
                            ) {
                                Box(modifier = Modifier
                                    .height(100.dp)
                                    .background(Color(savedColor.bgColor))
                                    .padding(8.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.sample_a),
                                        color = Color(savedColor.fontColor),
                                        modifier = Modifier.align(
                                            Alignment.Center)
                                    )
                                    Icon(painterResource(id = R.drawable.ic_remove_circle), contentDescription = stringResource(
                                        id = R.string.delete
                                    ),
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(40.dp)
                                            .clickable {
                                                coroutineScope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        repository.delete(savedColor)
                                                    }
                                                }
                                            }
                                            .align(Alignment.TopEnd))
                                }
                            }
                        }
                    }

                    /*
                    binding?.clearSavedColor?.setOnClickListener{
    ConfirmDialogFragment.show(
        parentFragmentManager,
        getString(R.string.title_clear_saved_color),
        Html.fromHtml(
            activityContext.getString(R.string.confirm_clear_all_settings),
            Html.FROM_HTML_MODE_COMPACT
        ),
        "clear_color"
    )
}
                     */
                }
            }
        }
    }
}

/*
parentFragmentManager.setFragmentResultListener(
        "clear_color",
        viewLifecycleOwner,
        { _, _ ->
            adapter?.clear()
        }
    )
    parentFragmentManager.setFragmentResultListener(
        "add_recommended_colors",
        viewLifecycleOwner,
        { _, _ -> DefaultColorInsertion().insert(view.context) }
    )
*/

/**
 * Commit new color.
 *
 * @param bgColor   Background color int
 * @param fontColor Font color int
 */
private fun commitNewColor(
    preferenceApplier: PreferenceApplier,
    currentBackgroundColor: MutableState<Color>,
    currentFontColor: MutableState<Color>,
    bgColor: Color,
    fontColor: Color
) {
    preferenceApplier.color = bgColor.toArgb()
    preferenceApplier.fontColor = fontColor.toArgb()

    currentBackgroundColor?.value = bgColor
    currentFontColor?.value = fontColor

    //TODO activity?.let { Updater().update(it) }
}

/*override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.color_settings_toolbar_menu_add_recommend -> {
            ConfirmDialogFragment.show(
                parentFragmentManager,
                getString(R.string.title_add_recommended_colors),
                getString(R.string.message_add_recommended_colors),
                "add_recommended_colors"
            )
            true
        }
        R.id.color_settings_toolbar_menu_add_random -> {
            RandomColorInsertion(repository)() {
                adapter?.refresh()
            }
            snackShort(R.string.done_addition)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}*/
    //override fun titleId(): Int = R.string.title_settings_color