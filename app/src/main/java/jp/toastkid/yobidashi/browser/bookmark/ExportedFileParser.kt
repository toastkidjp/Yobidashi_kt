package jp.toastkid.yobidashi.browser.bookmark

import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
     * Root folder name.
     */
    private const val ROOT_FOLDER_NAME = "root"

    /**
     * Parse bookmark html file.
     *
     * @param htmlFile
     */
    fun parse(htmlFile: File): List<Bookmark> {
        val doc: Document = Jsoup.parse(htmlFile, "UTF-8")
        parseDl(doc.select("dl"))
        return bookmarks
    }

    /**
     * Parse dl tag's section.
     *
     * @param elements
     */
    private fun parseDl(elements: Elements) {
        val folder = Bookmark()
        folder.title = ROOT_FOLDER_NAME
        folder.folder = true
        bookmarks.add(folder)

        var childFolderName = ROOT_FOLDER_NAME
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