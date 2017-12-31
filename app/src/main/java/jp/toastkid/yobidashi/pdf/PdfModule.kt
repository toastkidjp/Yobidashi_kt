package jp.toastkid.yobidashi.pdf

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.SeekBar
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
        private val parent: ViewGroup,
        private val toolbarCallback: (Boolean) -> Unit
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
    private val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    /**
     * Use for save tab's thumbnail.
     */
    private val screenshotDir: FilesDir by lazy { TabAdapter.makeNewScreenshotDir(context) }

    init {
        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
        binding.pdfImages.setHasFixedSize(true)

        binding.seek.max = adapter.itemCount
        binding.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.input.setText(p0?.progress?.toString() ?: "0")
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
        binding.input.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(inputText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputText?.let {
                    scrollTo(
                            try {
                                Integer.parseInt(it.toString())
                            } catch (e: NumberFormatException) {
                                0
                            }
                    )
                }
            }

        })
        binding.close.setOnClickListener { binding.seekCard.visibility = View.GONE }
    }

    /**
     * Apply color to views.
     *
     * @param colorPair
     */
    fun applyColor(colorPair: ColorPair) {
        binding.seekCard.setBackgroundColor(colorPair.bgColor())
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
        binding.seek.max = adapter.itemCount
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
     * @param id Tab's ID
     * @param tab [PdfTab]
     */
    internal fun assignNewThumbnail(tab: PdfTab): Disposable =
            Completable.fromAction { buildThumbnail() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            binding.pdfImages.drawingCache?.let {
                                val file = screenshotDir.assignNewFile(tab.id() + ".png")
                                Bitmaps.compress(binding.pdfImages.drawingCache, file)
                                tab.thumbnailPath = file.absolutePath
                            }
                        },
                        Timber::e
                )

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

    override fun show() {
        super.show()
        toolbarCallback(true)
    }

    override fun hide() {
        super.hide()
        toolbarCallback(false)
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

}