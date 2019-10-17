package jp.toastkid.yobidashi.search.favorite

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author toastkidjp
 */
@Entity(tableName = "FavoriteSearch")
class FavoriteSearch {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var category: String? = null

    var query: String? = null
}