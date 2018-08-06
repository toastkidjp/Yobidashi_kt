package jp.toastkid.yobidashi.browser

/**
 * Toolbar title pair.
 *
 * @param title Title
 * @param subtitle Subtitle
 *
 * @author toastkidjp
 */
class TitlePair private constructor(
        private val title: String,
        private val subtitle: String
) {

    fun title(): String = title

    fun subtitle(): String = subtitle

    companion object {

        /**
         * Make [TitlePair] with parameters.
         *
         * @param title Title
         * @param subtitle Subtitle
         * @return [TitlePair]
         */
        fun make(title: String?, subtitle: String?): TitlePair =
                TitlePair(title ?: "", subtitle ?: "")
    }
}
