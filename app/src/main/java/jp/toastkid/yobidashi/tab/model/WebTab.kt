package jp.toastkid.yobidashi.tab.model

import android.text.TextUtils
import jp.toastkid.yobidashi.tab.History
import java.io.File
import java.util.*

/**
 * Model of web-browser tab.
 *
 * @author toastkidjp
 */
internal class WebTab: Tab {

    private val histories: MutableList<History> = mutableListOf()

    private val id: String = UUID.randomUUID().toString()

    override var thumbnailPath: String = ""

    @Transient var background = false

    @Synchronized override fun back(): String {
        return histories[0].url()
    }

    @Synchronized override fun forward(): String {
        return histories[0].url()
    }

    fun addHistory(history: History?) {
        if (history == null
                || TextUtils.equals(history.url(), "about:blank")
                || histories.contains(history)
        ) {
            return
        }

        if (histories.isEmpty()) histories.add(history) else histories.set(0, history)

        if (background) {
            histories.removeAt(0)
            background = false
        }
    }

    val latest: History
        get() {
            if (histories.isEmpty()) {
                return History.EMPTY
            }
            return histories[0]
        }

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

        fun make(title: String, url: String): WebTab = WebTab().apply {
            addHistory(History.make(title, url))
        }

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
