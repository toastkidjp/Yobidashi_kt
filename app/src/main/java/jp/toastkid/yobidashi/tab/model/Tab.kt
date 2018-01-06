package jp.toastkid.yobidashi.tab.model

/**
 * @author toastkidjp
 */
internal interface Tab {

    var thumbnailPath: String

    fun id(): String

    fun setScrolled(scrollY: Int)

    fun getScrolled(): Int

    fun getUrl(): String = ""

    fun back(): String = ""

    fun forward(): String = ""

    fun deleteLastThumbnail()

    fun title(): String
}