package jp.toastkid.yobidashi.browser.history

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.toastkid.yobidashi.browser.UrlItem

/**
 * ViewHistory model.
 *
 * @author toastkidjp
 */
@Entity(indices = [Index(value = ["url"], unique = true)])
class ViewHistory : UrlItem {

    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    var title: String = ""

    var url: String = ""

    var favicon: String = ""

    var viewCount: Int = 0

    var lastViewed: Long = 0

    override fun urlString() = url

    override fun itemId() = _id

}

