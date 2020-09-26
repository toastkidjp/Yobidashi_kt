package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.WebView
import androidx.annotation.Size
import androidx.core.net.toUri
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.speech.SpeechMaker


/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
internal class CustomWebView(context: Context) : WebView(context), NestedScrollingChild3 {

    private val childHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)

    /**
     * Pull-to-Refresh availability.
     */
    var enablePullToRefresh = false

    /**
     * Scrolling value.
     */
    private var scrolling: Int = 0

    private var nestedOffsetY: Float = 0f

    private var lastY: Float = 0f

    private var nestedOffsetX: Float = 0f

    private var lastX: Float = 0f

    private val scrollOffset = IntArray(2)

    private val scrollConsumed = IntArray(2)

    private val speechMaker by lazy { SpeechMaker(context) }

    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        if (motionEvent?.action == MotionEvent.ACTION_UP) {
            scrolling = 0
        }

        if (isMultiTap(motionEvent)) {
            return super.dispatchTouchEvent(motionEvent)
        }

        val event = MotionEvent.obtain(motionEvent)
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetX = 0f
            nestedOffsetY = 0f
        }
        val eventX = event.x.toInt()
        val eventY = event.y.toInt()

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaX: Float = lastX - eventX
                var deltaY: Float = lastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(deltaX.toInt(), deltaY.toInt(), scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    lastY = eventY - scrollOffset[1].toFloat()
                    deltaX -= scrollConsumed[0]
                    lastX = eventX - scrollConsumed[0].toFloat()

                    event.offsetLocation(deltaX, deltaY)
                    nestedOffsetX += scrollOffset[0]
                    nestedOffsetY += scrollOffset[1]
                } else {
                    lastY = eventY.toFloat()
                }
                requestDisallowInterceptTouchEvent(true)

                val returnValue = super.dispatchTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(scrollOffset[0], scrollOffset[1], deltaX.toInt(), deltaY.toInt(), scrollOffset)) {
                    event.offsetLocation(scrollOffset[0].toFloat(), scrollOffset[1].toFloat())
                    nestedOffsetX += scrollOffset[0]
                    nestedOffsetY += scrollOffset[1]
                    lastY -= scrollOffset[1]
                }
                return returnValue
            }
            MotionEvent.ACTION_DOWN -> {
                requestDisallowInterceptTouchEvent(true)
                val returnValue = super.dispatchTouchEvent(event)
                lastX = eventX.toFloat()
                lastY = eventY.toFloat()
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
                return returnValue
            }
            MotionEvent.ACTION_UP -> {
                requestDisallowInterceptTouchEvent(false)
                val returnValue = super.dispatchTouchEvent(event)
                enablePullToRefresh = false
                // end NestedScroll
                stopNestedScroll()
                return returnValue
            }
            MotionEvent.ACTION_CANCEL -> {
                requestDisallowInterceptTouchEvent(false)
                val returnValue = super.dispatchTouchEvent(event)
                enablePullToRefresh = false
                // end NestedScroll
                stopNestedScroll()
                return returnValue
            }
        }
        return true
    }

    private fun isMultiTap(motionEvent: MotionEvent?) =
            (motionEvent?.pointerCount ?: 0) >= 2

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        enablePullToRefresh = scrolling <= 0
    }

    override fun onScrollChanged(horizontal: Int, vertical: Int, oldHorizontal: Int, oldVertical: Int) {
        super.onScrollChanged(horizontal, vertical, oldHorizontal, oldVertical)
        scrolling += vertical
    }

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? =
            super.startActionMode(
                    object : ActionMode.Callback {
                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            val menuInflater = MenuInflater(context)
                            menuInflater.inflate(R.menu.context_speech, menu)
                            menuInflater.inflate(R.menu.context_browser, menu)
                            return true
                        }

                        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                            when (item?.itemId) {
                                R.id.context_edit_speech -> {
                                    selectedTextExtractor.withAction(this@CustomWebView) {
                                        speechMaker.invoke(it)
                                    }
                                    mode?.finish()
                                    return true
                                }
                                R.id.preview_search -> {
                                    searchWithPreview()
                                    mode?.finish()
                                    return true
                                }
                                R.id.search_with_map_app -> {
                                    selectedTextExtractor.withAction(this@CustomWebView) { word ->
                                        val gmmIntentUri = "geo:0,0?q=$word".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        context.startActivity(mapIntent)
                                    }
                                    return true
                                }
                                R.id.web_search -> {
                                    search()
                                    mode?.finish()
                                    return true
                                }
                            }
                            return callback?.onActionItemClicked(mode, item) ?: false
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

    private fun search() {
        selectedTextExtractor.withAction(this@CustomWebView) { word ->
            context?.let {
                val url = urlFactory(
                        PreferenceApplier(it).getDefaultSearchEngine()
                                ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName(),
                        word
                ).toString()

                (it as? FragmentActivity)?.let { activity ->
                    ViewModelProvider(activity).get(BrowserViewModel::class.java)
                            .open(url.toUri())
                }
            }
        }
    }

    private fun searchWithPreview() {
        selectedTextExtractor.withAction(this@CustomWebView) { word ->
            context?.let {
                val url = urlFactory(
                        PreferenceApplier(it).getDefaultSearchEngine()
                                ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName(),
                        word
                )

                (it as? FragmentActivity)?.let { activity ->
                    ViewModelProvider(activity).get(BrowserViewModel::class.java)
                            .preview(url)
                }
            }
        }
    }


    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return childHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun stopNestedScroll(type: Int) {
        childHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return childHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            @Size(value = 2) offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
            dx: Int, dy: Int,
            @Size(value = 2) consumed: IntArray?,
            @Size(value = 2) offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?,
            type: Int,
            consumed: IntArray
    ) {
        return childHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow,
                type,
                consumed
        )
    }

    override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?,
            type: Int
    ): Boolean {
        return childHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow,
                type
        )
    }

    override fun dispatchNestedPreScroll(
            dx: Int,
            dy: Int,
            consumed: IntArray?,
            offsetInWindow: IntArray?,
            type: Int
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    companion object {

        private val urlFactory = UrlFactory()

        private val selectedTextExtractor = SelectedTextExtractor()

    }
}