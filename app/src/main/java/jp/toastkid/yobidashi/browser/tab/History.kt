package jp.toastkid.yobidashi.browser.tab

/**
 * History object.
 *
 * @author toastkidjp
 */
internal data class History internal constructor(private val title: String, private val url: String) {

    fun title(): String = title

    fun url(): String = url

    var scrolled: Int = 0

    companion object {

        /** Empty object.  */
        val EMPTY = History("", "")

        fun make(title: String, url: String): History = History(title, url)

    }
}
