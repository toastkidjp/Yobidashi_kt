/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.favorite

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.search.SearchCategory
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.view.SearchCategorySpinner
import jp.toastkid.yobidashi.search.view.SearchItemContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.MessageFormat

@Composable
fun FavoriteSearchListUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val preferenceApplier = PreferenceApplier(activityContext)

    val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

    val favoriteSearchItems = remember { mutableStateListOf<FavoriteSearch>() }

    val database = DatabaseFinder().invoke(activityContext)
    val repository = database.favoriteSearchRepository()

    ViewModelProvider(activityContext).get(AppBarViewModel::class.java)
        .replace {
            val editorOpen = remember { mutableStateOf(false) }

            Button(
                onClick = { editorOpen.value = true },
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = colorResource(id = R.color.soft_background),
                    contentColor = colorResource(id = R.color.colorPrimary),
                    disabledContentColor = Color.LightGray
                )
            ) {
                Text(text = stringResource(id = R.string.add))

                val spinnerOpen = remember { mutableStateOf(false) }
                val categoryName = remember {
                    mutableStateOf(
                        PreferenceApplier(activityContext).getDefaultSearchEngine()
                            ?: SearchCategory.getDefaultCategoryName()
                    )
                }
                val input = remember { mutableStateOf("") }

                Popup {
                    Column(
                        modifier = Modifier
                            .background(colorResource(id = R.color.soft_background))
                            .padding(dimensionResource(id = R.dimen.settings_item_left_margin))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(id = R.string.category),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            SearchCategorySpinner(spinnerOpen, categoryName)
                        }
                        // TODO imeActionLabel="@string/title_add"
                        TextField(
                            value = input.value,
                            onValueChange = { input.value = it },
                            label = { stringResource(id = R.string.word) }
                        )

                        Button(
                            onClick = {
                                if (input.value.isEmpty()) {
                                    contentViewModel.snackShort(
                                        R.string.favorite_search_addition_dialog_empty_message
                                    )
                                    return@Button
                                }

                                FavoriteSearchInsertion(
                                    activityContext,
                                    categoryName.value,
                                    input.value
                                ).invoke()

                                reload(repository, favoriteSearchItems)

                                val message = MessageFormat.format(
                                    activityContext.getString(R.string.favorite_search_addition_successful_format),
                                    input.value
                                )
                                contentViewModel.snackShort(message)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color(preferenceApplier.color),
                                contentColor = Color(preferenceApplier.fontColor),
                                disabledContentColor = Color.LightGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.title_add)
                            )
                        }
                    }
                }
            }
        }

    FavoriteSearchItemList(repository, favoriteSearchItems)

    val clearConfirmDialogState = remember { mutableStateOf(false) }
    contentViewModel.optionMenus(
        OptionMenu(titleId = R.string.title_delete_all, action = {
            clearConfirmDialogState.value = true
        })
    )

    DestructiveChangeConfirmDialog(
        clearConfirmDialogState,
        R.string.title_clear_favorite_search
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.deleteAll()
            }
            favoriteSearchItems.clear()

            contentViewModel.snackShort(R.string.done_clear)
        }
    }
}

@Composable
private fun FavoriteSearchItemList(
    repository: FavoriteSearchRepository,
    favoriteSearchItems: SnapshotStateList<FavoriteSearch>
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    MaterialTheme() {
        Surface(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .background(colorResource(id = R.color.setting_background))
                    .nestedScroll(rememberViewInteropNestedScrollConnection())
            ) {
                reload(repository, favoriteSearchItems)

                items(favoriteSearchItems) { favoriteSearch ->
                    SearchItemContent(
                        favoriteSearch.query,
                        favoriteSearch.category,
                        {
                            SearchAction(
                                context,
                                favoriteSearch.category ?: "",
                                favoriteSearch.query ?: "",
                                onBackground = it
                            ).invoke()
                            SearchAction(
                                context,
                                favoriteSearch.category ?: "",
                                favoriteSearch.query ?: ""
                            ).invoke()
                        },
                        {
                            repository.delete(favoriteSearch)
                            favoriteSearchItems.remove(favoriteSearch)
                        }
                    )
                }
            }
        }
    }
}

private fun reload(
    repository: FavoriteSearchRepository,
    favoriteSearchItems: SnapshotStateList<FavoriteSearch>
) {
    CoroutineScope(Dispatchers.Main).launch {
        val loaded = withContext(Dispatchers.IO) {
            repository.findAll()
        }
        favoriteSearchItems.clear()
        favoriteSearchItems.addAll(loaded)
    }
}
