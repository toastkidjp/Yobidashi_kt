package jp.toastkid.yobidashi.browser.tab

import java.util.*

/**
 * Model of editor tab.
 *
 * @author toastkidjp
 */
internal class EditorTab: Tab {

    private val editorTab: Boolean = true

    private val id: String = UUID.randomUUID().toString()

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) = Unit

    override fun getScrolled(): Int = 0

    override fun deleteLastThumbnail() = Unit

}