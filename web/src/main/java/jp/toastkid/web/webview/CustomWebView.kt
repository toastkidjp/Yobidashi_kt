package jp.toastkid.web.webview

import android.content.Context
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.translate.TranslationUrlGenerator
import jp.toastkid.libs.speech.SpeechMaker
import jp.toastkid.web.R
import jp.toastkid.web.webview.usecase.SelectedTextUseCase

/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
class CustomWebView(context: Context) : WebView(context) {

    /**
     * Pull-to-Refresh availability.
     */
    var enablePullToRefresh = false

    private var nestedOffsetY: Float = 0f

    private var lastY: Float = 0f

    private var nestedOffsetX: Float = 0f

    private var lastX: Float = 0f

    private val scrollOffset = IntArray(2)

    private val scrollConsumed = IntArray(2)

    private val speechMaker by lazy { SpeechMaker(context) }

    private val contentViewModel = (context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
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

                if (enablePullToRefresh && (deltaY < 0)) {
                    nestedScrollDispatcher?.dispatchPreScroll(
                        Offset(0f, deltaY),
                        NestedScrollSource.UserInput
                    )
                    return true
                }
                enablePullToRefresh = false

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
                if (event.action == MotionEvent.ACTION_DOWN) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                val returnValue = super.dispatchTouchEvent(event)
                lastX = eventX.toFloat()
                lastY = eventY.toFloat()
                // start NestedScroll
                //startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
                return returnValue
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val returnValue = super.dispatchTouchEvent(event)
                enablePullToRefresh = false
                nestedScrollDispatcher?.dispatchPostScroll(
                    Offset.Zero,
                    Offset.Zero,
                    NestedScrollSource.UserInput
                )
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
        if (clampedX || clampedY) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        enablePullToRefresh = clampedY && this.scrollY == 0
    }

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? =
            super.startActionMode(
                    object : ActionMode.Callback {
                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            if (enablePullToRefresh) {
                                return false
                            }
                            val menuInflater = MenuInflater(context)
                            menuInflater.inflate(R.menu.context_web, menu)
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
                                    selectedTextExtractor.withAction(this@CustomWebView) {
                                        context.startActivity(
                                                Intent(Intent.ACTION_VIEW, "geo:0,0?q=$it".toUri())
                                        )
                                    }
                                    mode?.finish()
                                    return true
                                }
                                R.id.web_search -> {
                                    search()
                                    mode?.finish()
                                    return true
                                }
                                R.id.translate -> {
                                    selectedTextExtractor.withAction(this@CustomWebView) {
                                        if (it.isBlank()) {
                                            contentViewModel?.snackShort(R.string.message_failed_query_extraction_from_web_view)
                                            return@withAction
                                        }
                                        contentViewModel?.preview(TranslationUrlGenerator()(it))
                                    }
                                    mode?.finish()
                                    return true
                                }
                                R.id.count -> {
                                    countSelectedCharacters()
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

    private fun countSelectedCharacters() {
        selectedTextExtractor.withAction(this) {
            SelectedTextUseCase.make(context)?.countCharacters(it)
        }
    }

    private fun search() {
        selectedTextExtractor.withAction(this@CustomWebView) { word ->
            SelectedTextUseCase.make(context)?.search(word)
        }
    }

    private fun searchWithPreview() {
        selectedTextExtractor.withAction(this@CustomWebView) { word ->
            SelectedTextUseCase.make(context)?.searchWithPreview(word)
        }
    }

    private var nestedScrollDispatcher: NestedScrollDispatcher? = null

    fun setNestedScrollDispatcher(nestedScrollDispatcher: NestedScrollDispatcher) {
        this.nestedScrollDispatcher = nestedScrollDispatcher
    }

    override fun destroy() {
        nestedScrollDispatcher = null
        super.destroy()
    }

    companion object {

        private val selectedTextExtractor = SelectedTextExtractor()

    }
}