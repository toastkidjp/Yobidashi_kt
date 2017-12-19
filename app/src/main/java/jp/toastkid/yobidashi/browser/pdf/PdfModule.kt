package jp.toastkid.yobidashi.browser.pdf

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModulePdfBinding
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller

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

    init {
        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
    }

    /**
     * Load PDF content from [Uri].
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
     * Assign new thumbnail image.
     *
     * @param id
     */
    fun assignNewThumbnail(id: String): String = adapter.assignNewThumbnail(id, getSafeIndex())

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