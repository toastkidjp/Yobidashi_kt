package jp.toastkid.yobidashi.pdf

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModulePdfBinding
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.facade.BaseModule
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
    }

    /**
     * Load PDF content from [Uri].
     *
     * TODO: keeping position.
     *
     * @param uri
     */
    fun load(uri: Uri) {
        adapter.load(uri)
        if (parent.childCount == 0) {
            parent.addView(binding.root)
        }
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
            if (index < 0 || index < adapter.itemCount) 0 else index

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

    internal fun pageUp() {
        RecyclerViewScroller.toTop(binding.pdfImages, adapter.itemCount)
    }

    internal fun pageDown() {
        RecyclerViewScroller.toBottom(binding.pdfImages, adapter.itemCount)
    }

}