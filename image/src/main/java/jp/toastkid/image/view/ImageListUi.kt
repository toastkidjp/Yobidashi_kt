/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.view

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jp.toastkid.image.Image
import jp.toastkid.image.R
import jp.toastkid.image.list.BucketLoader
import jp.toastkid.image.list.ImageFilterUseCase
import jp.toastkid.image.list.ImageLoader
import jp.toastkid.image.list.ImageLoaderUseCase
import jp.toastkid.image.preview.ImagePreviewUi
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageListUi() {
    val context = LocalContext.current

    val contentResolver = context.contentResolver ?: return

    val preferenceApplier = PreferenceApplier(context)

    val preview = remember { mutableStateOf(false) }

    val images = remember { mutableStateListOf<Image>() }

    val backHandlerState = remember { mutableStateOf(false) }

    val imageLoader = ImageLoader(contentResolver)
    val imageLoaderUseCase = remember {
        ImageLoaderUseCase(
            preferenceApplier,
            {
                images.clear()
                images.addAll(it)
            },
            BucketLoader(contentResolver),
            imageLoader,
            backHandlerState,
            { }
        )
    }

    val imageFilterUseCase = remember {
        ImageFilterUseCase(
            preferenceApplier,
            {
                images.clear()
                images.addAll(it)
            },
            imageLoaderUseCase,
            imageLoader,
            { }
        )
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                imageLoaderUseCase()
                return@rememberLauncherForActivityResult
            }

            (context as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it).get(ContentViewModel::class.java)
                    .snackShort(R.string.message_audio_file_is_not_found)
            }
        }

    (context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(PageSearcherViewModel::class.java)
            .also { viewModel ->
                val keyword = viewModel.find.observeAsState().value?.getContentIfNotHandled()
                if (keyword.isNullOrBlank()) {
                    return@also
                }
                imageFilterUseCase(keyword)
            }
    }

    if (preview.value.not()) {
        LaunchedEffect(key1 = "first_launch") {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val index = remember { mutableStateOf(-1) }

    if (preview.value) {
        ImagePreviewUi(images, index.value)
    } else {
        ImageListUi(
            imageLoaderUseCase,
            images
        ) {
            index.value = it
            preview.value = true
            backHandlerState.value = true
        }
    }

    BackHandler(backHandlerState.value) {
        if (index.value != -1) {
            index.value = -1
            preview.value = false
        } else {
            imageLoaderUseCase.back {}
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImageListUi(
    imageLoaderUseCase: ImageLoaderUseCase,
    images: List<Image>,
    showPreview: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val preferenceApplier = PreferenceApplier(LocalContext.current)

    val chunked = images.chunked(2)
    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .background(Color.Transparent)
    ) {
        items(chunked) { itemRow ->
            Row {
                itemRow.forEach { image ->
                    Surface(
                        elevation = 4.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .combinedClickable(
                                    true,
                                    onClick = {
                                        if (image.isBucket) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                imageLoaderUseCase(image.name)
                                            }
                                        } else {
                                            showPreview(images.indexOf(image))
                                        }
                                    },
                                    onLongClick = {
                                        preferenceApplier.addExcludeItem(image.path)
                                        imageLoaderUseCase()
                                    }
                                )
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(image.path)
                                    .crossfade(true)
                                    .placeholder(R.drawable.ic_image)
                                    .build(),
                                contentDescription = image.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.height(152.dp)
                            )
                            Text(
                                text = image.makeDisplayName(),
                                fontSize = 14.sp,
                                maxLines = 2,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
