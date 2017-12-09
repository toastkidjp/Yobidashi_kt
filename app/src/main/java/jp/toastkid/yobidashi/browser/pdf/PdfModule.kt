package jp.toastkid.yobidashi.browser.pdf

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModulePdfBinding

/**
 * PDF Module.
 *
 * @author toastkidjp
 */
class PdfModule(val context: Context, val parent: ViewGroup) {

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

    init {
        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
}