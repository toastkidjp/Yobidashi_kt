/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.Manifest
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.WorkerThread
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentImageViewerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import timber.log.Timber

/**
 * @author toastkidjp
 */
class ImageViewerFragment : Fragment(), CommonFragmentAction {

    private lateinit var binding: FragmentImageViewerBinding

    private lateinit var bucketLoader: BucketLoader

    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceApplier: PreferenceApplier

    private var adapter: Adapter? = null

    private val disposables = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        preferenceApplier = PreferenceApplier(context)

        adapter = Adapter(
                fragmentManager,
                {
                    Completable.fromAction { loadImages(it) }
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                            .addTo(disposables)
                },
                {
                    preferenceApplier.addExcludeItem(it)
                    loadImages()
                }
        )

        val contentResolver = context.contentResolver ?: return
        bucketLoader = BucketLoader(contentResolver)
        imageLoader = ImageLoader(contentResolver)

        binding.images.adapter = adapter
        binding.images.layoutManager =
                GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
    }

    fun reset() {
        MediaControllerCompat.getMediaController(requireActivity()).transportControls.stop()
    }

    override fun pressBack(): Boolean {
        if (adapter?.isBucketMode() == true) {
            activity?.supportFragmentManager?.popBackStack()
        } else {
            loadImages()
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        RxPermissions(requireActivity())
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it) {
                                loadImages()
                                return@subscribe
                            }

                            Toaster.snackShort(
                                    binding.root,
                                    R.string.message_audio_file_is_not_found,
                                    PreferenceApplier(binding.root.context).colorPair()
                            )
                            activity?.supportFragmentManager?.popBackStack()
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    @WorkerThread
    private fun loadImages(bucket: String? = null) {
        adapter?.clear()

        val excludedItemFilter = ExcludingItemFilter(preferenceApplier.excludedItems())

        val parentExtractor = ParentExtractor()

        if (bucket.isNullOrBlank()) {
            bucketLoader().filter { excludedItemFilter(parentExtractor(it.path)) }
        } else {
            imageLoader(bucket).filter { excludedItemFilter(it.path) }
        }
                .forEach { adapter?.add(it) }
        refreshContent()
    }

    private fun refreshContent() {
        activity?.runOnUiThread {
            adapter?.notifyDataSetChanged()
            RecyclerViewScroller.toTop(binding.images, adapter?.itemCount ?: 0)
        }
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_image_viewer
    }
}