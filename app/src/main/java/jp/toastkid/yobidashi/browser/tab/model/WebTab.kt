package jp.toastkid.yobidashi.browser.tab.model

import android.text.TextUtils
import jp.toastkid.yobidashi.browser.tab.History
import java.io.File
import java.util.*

/**
 * Model of web-browser tab.
 *
 * @author toastkidjp
 */
internal class WebTab: Tab {

    internal val histories: MutableList<History> = mutableListOf()

    private var index: Int = -1

    private val id: String = UUID.randomUUID().toString()

    var thumbnailPath: String = ""

    @Transient var background = false

    @Synchronized override fun back(): String {
        val nextIndex = if (index == 0) index else index - 1
        if (nextIndex < 0) {
            return ""
        }
        val sameIndex = nextIndex == index
        index = nextIndex
        return if (sameIndex) "" else histories[nextIndex].url()
    }

    @Synchronized override fun forward(): String {
        val nextIndex = if (index == histories.size - 1) index else index + 1
        val sameIndex = nextIndex == index
        index = nextIndex
        return if (sameIndex) "" else histories[nextIndex].url()
    }

    internal fun moveAndGet(newIndex: Int): String {
        if (newIndex < 0 || histories.size <= newIndex) {
            return ""
        }
        index = newIndex
        return histories[index].url()
    }

    fun addHistory(history: History) {
        if (TextUtils.equals(history.url(), "about:blank")
                || histories.contains(history)) {
            return
        }

        histories.add(history)

        if (background) {
            histories.removeAt(0)
            background = false
            return
        }
        index++
    }

    val latest: History
        get() {
            if (index < 0 || histories.size <= index) {
                return History.EMPTY
            }
            return histories[index]
        }

    internal fun currentIndex() : Int = index

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) {
        latest.scrolled = scrollY
    }

    override fun getScrolled(): Int = latest.scrolled

    override fun getUrl(): String = latest.url()

    override fun deleteLastThumbnail() {
        val lastScreenshot = File(thumbnailPath)
        if (lastScreenshot.exists()) {
            lastScreenshot.delete()
        }
    }

    override fun title(): String = latest.title()

    companion object {

        /**
         * Make [WebTab] for opening by background.
         *
         * @param title Title
         * @param url URL
         */
        fun makeBackground(title: String, url: String): WebTab = WebTab().apply {
            addHistory(History.make(title, url))
            background = true
        }
    }

}
