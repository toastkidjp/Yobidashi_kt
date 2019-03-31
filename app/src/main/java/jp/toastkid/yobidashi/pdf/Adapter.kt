package jp.toastkid.yobidashi.pdf

import android.annotation.TargetApi
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.ActivityOptionsFactory
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.ImagePreviewActivity
import java.io.File

/**
 * PDF Viewer's adapter.
 *
 * @param context [Context]
 * @author toastkidjp
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Adapter(val context: Context): RecyclerView.Adapter<ViewHolder>() {

    /**
     * Layout inflater.
     */
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * File descriptor.
     */
    private var fileDescriptor: ParcelFileDescriptor? = null

    /**
     * PDF renderer.
     */
    private var pdfRenderer: PdfRenderer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.item_pdf_content, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        pdfRenderer?.let {
            val image: Bitmap = PdfImageFactory.invoke(it.openPage(position))
            holder.setImage(image)
            holder.setIndicator(position + 1, itemCount)
            holder.setOnLongTap(
                    View.OnLongClickListener { view ->
                        val cachedBitmapFile: File = ImageCache.saveBitmap(context, image)
                        context.startActivity(
                                ImagePreviewActivity.makeIntent(context, cachedBitmapFile),
                                ActivityOptionsFactory.makeScaleUpBundle(view).toBundle()
                        )
                        true
                    }
            )
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
        val contentResolver = context.contentResolver
        fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        pdfRenderer?.close()
        pdfRenderer = PdfRenderer(fileDescriptor)
        notifyDataSetChanged()
    }

    /**
     * Dispose disposables.
     */
    fun dispose() {
        pdfRenderer?.close()
        fileDescriptor?.close()
    }
}