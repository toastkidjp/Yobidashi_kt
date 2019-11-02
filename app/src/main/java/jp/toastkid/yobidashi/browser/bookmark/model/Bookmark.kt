package jp.toastkid.yobidashi.browser.bookmark.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.toastkid.yobidashi.browser.bookmark.Bookmarks

/**
 * Bookmark model.
 *
 * @author toastkidjp
 */
@Entity(indices = [Index(value = ["parent", "folder"])])
class Bookmark {

    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    var title: String = ""

    var url: String = ""

    var favicon: String = ""

    var parent: String = Bookmarks.ROOT_FOLDER_NAME

    var folder: Boolean = false

    var viewCount: Int = 0

    var lastViewed: Long = 0

    override fun toString(): String {
        return "Bookmark(_id=$_id, title='$title', url='$url', favicon='$favicon', parent='$parent', folder=$folder, viewCount=$viewCount, lastViewed=$lastViewed)"
    }


}
