package jp.toastkid.yobidashi.browser.bookmark.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table
import jp.toastkid.yobidashi.browser.bookmark.Bookmarks

/**
 * Bookmark model.
 *
 * @author toastkidjp
 */
@Table
class Bookmark {

    @PrimaryKey(autoincrement = true)
    var _id: Long = 0

    @Column
    var title: String = ""

    @Column
    var url: String = ""

    @Column
    var favicon: String = ""

    @Column(indexed = true)
    var parent: String = Bookmarks.ROOT_FOLDER_NAME

    @Column(indexed = true)
    var folder: Boolean = false

    @Column
    var view_count: Int = 0

    @Column
    var last_viewed: Long = 0

    fun getBookmarks(orma: OrmaDatabase): Bookmark_Relation {
        return orma.relationOfBookmark()._idEq(1)
    }

}
