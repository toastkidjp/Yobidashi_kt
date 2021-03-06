package jp.toastkid.yobidashi.tab.model

import java.io.File
import java.util.UUID

/**
 * Model of editor tab.
 *
 * @author toastkidjp
 */
internal class EditorTab: Tab {

    private val editorTab: Boolean = true

    private val id: String = UUID.randomUUID().toString()

    private var titleStr: String = "Editor"

    var path: String = ""

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) = Unit

    override fun getScrolled(): Int = 0

    override fun title(): String = titleStr

    fun setFileInformation(file: File) {
        path = file.absolutePath
        titleStr = file.name
    }

}