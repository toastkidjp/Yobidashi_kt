package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.MotionEvent
import android.webkit.WebView
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.webview.dialog.AnchorTypeLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ElseCaseLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ImageAnchorTypeLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ImageTypeLongTapDialogFragment
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
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
                    if (context is FragmentActivity) {
                        showDialogFragment(
                                ImageAnchorTypeLongTapDialogFragment.make(url),
                                context.supportFragmentManager
                        )
                    }
                    false
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    if (context is FragmentActivity) {
                        showDialogFragment(
                                ImageTypeLongTapDialogFragment.make(url),
                                context.supportFragmentManager
                        )
                    }
                    true
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    if (context is FragmentActivity) {
                        showDialogFragment(
                                AnchorTypeLongTapDialogFragment.make(url),
                                context.supportFragmentManager
                        )
                    }
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

    private fun showDialogFragment(
            dialogFragment: DialogFragment,
            supportFragmentManager: FragmentManager?
    ) {
        dialogFragment.setTargetFragment(
                supportFragmentManager?.findFragmentByTag(BrowserFragment::class.java.simpleName),
                1
        )
        dialogFragment.show(
                supportFragmentManager,
                dialogFragment::class.java.simpleName
        )
    }


    /**
     * Store image to file.
     *
     * @param url URL string.
     * @param context [Context]
     */
    private fun storeImage(url: String, context: Context): Maybe<File> {
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return Maybe.empty()
        }
        return Single.fromCallable { HTTP_CLIENT.newCall(Request.Builder().url(url).build()).execute() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter { it.code() == HttpURLConnection.HTTP_OK }
                .map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
                .map {
                    val storeroom = FilesDir(context, BackgroundSettingActivity.BACKGROUND_DIR)
                    val file = storeroom.assignNewFile(Uri.parse(url))
                    Bitmaps.compress(it, file)
                    file
                }
    }

    fun dispose() {
        disposables.clear()
    }
}
