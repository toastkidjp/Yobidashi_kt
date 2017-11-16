package jp.toastkid.yobidashi.browser.bookmark

import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.File

/**
 * Parser of exported bookmark html file.
 *
 * @author toastkidjp
 */
object ExportedFileParser {

    /**
     * Bookmark items.
     */
    private val bookmarks: MutableList<Bookmark> = mutableListOf()

    /**
     * Parse bookmark html file.
     *
     * @param htmlFile
     */
    operator fun invoke(htmlFile: File): List<Bookmark> {
        parseDl(Jsoup.parse(htmlFile, "UTF-8").select("dl"))
        return bookmarks
    }

    /**
     * Parse dl tag's section.
     *
     * @param elements
     */
    private fun parseDl(elements: Elements) {
        var childFolderName = Bookmarks.ROOT_FOLDER_NAME
        elements.toList().forEach {
            it.children().toList().forEach { childFolderName = parseChild(it, childFolderName) }
        }
    }

    /**
     * Parse child content.
     *
     * @param child
     * @param folderName
     */
    private fun parseChild(child: Element, folderName: String): String {
        var childFolderName = folderName
        child.children().toList().forEach {
            when (it.tagName()) {
                "dl" -> {
                    parseChild(it, childFolderName)
                }
                "h3" -> {
                    childFolderName = it.text()
                    val childFolder = Bookmark()
                    childFolder.title = childFolderName
                    childFolder.parent = folderName
                    childFolder.folder = true
                    bookmarks.add(childFolder)
                }
                "a" -> {
                    val bookmark = Bookmark()
                    bookmark.title = it.text()
                    bookmark.url = it.attr("href")
                    bookmark.parent = childFolderName
                    bookmark.folder = false
                    bookmarks.add(bookmark)
                }
            }
        }
        return childFolderName
    }
}