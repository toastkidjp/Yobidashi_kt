/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.list

import android.Manifest
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.permission.RuntimePermissions
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RecyclerViewScroller
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentImageViewerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.media.image.setting.ExcludingSettingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ImageViewerFragment : Fragment(), CommonFragmentAction, ContentScrollable {

    private lateinit var binding: FragmentImageViewerBinding

    private lateinit var bucketLoader: BucketLoader

    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var imageLoaderUseCase: ImageLoaderUseCase

    private lateinit var imageFilterUseCase: ImageFilterUseCase

    private var adapter: Adapter? = null

    private val disposables: Job by lazy { Job() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        setHasOptionsMenu(true)
        return binding.root
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

        val viewLifecycleOwner = viewLifecycleOwner
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            CoroutineScope(Dispatchers.IO).launch(disposables) { imageLoaderUseCase(it) }
        })

        viewModel.onLongClick.observe(viewLifecycleOwner, Observer {
            preferenceApplier.addExcludeItem(it)
            imageLoaderUseCase()
        })

        viewModel.refresh.observe(viewLifecycleOwner, Observer {
            attemptLoad()
        })

        observePageSearcherViewModel()

        binding.images.adapter = adapter
        binding.images.layoutManager =
                GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)

        imageLoaderUseCase = ImageLoaderUseCase(
                preferenceApplier,
                adapter,
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
    }

    private fun observePageSearcherViewModel() {
        val activity = activity ?: return
        ViewModelProvider(activity).get(PageSearcherViewModel::class.java)
                .also { viewModel ->
                    viewModel.find.observe(activity, Observer {
                        val text = it?.getContentIfNotHandled() ?: return@Observer
                        imageFilterUseCase(text)
                    })
                }
    }

    fun reset() {
        MediaControllerCompat.getMediaController(requireActivity()).transportControls.stop()
    }

    override fun pressBack(): Boolean {
        if (adapter?.isBucketMode() == true) {
            activity?.supportFragmentManager?.popBackStack()
        } else {
            imageLoaderUseCase.clearCurrentBucket()
            imageLoaderUseCase()
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        attemptLoad()
    }

    private fun attemptLoad() {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            RuntimePermissions(requireActivity())
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect {
                        if (it.granted) {
                            imageLoaderUseCase()
                            return@collect
                        }

                        Toaster.snackShort(
                                binding.root,
                                R.string.message_audio_file_is_not_found,
                                PreferenceApplier(binding.root.context).colorPair()
                        )
                        activity?.supportFragmentManager?.popBackStack()
                    }
        }
    }

    private fun refreshContent() {
        activity?.runOnUiThread {
            adapter?.notifyDataSetChanged()
            RecyclerViewScroller.toTop(binding.images, adapter?.itemCount ?: 0)
        }
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.images, adapter?.itemCount ?: 0)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.images, adapter?.itemCount ?: 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.image_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.excluding_items_setting -> {
                val fragment = ExcludingSettingFragment()
                fragment.setTargetFragment(this, 1)
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
        super.onDetach()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_image_viewer
    }
}