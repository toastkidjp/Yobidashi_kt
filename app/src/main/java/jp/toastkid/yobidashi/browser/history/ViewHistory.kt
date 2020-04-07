package jp.toastkid.yobidashi.browser.history

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.search.url_suggestion.ViewHolder

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

    override fun bind(holder: ViewHolder) {
        holder.setTitle(title)
        holder.setUrl(url)
        holder.setIconResource(R.drawable.ic_history_black)
    }

    override fun urlString() = url

}

