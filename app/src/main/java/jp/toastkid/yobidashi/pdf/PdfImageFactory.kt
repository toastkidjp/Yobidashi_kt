package jp.toastkid.yobidashi.pdf

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build

/**
 * PDF content image factory. This class can use only Android L and upper L.
 *
 * @author toastkidjp
 */
class PdfImageFactory {

    /**
     * Invoke action.
     *
     * <pre>
     * if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
     *     return
     * }
     * </pre>
     * @param currentPage current PDF page
     * @return non-null bitmap
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun invoke(currentPage: PdfRenderer.Page): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(
                currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        currentPage.close()
        return bitmap
    }
}