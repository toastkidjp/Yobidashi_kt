package jp.toastkid.yobidashi.browser.bookmark

import android.text.TextUtils
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File

/**
 * Parser of exported bookmark html file.
 *
 * @author toastkidjp
 */
object ExportedFileParser {

    /**
     * Bookmark container.
     */
    private val bookmarks: MutableList<Bookmark> = mutableListOf()

    /**
     * Bookmark html encoding.
     */
    private const val ENCODE = "UTF-8"

    /**
     * Parse bookmark html file.
     *
     * @param htmlFile
     */
    operator fun invoke(htmlFile: File): List<Bookmark> {
        Jsoup.parse(htmlFile, ENCODE).select("dl").first().children()
                .forEach { parseChild(it,  Bookmarks.ROOT_FOLDER_NAME)?.let { bookmarks.add(it) } }
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
                if (TextUtils.equals("a", it.tagName())) {
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
                child.select("dl").first()?.children()?.forEach {
                    parseChild(it, childFolder.title)?.let { bookmarks.add(it) }
                }
                return childFolder
            }
}