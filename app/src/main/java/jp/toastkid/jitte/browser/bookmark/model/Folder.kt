package jp.toastkid.jitte.browser.bookmark.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

/**
 * @author toastkidjp
 */
@Table
class Folder: BookmarkItem {
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column(indexed = true, unique = true)
    var title: String = ""

    @Column(indexed = true, unique = true)
    var parent: String = ROOT.title

    override fun getName(): String {
        return title
    }

    fun getBookmarks(orma: OrmaDatabase): Bookmark_Relation {
        return orma.relationOfBookmark()._idEq(1)
    }

    companion object {

        val ROOT = Folder.make("ROOT")

        fun make(title: String): Folder {
            val folder = Folder()
            folder.title = title
            return folder
        }
    }

}
