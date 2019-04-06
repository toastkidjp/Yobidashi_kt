package jp.toastkid.yobidashi.browser

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Method object of downloading image file.
 *
 * @author toastkidjp
 */
class ImageDownloadActionDialogFragment : DialogFragment() {

    /**
     * Use for setting image file.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val url = arguments?.getString(KEY_URL) ?: return super.onCreateDialog(savedInstanceState)

        @SuppressLint("InflateParams")
        val contentView = LayoutInflater.from(activityContext)
                .inflate(R.layout.content_dialog_image, null)
        val imageView = contentView.findViewById<ImageView>(R.id.image)
        downloadPreview(url, imageView)

        contentView.setOnClickListener {
            imageView.visibility = if (imageView.isVisible) View.GONE else View.VISIBLE
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_download_image)
                .setMessage(R.string.message_confirm_downloading_image)
                .setView(contentView)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    DownloadAction(activityContext, url)()
                    d.dismiss()
                }
                .create()
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
        client.newCall(Request.Builder().url(url).build()).enqueue(object : okhttp3.Callback {
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

    companion object {

        private const val KEY_URL = "url"

        fun show(context: Context, url: String) {
            val dialogFragment = ImageDownloadActionDialogFragment()

            if (context is FragmentActivity) {
                dialogFragment.arguments = bundleOf(KEY_URL to url)
                dialogFragment.show(
                        context.supportFragmentManager,
                        ImageDownloadActionDialogFragment::class.java.simpleName
                )
            }
        }
    }
}