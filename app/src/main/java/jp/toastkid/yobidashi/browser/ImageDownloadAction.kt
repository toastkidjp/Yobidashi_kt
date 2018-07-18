package jp.toastkid.yobidashi.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import androidx.core.view.isVisible
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Method object of downloading image file.
 *
 * @author toastkidjp
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

        @SuppressLint("InflateParams")
        val contentView = LayoutInflater.from(context)
                .inflate(R.layout.content_dialog_image, null)
        val imageView = contentView.findViewById<ImageView>(R.id.image)
        downloadPreview(url, imageView)

        contentView.setOnClickListener {
            imageView.visibility = if (imageView.isVisible) View.GONE else View.VISIBLE
        }

        showConfirmDialog(context, contentView, url)
    }

    /**
     * Download preview image.
     */
    private fun downloadPreview(url: String, imageView: ImageView) {
        val context: Context = imageView.context
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }
        val client = HttpClientFactory.make()
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Timber.e(e)
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
    private fun showConfirmDialog(context: Context, view: View, url: String) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_download_image)
                .setMessage(R.string.message_confirm_downloading_image)
                .setView(view)
                .setNegativeButton(R.string.cancel, { d, _ -> d.cancel() })
                .setPositiveButton(
                        R.string.ok,
                        { d, _ ->
                            DownloadAction(context, url).invoke()
                            d.dismiss()
                        }
                )
                .show()
    }
}