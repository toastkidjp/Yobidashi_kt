/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.favorite

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.R
import jp.toastkid.search.SearchAction
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.view.SearchCategorySpinner
import jp.toastkid.search.view.SearchItemContent
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.MessageFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteSearchListUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)

    val favoriteSearchItems = remember { mutableStateListOf<FavoriteSearch>() }

    val repository = RepositoryFactory().favoriteSearchRepository(activityContext)

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
                modifier = Modifier.height(56.dp)
            ) {
                SearchCategorySpinner(spinnerOpen, categoryName)

                TextField(
                    value = input.value,
                    onValueChange = { input.value = it },
                    label = { stringResource(id = R.string.word) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Start,
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "clear text",
                            tint = MaterialTheme.colorScheme.onPrimary,
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
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteSearchItemList(
    repository: FavoriteSearchRepository,
    favoriteSearchItems: SnapshotStateList<FavoriteSearch>
) {
    val context = LocalContext.current

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
                },
                modifier = Modifier.animateItemPlacement()
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
