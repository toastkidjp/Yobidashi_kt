package jp.toastkid.yobidashi.pdf

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.SeekBar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModulePdfBinding
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.PdfTab
import timber.log.Timber

/**
 * PDF Module.
 *
 * @param context
 * @param parent
 *
 * @author toastkidjp
 */
class PdfModule(
        private val context: Context,
        private val parent: ViewGroup
): BaseModule(parent) {

    /**
     * Data binding object.
     */
    private val binding = DataBindingUtil.inflate<ModulePdfBinding>(
            LayoutInflater.from(context),
            R.layout.module_pdf,
            parent,
            false
    )

    /**
     * Adapter.
     */
    private val adapter = Adapter(context)

    /**
     * LayoutManager.
     */
    private val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    /**
     * Use for save tab's thumbnail.
     */
    private val screenshotDir: FilesDir by lazy { TabAdapter.makeNewScreenshotDir(context) }

    init {
        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(binding.pdfImages)

        binding.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = p0?.progress ?: 0
                binding.input.setText((progress + 1).toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
        binding.input.addTextChangedListener(object: TextWatcher{
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
    }

    /**
     * Apply color to views.
     *
     * @param colorPair
     */
    fun applyColor(colorPair: ColorPair) {
        binding.header.setBackgroundColor(colorPair.bgColor())
        binding.seek.progressDrawable.colorFilter =
                PorterDuffColorFilter(colorPair.fontColor(), PorterDuff.Mode.SRC_IN)
        Colors.setEditTextColor(binding.input, colorPair.fontColor())
    }

    /**
     * Load PDF content from [Uri].
     *
     * @param uri
     */
    fun load(uri: Uri) {
        if (parent.childCount == 0) {
            parent.addView(binding.root)
        }
        adapter.load(uri)
        binding.pdfImages.scheduleLayoutAnimation()
        binding.seek.max = adapter.itemCount - 1
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
     *
     * @param tab [PdfTab]
     */
    internal fun assignNewThumbnail(tab: PdfTab): Disposable {
        Timber.e(RuntimeException("thumb"))
        return Completable.fromAction { buildThumbnail() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            binding.pdfImages.drawingCache?.let {
                                val file = screenshotDir.assignNewFile(tab.id() + ".png")
                                Bitmaps.compress(it, file)
                                tab.thumbnailPath = file.absolutePath

                                Completable.fromAction {
                                    binding.root.background = it.toDrawable(binding.root.resources)
                                }
                            }
                        },
                        Timber::e
                )
    }

    /**
     * Build current thumbnail.
     */
    private fun buildThumbnail() {
        binding.pdfImages.invalidate()
        binding.pdfImages.buildDrawingCache()
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
     * Move to first page.
     */
    internal fun pageUp() {
        RecyclerViewScroller.toTop(binding.pdfImages, adapter.itemCount)
    }

    /**
     * Move to last page.
     */
    internal fun pageDown() {
        RecyclerViewScroller.toBottom(binding.pdfImages, adapter.itemCount)
    }

    override fun isVisible() = binding.root.isVisible
}