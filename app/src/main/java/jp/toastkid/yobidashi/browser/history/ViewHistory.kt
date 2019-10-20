package jp.toastkid.yobidashi.browser.history

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ViewHistory model.
 *
 * @author toastkidjp
 */
@Entity(indices = [Index(value = ["url"], unique = true)])
class ViewHistory {

    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    var title: String = ""

    var url: String = ""

    var favicon: String = ""

    var viewCount: Int = 0

    var lastViewed: Long = 0
}

