package jp.toastkid.yobidashi.pdf

import android.annotation.TargetApi
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R

/**
 * PDF Viewer's adapter.
 *
 * @param layoutInflater Use for inflating item views
 * @param contentResolver Use for loading PDF content
 * @author toastkidjp
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val contentResolver: ContentResolver?
): RecyclerView.Adapter<ViewHolder>() {

    /**
     * File descriptor.
     */
    private var fileDescriptor: ParcelFileDescriptor? = null

    /**
     * PDF renderer.
     */
    private var pdfRenderer: PdfRenderer? = null

    private val pdfImageFactory = PdfImageFactory()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        pdfRenderer?.let {
            val image: Bitmap = pdfImageFactory(it.openPage(position))
            holder.setImage(image)
            holder.setIndicator(position + 1, itemCount)
        }
    }

    override fun getItemCount(): Int = pdfRenderer?.pageCount ?: 0

    /**
     * Load PDF from [Uri].
     *
     * @param uri
     * @throws SecurityException
     */
    fun load(uri: Uri) {
        fileDescriptor = contentResolver?.openFileDescriptor(uri, "r")
        pdfRenderer?.close()
        pdfRenderer = fileDescriptor?.let { PdfRenderer(it) }
        notifyDataSetChanged()
    }

    /**
     * Dispose disposables.
     */
    fun dispose() {
        pdfRenderer?.close()
        fileDescriptor?.close()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_pdf_content

    }
}