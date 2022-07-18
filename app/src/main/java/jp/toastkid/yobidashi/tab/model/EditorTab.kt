package jp.toastkid.yobidashi.tab.model

import androidx.annotation.Keep
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.UUID

/**
 * Model of editor tab.
 *
 * @author toastkidjp
 */
@Serializable
@SerialName("editor")
internal class EditorTab: Tab {

    @Required
    @Keep
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