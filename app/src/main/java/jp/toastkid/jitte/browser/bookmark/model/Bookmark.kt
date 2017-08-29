package jp.toastkid.jitte.browser.bookmark.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

/**
 * Bookmark model.
 *
 * @author toastkidjp
 */
@Table
class Bookmark: BookmarkItem {

    @PrimaryKey(autoincrement = true)
    var _id: Long = 0

    @Column
    var title: String = ""

    @Column
    var url: String = ""

    @Column
    var favicon: String = ""

    @Column(indexed = true)
    var parent: String = "root"

    @Column(indexed = true)
    var folder: Boolean = false

    @Column
    var view_count: Int = 0

    @Column
    var last_viewed: Long = 0

    override fun getName(): String {
        return title
    }

    fun getBookmarks(orma: OrmaDatabase): Bookmark_Relation {
        return orma.relationOfBookmark()._idEq(1)
    }

}
