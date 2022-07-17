/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.favorite

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
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

    contentViewModel.replaceAppBarContent {
            val spinnerOpen = remember { mutableStateOf(false) }

            val categoryName = remember {
                mutableStateOf(
                    PreferenceApplier(activityContext).getDefaultSearchEngine()
                        ?: SearchCategory.getDefaultCategoryName()
                )
            }

            val input = remember { mutableStateOf("") }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(dimensionResource(id = R.dimen.toolbar_height))
            ) {
                SearchCategorySpinner(spinnerOpen, categoryName)

                TextField(
                    value = input.value,
                    onValueChange = { input.value = it },
                    label = { stringResource(id = R.string.word) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(preferenceApplier.fontColor),
                        textAlign = TextAlign.Start,
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "clear text",
                            tint = Color(preferenceApplier.fontColor),
                            modifier = Modifier
                                .clickable {
                                    input.value = ""
                                }
                        )
                    },
                    keyboardActions = KeyboardActions {
                        addItem(input, contentViewModel, activityContext, categoryName, repository, favoriteSearchItems)
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = true,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        addItem(
                            input,
                            contentViewModel,
                            activityContext,
                            categoryName,
                            repository,
                            favoriteSearchItems
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.surface,
                        contentColor = MaterialTheme.colors.onSurface,
                        disabledContentColor = Color.LightGray
                    )
                ) {
                    Text(text = stringResource(id = R.string.add))
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

private fun addItem(
    input: MutableState<String>,
    contentViewModel: ContentViewModel,
    activityContext: ComponentActivity,
    categoryName: MutableState<String>,
    repository: FavoriteSearchRepository,
    favoriteSearchItems: SnapshotStateList<FavoriteSearch>
) {
    val newWord = input.value.trim()
    if (newWord.isEmpty()) {
        contentViewModel.snackShort(
            R.string.favorite_search_addition_dialog_empty_message
        )
        return
    }

    FavoriteSearchInsertion(
        activityContext,
        categoryName.value,
        newWord
    ).invoke()

    reload(repository, favoriteSearchItems)

    val message = MessageFormat.format(
        activityContext.getString(R.string.favorite_search_addition_successful_format),
        newWord
    )
    contentViewModel.snackShort(message)
}

@Composable
private fun FavoriteSearchItemList(
    repository: FavoriteSearchRepository,
    favoriteSearchItems: SnapshotStateList<FavoriteSearch>
) {
    val context = LocalContext.current

    rememberCoroutineScope()

    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        reload(repository, favoriteSearchItems)

        items(favoriteSearchItems, { it.id }) { favoriteSearch ->
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
