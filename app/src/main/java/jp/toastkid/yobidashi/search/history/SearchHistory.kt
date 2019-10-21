package jp.toastkid.yobidashi.search.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Search history.
 *
 * @author toastkidjp
 */
@Entity(tableName = "SearchHistory")
class SearchHistory {

    @PrimaryKey
    var key: String = ""

    var category: String? = ""

    var query: String? = ""

    @ColumnInfo(index = true)
    var timestamp: Long = 0

    companion object {

        fun make(category: String, query: String): SearchHistory {
            val sh = SearchHistory()
            sh.key = category + query
            sh.category = category
            sh.query = query
            sh.timestamp = System.currentTimeMillis()
            return sh
        }
    }
}
