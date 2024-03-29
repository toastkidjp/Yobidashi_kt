package jp.toastkid.yobidashi.tab

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * History object.
 *
 * @param title Title
 * @param url URL
 * @author toastkidjp
 */
@Serializable
data class History internal constructor(
    @Required @SerialName("a") private val title: String,
    @Required @SerialName("b") private val url: String
) {

    /**
     * Return Title.
     *
     * @return Title
     */
    fun title(): String = title

    /**
     * Return URL.
     *
     * @return URL
     */
    fun url(): String = url

    /**
     * Scrolled value.
     *
     * @return scrolled value
     */
    var scrolled: Int = 0

    companion object {

        /**
         * Empty object.
         */
        val EMPTY = History("", "")

        /**
         * Make object with title and URL.
         *
         * @param title
         * @param url
         */
        fun make(title: String, url: String): History = History(title, url)

    }
}
