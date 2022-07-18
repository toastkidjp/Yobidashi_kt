package jp.toastkid.yobidashi.tab.model

import androidx.annotation.Keep
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * PDF tab.
 *
 * @author toastkidjp
 */
@Serializable
@SerialName("pdf")
class PdfTab: Tab {

    @Required
    @Keep
    private val pdfTab: Boolean = true

    private var titleStr: String = "PDF Viewer"

    private var path: String = ""

    private val id: String = UUID.randomUUID().toString()

    private var position: Int = 0

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) {
        position = scrollY
    }

    override fun getScrolled(): Int = position

    override fun title(): String = titleStr

    override fun getUrl(): String = path

    fun setTitle(title: String) {
        titleStr = title
    }

    fun setPath(path: String) {
        this.path = path
    }
}