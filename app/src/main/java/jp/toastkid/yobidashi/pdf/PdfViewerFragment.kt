/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.pdf

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentPdfViewerBinding
import jp.toastkid.yobidashi.databinding.ModulePdfHeaderBinding
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.HeaderViewModel

/**
 * @author toastkidjp
 */
class PdfViewerFragment : Fragment() {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentPdfViewerBinding

    private lateinit var headerBinding: ModulePdfHeaderBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: Adapter

    /**
     * LayoutManager.
     */
    private lateinit var layoutManager: LinearLayoutManager

    private val thumbnailGenerator = ThumbnailGenerator()

    private val disposables = CompositeDisposable()

    private var headerViewModel: HeaderViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentPdfViewerBinding>(
                inflater,
                R.layout.fragment_pdf_viewer,
                container,
                false
        )
        headerBinding = DataBindingUtil.inflate<ModulePdfHeaderBinding>(
                inflater,
                R.layout.module_pdf_header,
                container,
                false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = Adapter(LayoutInflater.from(context), context?.contentResolver)
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(binding.pdfImages)

        headerBinding.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = p0?.progress ?: 0
                headerBinding.input.setText((progress + 1).toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
        headerBinding.input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(inputText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputText?.let {
                    val newIndex = try {
                        Integer.parseInt(it.toString()) - 1
                    } catch (e: NumberFormatException) {
                        -1
                    }

                    if (newIndex == -1) {
                        return@let
                    }

                    scrollTo(newIndex)
                }
            }

        })

        (context as? FragmentActivity)?.let {
            headerViewModel = ViewModelProviders.of(it).get(HeaderViewModel::class.java)
        }

        arguments?.getParcelable<Uri>("uri")?.also { load(it) }
    }

    /**
     * Load PDF content from [Uri].
     *
     * @param uri
     */
    private fun load(uri: Uri) {
        adapter.load(uri)
        binding.pdfImages.scheduleLayoutAnimation()
        headerBinding.seek.max = adapter.itemCount - 1
    }

    /**
     * Scroll to specified position.
     *
     * @param position
     */
    fun scrollTo(position: Int) {
        layoutManager.scrollToPosition(getSafeIndex(position))
    }

    /**
     * Assign new thumbnail image.
     */
    fun makeThumbnail(): Bitmap? {
        return thumbnailGenerator(binding.pdfImages)
    }

    /**
     * Get safe index.
     *
     * @param index
     */
    private fun getSafeIndex(index: Int): Int =
            if (index < 0 || adapter.itemCount < index) 0 else index

    /**
     * Return current item position.
     *
     * @return current item position.
     */
    fun currentItemPosition(): Int = layoutManager.findFirstVisibleItemPosition()

    /**
     * Animate root view.
     *
     * @param animation
     */
    fun animate(animation: Animation) {
        binding.root.startAnimation(animation)
    }

    /**
     * TODO Enable.
     * Move to first page.
     */
    fun pageUp() {
        RecyclerViewScroller.toTop(binding.pdfImages, adapter.itemCount)
    }

    /**
     * TODO Enable.
     * Move to last page.
     */
    fun pageDown() {
        RecyclerViewScroller.toBottom(binding.pdfImages, adapter.itemCount)
    }

    override fun onResume() {
        super.onResume()
        headerViewModel?.replace(headerBinding.root)
        applyColor(PreferenceApplier(requireContext()).colorPair())
    }

    /**
     * Apply color to views.
     *
     * @param colorPair
     */
    private fun applyColor(colorPair: ColorPair) {
        headerBinding.header.setBackgroundColor(colorPair.bgColor())
        headerBinding.seek.progressDrawable.colorFilter =
                PorterDuffColorFilter(colorPair.fontColor(), PorterDuff.Mode.SRC_IN)
        EditTextColorSetter().invoke(headerBinding.input, colorPair.fontColor())
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }
}