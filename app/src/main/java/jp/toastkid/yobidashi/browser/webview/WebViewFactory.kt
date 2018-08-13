package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.view.MotionEvent
import android.webkit.WebView
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.ImageDownloadAction
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import okhttp3.Request
import java.io.File
import java.net.HttpURLConnection

/**
 * [WebView] factory.
 *
 * @author toastkidjp
 */
internal object WebViewFactory {

    private val disposables = CompositeDisposable()

    /**
     * HTTP Client.
     */
    private val HTTP_CLIENT by lazy { HttpClientFactory.make() }

    /**
     * Use for only extract anchor URL.
     */
    private val handler = Handler(Handler.Callback { message ->
        message?.data?.get("url")?.toString()?.let { anchor = it }
        true
    })

    /**
     * Extracted anchor URL.
     */
    private var anchor: String = ""

    fun make(context: Context, loader: (String, Boolean) -> Unit): CustomWebView {
        val webView = CustomWebView(context)
        webView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.getAction()) {
                MotionEvent.ACTION_UP -> webView.enablePullToRefresh = false
            }
            false
        }

        val preferenceApplier = PreferenceApplier(context)

        webView.setOnLongClickListener { v ->
            val hitResult = webView.hitTestResult
            when (hitResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    webView.requestFocusNodeHref(handler.obtainMessage())
                    AlertDialog.Builder(webView.context)
                            .setTitle("Image: " + url)
                            .setItems(R.array.image_anchor_menu, { dialog, which ->
                                when (which) {
                                    0 -> loader(anchor, false)//openNewTab(url)
                                    1 -> loader(anchor, true)//openBackgroundTab(url)
                                    2 -> webView.loadUrl(anchor)
                                    3 -> {
                                        storeImage(url, webView).subscribe { file ->
                                            preferenceApplier.backgroundImagePath = file.absolutePath
                                            Toaster.snackShort(
                                                    webView,
                                                    R.string.message_change_background_image,
                                                    preferenceApplier.colorPair()
                                            )
                                        }.addTo(disposables)
                                    }
                                    4 -> storeImage(url, webView).subscribe({
                                        Toaster.snackShort(
                                                webView,
                                                R.string.message_done_save,
                                                preferenceApplier.colorPair()
                                        )
                                    }).addTo(disposables)
                                    5 -> ImageDownloadAction(webView, hitResult).invoke()
                                    6 -> Clipboard.clip(v.context, anchor)
                                }
                                anchor = ""
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.cancel, { d, i -> d.cancel() })
                            .show()
                    false
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    AlertDialog.Builder(webView.context)
                            .setTitle("Image: " + url)
                            .setItems(R.array.image_menu, { dialog, which ->
                                when (which) {
                                    0 -> {
                                        storeImage(url, webView).subscribe { file ->
                                            preferenceApplier.backgroundImagePath = file.absolutePath
                                            Toaster.snackShort(
                                                    webView,
                                                    R.string.message_change_background_image,
                                                    preferenceApplier.colorPair()
                                            )
                                        }.addTo(disposables)
                                    }
                                    1 -> storeImage(url, webView).subscribe({
                                        Toaster.snackShort(
                                                webView,
                                                R.string.message_done_save,
                                                preferenceApplier.colorPair()
                                        )
                                    }).addTo(disposables)
                                    2 -> ImageDownloadAction(webView, hitResult).invoke()
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.cancel, { d, i -> d.cancel() })
                            .show()
                    false
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    AlertDialog.Builder(context)
                            .setTitle("URL: $url")
                            .setItems(R.array.url_menu, { _, which ->
                                when (which) {
                                    0 -> loader(url, false)//openNewTab(url)
                                    1 -> loader(url, true)//openBackgroundTab(url)
                                    2 -> webView.loadUrl(url)
                                    3 -> Clipboard.clip(v.context, url)
                                }
                            })
                            .setNegativeButton(R.string.cancel, { d, i -> d.cancel() })
                            .show()
                    false
                }
                else -> {
                    val extra = hitResult.extra
                    if (extra == null || extra.isEmpty()) {
                        return@setOnLongClickListener false
                    }

                    if (context is FragmentActivity) {
                        ElseCaseLongTapDialogFragment
                                .make(preferenceApplier.getDefaultSearchEngine(), extra)
                                .show(
                                        context.supportFragmentManager,
                                        ElseCaseLongTapDialogFragment::class.java.simpleName
                                )
                    }
                    false
                }
            }
        }
        val settings = webView.settings
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        return webView
    }

    /**
     * Store image to file.
     *
     * @param url URL string.
     * @param webView [WebView] instance
     */
    private fun storeImage(url: String, webView: WebView): Maybe<File> {
        val context: Context = webView.context
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return Maybe.empty()
        }
        return Single.fromCallable {
            HTTP_CLIENT.newCall(Request.Builder().url(url).build()).execute()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter { it.code() == HttpURLConnection.HTTP_OK }
                .map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
                .map {
                    val storeroom = FilesDir(webView.context, BackgroundSettingActivity.BACKGROUND_DIR)
                    val file = storeroom.assignNewFile(Uri.parse(url))
                    Bitmaps.compress(it, file)
                    file
                }
    }

    fun dispose() {
        disposables.clear()
    }
}
