package jp.toastkid.yobidashi.tab.model

import java.io.File
import java.util.*

/**
 * PDF tab.
 *
 * @author toastkidjp
 */
class PdfTab: Tab {

    private val pdfTab: Boolean = true

    private var titleStr: String = "PDF Viewer"

    private var path: String = ""

    var thumbnailPath: String = ""

    private val id: String = UUID.randomUUID().toString()

    override fun id(): String = id

    override fun setScrolled(scrollY: Int) = Unit

    override fun getScrolled(): Int = 0

    override fun deleteLastThumbnail() {
        val lastScreenshot = File(thumbnailPath)
        if (lastScreenshot.exists()) {
            lastScreenshot.delete()
        }
    }

    override fun title(): String = titleStr

    override fun getUrl(): String = path

    fun setTitle(title: String) {
        titleStr = title
    }

    fun setPath(path: String) {
        this.path = path
    }
}