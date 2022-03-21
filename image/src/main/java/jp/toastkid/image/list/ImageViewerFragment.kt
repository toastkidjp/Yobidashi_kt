/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.list

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.image.Image
import jp.toastkid.image.R
import jp.toastkid.image.preview.ImagePreviewDialogFragment
import jp.toastkid.image.setting.ExcludingSettingFragment
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ImageViewerFragment : Fragment(), CommonFragmentAction, ContentScrollable {

    private lateinit var bucketLoader: BucketLoader

    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var imageLoaderUseCase: ImageLoaderUseCase

    private lateinit var imageFilterUseCase: ImageFilterUseCase

    private var adapter: Adapter? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                imageLoaderUseCase()
                return@registerForActivityResult
            }

            activity?.let {
                ViewModelProvider(it).get(ContentViewModel::class.java)
                    .snackShort(R.string.message_audio_file_is_not_found)
            }
            activity?.supportFragmentManager?.popBackStack()
        }

    private var scrollState: LazyListState? = null

    private val disposables: Job by lazy { Job() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val context = context ?: return null
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        val viewModel = ViewModelProvider(this).get(ImageViewerFragmentViewModel::class.java)
        viewModel.images.observe(viewLifecycleOwner, { images ->
            composeView.setContent {
                ImageListUi(images)
            }
        })

        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        val contentResolver = context.contentResolver ?: return
        bucketLoader = BucketLoader(contentResolver)
        imageLoader = ImageLoader(contentResolver)

        preferenceApplier = PreferenceApplier(context)

        val viewModelProvider = ViewModelProvider(this)
        val viewModel =
            viewModelProvider.get(ImageViewerFragmentViewModel::class.java)

        adapter = Adapter(parentFragmentManager, viewModel)

        observePageSearcherViewModel()

        imageLoaderUseCase = ImageLoaderUseCase(
            preferenceApplier,
            { viewModel.submitImages(it) },
            BucketLoader(contentResolver),
            imageLoader,
            this::refreshContent
        )

        imageFilterUseCase = ImageFilterUseCase(
            preferenceApplier,
            adapter,
            imageLoaderUseCase,
            imageLoader,
            this::refreshContent
        )

        parentFragmentManager.setFragmentResultListener(
            "excluding",
            viewLifecycleOwner,
            { _, _ -> imageLoaderUseCase() }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ImageListUi(images: List<Image>) {
        val listState = rememberLazyListState()
        this.scrollState = listState

        MaterialTheme {
            Surface(Modifier.background(colorResource(id = R.color.soft_background))) {
                LazyVerticalGrid(
                    state = listState,
                    cells = GridCells.Fixed(2),
                    modifier = Modifier
                        .nestedScroll(rememberViewInteropNestedScrollConnection())
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(images) { index, image ->
                        Surface(
                            elevation = 4.dp,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .combinedClickable(
                                        true,
                                        onClick = {
                                            if (image.isBucket) {
                                                CoroutineScope(Dispatchers.IO).launch(disposables) {
                                                    imageLoaderUseCase(image.name)
                                                }
                                            } else {
                                                val fragmentManager = parentFragmentManager
                                                ImagePreviewDialogFragment
                                                    .withImages(images, index)
                                                    .show(
                                                        fragmentManager,
                                                        ImagePreviewDialogFragment::class.java.simpleName
                                                    )
                                            }
                                        },
                                        onLongClick = {
                                            preferenceApplier.addExcludeItem(image.path)
                                            imageLoaderUseCase()
                                        }
                                    )
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = image.path,
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

    private fun observePageSearcherViewModel() {
        val activity = activity ?: return
        ViewModelProvider(activity).get(PageSearcherViewModel::class.java)
            .also { viewModel ->
                viewModel.find.observe(viewLifecycleOwner, Observer {
                    imageFilterUseCase(it)
                })
            }
    }

    override fun pressBack(): Boolean {
        imageLoaderUseCase.back {
            activity?.supportFragmentManager?.popBackStack()
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        attemptLoad()
    }

    private fun attemptLoad() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun refreshContent() {
        toTop()
    }

    override fun toTop() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(0, 0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(scrollState?.layoutInfo?.totalItemsCount ?: 0, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.image_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.excluding_items_setting -> {
                val fragment = ExcludingSettingFragment()
                fragment.show(parentFragmentManager, "setting")
            }
            R.id.sort_by_date -> {
                preferenceApplier.setImageViewerSort(Sort.DATE.name)
                imageLoaderUseCase()
            }
            R.id.sort_by_name -> {
                preferenceApplier.setImageViewerSort(Sort.NAME.name)
                imageLoaderUseCase()
            }
            R.id.sort_by_count -> {
                preferenceApplier.setImageViewerSort(Sort.ITEM_COUNT.name)
                imageLoaderUseCase()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        disposables.cancel()
        parentFragmentManager.clearFragmentResultListener("excluding")
        requestPermissionLauncher.unregister()
        super.onDetach()
    }

}