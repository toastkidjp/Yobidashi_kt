package jp.toastkid.web.bookmark

import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Parser of exported bookmark html file.
 *
 * @author toastkidjp
 */
class ExportedFileParser {

    /**
     * Bookmark container.
     */
    private val bookmarks: MutableList<Bookmark> = mutableListOf()

    /**
     * Parse bookmark html file.
     *
     * @param htmlFile
     */
    operator fun invoke(htmlFile: File): List<Bookmark> = read(Jsoup.parse(htmlFile, ENCODE))

    /**
     * Parse bookmark html file.
     *
     * @param inputStream [InputStream]
     */
    operator fun invoke(inputStream: InputStream): List<Bookmark> = read(
            Jsoup.parse(
                    InputStreamReader(inputStream).use { it.readText() },
                    ENCODE
            )
    )

    private fun read(doc: Document): List<Bookmark> {
        doc.select("dl")
                .first()
                ?.children()
                ?.forEach { element ->
                    parseChild(element,  Bookmark.getRootFolderName())?.let { bookmarks.add(it) }
                }
        return bookmarks
    }

    /**
     * Parse child content.
     *
     * @param child
     * @param folderName
     */
    private fun parseChild(child: Element, folderName: String): Bookmark? =
            child.select("h3, a").first()?.let {
                if ("a" == it.tagName()) {
                    val bookmark = Bookmark()
                    bookmark.title  = it.text()
                    bookmark.url    = it.attr("href")
                    bookmark.parent = folderName
                    bookmark.folder = false
                    return bookmark
                }
                val childFolder = Bookmark()
                childFolder.parent = folderName
                childFolder.title  = it.text()
                childFolder.folder = true
                child.select("dl").first()?.children()?.forEach { element ->
                    parseChild(element, childFolder.title)?.let { bookmark -> bookmarks.add(bookmark) }
                }
                return childFolder
            }

    companion object {

        /**
         * Bookmark html encoding.
         */
        private const val ENCODE = "UTF-8"
    }
}