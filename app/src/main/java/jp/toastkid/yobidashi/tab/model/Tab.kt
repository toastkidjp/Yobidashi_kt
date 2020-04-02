package jp.toastkid.yobidashi.tab.model

/**
 * @author toastkidjp
 */
interface Tab {

    fun thumbnailPath(): String = "${id()}.png"

    fun id(): String

    fun setScrolled(scrollY: Int)

    fun getScrolled(): Int

    fun getUrl(): String = ""

    fun back(): String = ""

    fun forward(): String = ""

    fun title(): String
}