package jp.toastkid.jitte.browser.tab

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.Urls
import jp.toastkid.jitte.libs.network.DownloadAction
import jp.toastkid.jitte.libs.network.HttpClientFactory
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Method object of downloading image file.
 */
class ImageDownloadAction(
        val view: View,
        val hitResult: WebView.HitTestResult
) {

    /**
     * Use for setting image file.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    /**
     * Invoke methods.
     */
    fun invoke() {
        val context: Context = view.context

        val url = hitResult.extra
        if (Urls.isInvalidUrl(url)) {
            Toaster.snackShort(
                    view,
                    context.getString(R.string.message_cannot_downloading_image),
                    PreferenceApplier(context).colorPair()
            )
            return
        }

        val imageView = ImageView(context)
        downloadPreview(url, imageView)

        showConfirmDialog(context, imageView, url)
    }

    /**
     * Download preview image.
     */
    private fun downloadPreview(url: String, imageView: ImageView) {
        val client = HttpClientFactory.make()
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                uiThreadHandler.post {
                    imageView.setImageBitmap(BitmapFactory.decodeStream(response?.body()?.byteStream()))
                }
            }
        })
    }

    /**
     * Show confirm dialog.
     */
    private fun showConfirmDialog(context: Context, imageView: ImageView, url: String) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_download_image)
                .setMessage(R.string.message_confirm_downloading_image)
                .setView(imageView)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, { d, i -> d.cancel() })
                .setPositiveButton(
                        R.string.ok,
                        { d, i -> DownloadAction(context, url).invoke() }
                )
                .show()
    }
}