package jp.toastkid.jitte

/**
 * Toolbar title pair.

 * TODO write test

 * @author toastkidjp
 */
class TitlePair private constructor(private val title: String, private val subtitle: String) {

    fun title(): String {
        return title
    }

    fun subtitle(): String {
        return subtitle
    }

    companion object {

        fun make(title: String, subtitle: String): TitlePair {
            return TitlePair(title, subtitle)
        }
    }
}
