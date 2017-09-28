package jp.toastkid.yobidashi.browser

/**
 * Toolbar title pair.
 *
 * @author toastkidjp
 */
class TitlePair private constructor(private val title: String, private val subtitle: String) {

    fun title(): String = title

    fun subtitle(): String = subtitle

    companion object {

        fun make(title: String, subtitle: String): TitlePair = TitlePair(title, subtitle)
    }
}
