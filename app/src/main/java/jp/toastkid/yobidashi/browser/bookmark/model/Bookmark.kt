package jp.toastkid.yobidashi.browser.bookmark.model

import android.content.Context
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.toastkid.yobidashi.libs.storage.FilesDir

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

    var parent: String = getRootFolderName()

    var folder: Boolean = false

    var viewCount: Int = 0

    var lastViewed: Long = 0

    override fun toString(): String {
        return "Bookmark(_id=$_id, title='$title', url='$url', favicon='$favicon', parent='$parent', folder=$folder, viewCount=$viewCount, lastViewed=$lastViewed)"
    }

    companion object {

        private const val ROOT_FOLDER_NAME: String = "root"

        fun getRootFolderName() = ROOT_FOLDER_NAME

        fun makeFaviconUrl(context: Context, url: String): String {
            val host = url.toUri().host ?: url
            return FilesDir(context, "favicons").assignNewFile("$host.png").absolutePath
        }

        fun make(
                title: String,
                url: String = "",
                faviconPath: String = "",
                parent: String = "parent",
                folder: Boolean = false
        ): Bookmark {
            val bookmark = Bookmark()
            bookmark.title = title
            bookmark.url = url
            bookmark.favicon = faviconPath
            bookmark.lastViewed = System.currentTimeMillis()
            bookmark.parent = parent
            bookmark.folder = folder
            return bookmark
        }
    }

}
