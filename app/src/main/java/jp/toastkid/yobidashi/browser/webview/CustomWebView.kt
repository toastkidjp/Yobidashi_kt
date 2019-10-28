package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.text.TextUtils
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.WebView
import androidx.annotation.Size
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.UrlFactory

/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
internal class CustomWebView(context: Context) : WebView(context), NestedScrollingChild {

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

    private val scrollOffset = IntArray(2)

    private val scrollConsumed = IntArray(2)

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
            nestedOffsetY = 0f
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY)

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY: Float = lastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY.toInt(), scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    lastY = eventY - scrollOffset[1].toFloat()
                    event.offsetLocation(0f, -scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                }
                requestDisallowInterceptTouchEvent(true)

                val returnValue = super.dispatchTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, scrollOffset[1], 0, deltaY.toInt(), scrollOffset)) {
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                    lastY -= scrollOffset[1]
                }
                return returnValue
            }
            MotionEvent.ACTION_DOWN -> {
                requestDisallowInterceptTouchEvent(true)
                val returnValue = super.dispatchTouchEvent(event)
                lastY = eventY.toFloat()
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
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


    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
                                      dyUnconsumed: Int, @Size(value = 2) offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int,
                                         @Size(value = 2) consumed: IntArray?,
                                         @Size(value = 2) offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }
}