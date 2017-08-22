package jp.toastkid.jitte.browser.tab

import java.util.*

/**
 * @author toastkidjp
 */
internal class Tab {

    private val histories: MutableList<History>

    private var index: Int = 0

    val id: String = UUID.randomUUID().toString()

    var thumbnailPath: String

    var lastTitle: String? = null

    init {
        histories = ArrayList<History>()
        thumbnailPath = ""
        index = -1
    }

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

    fun addHistory(history: History) {
        histories.add(history)
        index++
    }

    val latest: History
        get() {
            if (index < 0 || histories.size <= index) {
                return History.EMPTY
            }
            return histories[index]
        }
}
