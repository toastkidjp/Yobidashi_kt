package jp.toastkid.yobidashi.browser.pdf

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
import jp.toastkid.yobidashi.browser.tab.TabAdapter
import jp.toastkid.yobidashi.browser.tab.model.PdfTab
import jp.toastkid.yobidashi.databinding.ModulePdfBinding
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
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
        val context: Context,
        val parent: ViewGroup,
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
        layoutManager.scrollToPosition(0)
        if (parent.childCount == 0) {
            parent.addView(binding.root)
        }
    }

    /**
     * Assign new thumbnail image.
     *
     * @param id Tab's ID
     * @param tab [PdfTab]
     */
    internal fun assignNewThumbnail(id: String, tab: PdfTab): Disposable =
            Completable.fromAction { binding.pdfImages.invalidate() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe({
                    binding.pdfImages.buildDrawingCache()
                    binding.pdfImages.drawingCache?.let {
                        val file = screenshotDir.assignNewFile(id + ".png")
                        Bitmaps.compress(binding.pdfImages.drawingCache, file)
                        tab.thumbnailPath = file.absolutePath
                    }
                }, { Timber.e(it) }
                )

    /**
     * Get safe index.
     */
    private fun getSafeIndex(): Int {
        val index = layoutManager.findFirstVisibleItemPosition()
        return if (index < 0 || index < adapter.itemCount) 0 else index
    }

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