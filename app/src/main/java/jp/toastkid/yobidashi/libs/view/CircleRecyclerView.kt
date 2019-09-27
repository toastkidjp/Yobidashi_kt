package jp.toastkid.yobidashi.libs.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import java.lang.ref.WeakReference

/**
 * @author kHRYSTAL <723526676@qq.com>
 * @author toastkidjp
 */
class CircleRecyclerView @JvmOverloads constructor(
        context: Context,
        @Nullable attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle), View.OnClickListener {

    private var isForceCentering: Boolean = false
    private val centerRunnable = CenterRunnable()
    private val viewMode: CircularViewMode?
    private var needCenterForce: Boolean = false
    private var needLoop = true
    private var centerItemClickListener: OnCenterItemClickListener? = null
    private var currentCenterChildView: View? = null
    private var onScrollListener: OnScrollListener? = null
    private var firstOnLayout: Boolean = false
    private var firstSetAdapter = true

    private val postHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            scrollToPosition(DEFAULT_SELECTION)
        }
    }

    init {
        viewMode = CircularViewMode()
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (needLoop) {
            if (!firstOnLayout) {
                firstOnLayout = true
                postHandler.sendEmptyMessage(0)
            }
            //            scrollToPosition(DEFAULT_SELECTION);
            currentCenterChildView = findViewAtCenter()
            smoothScrollToView(currentCenterChildView)
        } else if (!needLoop && needCenterForce) {
            val layoutManager = layoutManager as LinearLayoutManager
            if (layoutManager.canScrollHorizontally()) {
                setPadding(width / 2, 0, width / 2, 0)
            } else if (layoutManager.canScrollVertically()) {
                setPadding(0, height / 2, 0, height / 2)
            }
            clipToPadding = false
            clipChildren = false
            currentCenterChildView = findViewAtCenter()
            smoothScrollToView(currentCenterChildView)
        } else {
            clipToPadding = false
            clipChildren = false
        }

        currentCenterChildView?.setOnClickListener(this)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (viewMode != null) {
            val count = childCount
            for (i in 0 until count) {
                val v = getChildAt(i)
                if (v !== currentCenterChildView && centerItemClickListener != null) {
                    v.setOnClickListener(null)
                }

                v.setTag(R.string.is_center, v === currentCenterChildView)

                viewMode.applyToView(v, this)
            }
        }

        onScrollListener?.onScrollChanged(l, t, oldl, oldt)
    }

    override fun requestLayout() {
        super.requestLayout()

        val layoutManager = layoutManager ?: return
        val count = layoutManager.childCount ?: 0
        for (i in 0 until count) {
            val v = getChildAt(i)
            if (v !== currentCenterChildView && centerItemClickListener != null) {
                v.setOnClickListener(null)
            }
            v.setTag(R.string.is_center, v === currentCenterChildView)

            viewMode?.applyToView(v, this)
        }
    }

    fun smoothScrollToView(@Nullable v: View?) {
        if (v == null) {
            return
        }
        var distance = 0
        val layoutManager = layoutManager
        if (layoutManager is LinearLayoutManager) {
            if (layoutManager.canScrollVertically()) {
                val y = v.y + v.height * 0.5f
                val halfHeight = height * 0.5f
                distance = (y - halfHeight).toInt()
            } else if (layoutManager.canScrollHorizontally()) {
                val x = v.x + v.width * 0.5f
                val halfWidth = width * 0.5f
                distance = (x - halfWidth).toInt()
            }

        } else {
            throw IllegalArgumentException("CircleRecyclerView just support T extend LinearLayoutManager!")
        }
        smoothScrollBy(distance, distance)
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        onScrollListener?.onScrolled(dx, dy)
    }

    override fun onScrollStateChanged(state: Int) {
        if (state == RecyclerView.SCROLL_STATE_IDLE
                && needCenterForce
                && !isForceCentering) {
            isForceCentering = true
            currentCenterChildView = findViewAtCenter()
            if (currentCenterChildView != null && centerItemClickListener != null)
                currentCenterChildView?.setOnClickListener(this)
            centerRunnable.setView(currentCenterChildView)
            ViewCompat.postOnAnimation(this, centerRunnable)
        }

        onScrollListener?.onScrollStateChanged(state)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        removeCallbacks(centerRunnable)
        isForceCentering = false
        return super.onTouchEvent(e)
    }

    private fun findViewAt(x: Int, y: Int): View? {
        val count = childCount
        for (i in 0 until count) {
            val v = getChildAt(i)
            val x0 = v.left
            val y0 = v.top
            val x1 = v.width + x0
            val y1 = v.height + y0
            if (x in x0..x1 && y >= y0 && y <= y1) {
                return v
            }
        }
        return null
    }

    private fun findViewAtCenter(): View? {
        val layoutManager = layoutManager ?: return null
        if (layoutManager.canScrollVertically()) {
            return findViewAt(0, height / 2)
        } else if (layoutManager.canScrollHorizontally()) {
            return findViewAt(width / 2, 0)
        }
        return null
    }

    override fun onClick(v: View) {
        centerItemClickListener?.onCenterItemClick(v)
    }

    inner class CenterRunnable : Runnable {

        private var mView: WeakReference<View>? = null

        fun setView(v: View?) {
            mView = WeakReference<View>(v)
        }

        override fun run() {
            smoothScrollToView(mView?.get())
            if (needCenterForce) {
                isForceCentering = true
            }
        }
    }

    /**
     * default needLoop is true
     * if not needLoop && centerForce
     * will setPadding your layoutManger direction half width or height
     * and setClipPadding(false), setClipChildren(false)
     * @param needLoop default true
     */
    fun setNeedLoop(needLoop: Boolean) {
        this.needLoop = needLoop
    }

    interface OnCenterItemClickListener {
        fun onCenterItemClick(v: View)
    }

    interface OnScrollListener {
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
        fun onScrollStateChanged(state: Int)
        fun onScrolled(dx: Int, dy: Int)
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        super.setAdapter(adapter)
        if (firstSetAdapter) {
            firstSetAdapter = false
            return
        }
        if (adapter != null && needCenterForce) {
            postHandler.sendEmptyMessage(0)
        }
    }

    fun setMode(useLeft: Boolean) {
        if (useLeft) {
            viewMode?.setLeftMode()
        } else {
            viewMode?.setRightMode()
        }
    }

    companion object {

        private const val DEFAULT_SELECTION = Integer.MAX_VALUE shr 1
    }
}