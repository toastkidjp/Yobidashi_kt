package jp.toastkid.yobidashi.browser.tab

import android.text.TextUtils
import java.util.*

/**
 * Model of web-browser tab.
 *
 * @author toastkidjp
 */
internal class Tab {

    internal val histories: MutableList<History> = mutableListOf()

    private var index: Int = -1

    val id: String = UUID.randomUUID().toString()

    var thumbnailPath: String = ""

    var lastTitle: String? = null

    @Transient var background = false

    @Synchronized fun back(): String {
        val nextIndex = if (index == 0) index else index - 1
        val sameIndex = nextIndex == index
        index = nextIndex
        return if (sameIndex) "" else histories[nextIndex].url()
    }

    @Synchronized fun forward(): String {
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

    companion object {

        fun makeBackground(title: String, url: String): Tab {
            val backgroundTab = Tab()
            backgroundTab.addHistory(History.make(title, url))
            backgroundTab.background = true
            return backgroundTab
        }
    }

}
