package jp.toastkid.yobidashi.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.WebView
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.UrlFactory

/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
internal class CustomWebView(context: Context) : WebView(context) {

    var enablePullToRefresh = false

    var scrollListener: (Int, Int, Int, Int) -> Unit = { _, _, _, _  -> }

    var scrolling: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            scrolling = 0
        }
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        enablePullToRefresh = scrolling <= 0
    }

    override fun onScrollChanged(horizontal: Int, vertical: Int, oldHorizontal: Int, oldVertical: Int) {
        super.onScrollChanged(horizontal, vertical, oldHorizontal, oldVertical)
        scrolling += vertical
        scrollListener(horizontal, vertical, oldHorizontal, oldVertical)
    }

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode =
            super.startActionMode(
                    object : ActionMode.Callback {
                        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                            if (TextUtils.equals("Web search", item?.title)) {
                                SelectedTextExtractor.withAction(this@CustomWebView) { word ->
                                    context?.let {
                                        val url = UrlFactory.make(
                                                it,
                                                PreferenceApplier(it).getDefaultSearchEngine(),
                                                word
                                        ).toString()

                                        loadUrl(url)
                                    }
                                    val activityContext = context ?: return@withAction

                                }
                                return true
                            }
                            return callback?.onActionItemClicked(mode, item) ?: false
                        }

                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            return callback?.onCreateActionMode(mode, menu) ?: false
                        }

                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            return callback?.onPrepareActionMode(mode, menu) ?: false
                        }

                        override fun onDestroyActionMode(mode: ActionMode?) {
                            callback?.onDestroyActionMode(mode)
                        }
                    },
                    type
            )
}