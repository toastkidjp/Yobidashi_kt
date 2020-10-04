package jp.toastkid.yobidashi.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.webview.dialog.AnchorTypeLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ElseCaseLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ImageAnchorTypeLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ImageTypeLongTapDialogFragment

/**
 * [WebView] factory.
 *
 * @author toastkidjp
 */
internal class WebViewFactory {

    /**
     * Use for only extract anchor URL.
     */
    private val handler = Handler(Handler.Callback { message ->
        message.data?.let(longTapItemHolder::extract)
        true
    })

    private val longTapItemHolder = LongTapItemHolder()

    /**
     * Color alpha converter.
     */
    private val alphaConverter = AlphaConverter()

    /**
     * Make new [WebView].
     *
     * @param context [Context]
     * @return [CustomWebView]
     */
    @SuppressLint("ClickableViewAccessibility")
    fun make(context: Context): CustomWebView {
        val webView = CustomWebView(context)
        webView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        val preferenceApplier = PreferenceApplier(context)

        webView.setOnLongClickListener {
            val hitResult = webView.hitTestResult
            when (hitResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val url = hitResult.extra ?: return@setOnLongClickListener false
                    webView.requestFocusNodeHref(handler.obtainMessage())
                    if (context is FragmentActivity) {
                        if (longTapItemHolder.anchor.isEmpty()) {
                            handler.postDelayed({
                                showImageAnchorDialog(url, context)
                                longTapItemHolder.reset()
                            }, 300L)

                            return@setOnLongClickListener true
                        }
                        showImageAnchorDialog(url, context)
                    }
                    false
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    val url = hitResult.extra ?: return@setOnLongClickListener false
                    if (context is FragmentActivity) {
                        showDialogFragment(
                                ImageTypeLongTapDialogFragment.make(url),
                                context.supportFragmentManager
                        )
                    }
                    true
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    webView.requestFocusNodeHref(handler.obtainMessage())

                    if (context is FragmentActivity) {
                        handler.postDelayed(
                                {
                                    showDialogFragment(
                                            AnchorTypeLongTapDialogFragment
                                                    .make(longTapItemHolder.title, longTapItemHolder.anchor),
                                            context.supportFragmentManager
                                    )
                                    longTapItemHolder.reset()
                                },
                                300L
                        )
                        return@setOnLongClickListener true
                    }
                    false
                }
                else -> {
                    val extra = hitResult.extra ?: return@setOnLongClickListener false

                    if (context is FragmentActivity) {
                        ElseCaseLongTapDialogFragment
                                .make(preferenceApplier.getDefaultSearchEngine() ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName(), extra)
                                .show(
                                        context.supportFragmentManager,
                                        ElseCaseLongTapDialogFragment::class.java.simpleName
                                )
                    }
                    false
                }
            }
        }

        WebSettingApplier(preferenceApplier).invoke(webView.settings)

        webView.isNestedScrollingEnabled = true
        webView.setBackgroundColor(alphaConverter.readBackground(context))

        return webView
    }

    /**
     * Show image anchor type dialog.
     *
     * @param url URL string
     * @param fragmentActivity [FragmentActivity]
     */
    private fun showImageAnchorDialog(url: String, fragmentActivity: FragmentActivity) {
        val dialogFragment = ImageAnchorTypeLongTapDialogFragment.make(
                longTapItemHolder.title,
                url,
                longTapItemHolder.anchor
        )

        showDialogFragment(
                dialogFragment,
                fragmentActivity.supportFragmentManager
        )
    }

    /**
     * Show dialog fragment.
     *
     * @param dialogFragment [DialogFragment]
     * @param supportFragmentManager [FragmentManager]
     */
    private fun showDialogFragment(
            dialogFragment: DialogFragment,
            supportFragmentManager: FragmentManager?
    ) {
        dialogFragment.setTargetFragment(
                supportFragmentManager?.findFragmentByTag(BrowserFragment::class.java.canonicalName),
                1
        )
        val fragmentManager = supportFragmentManager ?: return
        dialogFragment.show(
                fragmentManager,
                dialogFragment::class.java.simpleName
        )
    }

}
