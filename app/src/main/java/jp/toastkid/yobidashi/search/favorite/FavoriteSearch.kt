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

    companion object {

        /**
         * Make object.
         *
         * @param c Category string
         * @param q Query
         */
        fun with(c: String, q: String): FavoriteSearch {
            return FavoriteSearch().also {
                it.category = c
                it.query = q
            }
        }

    }
}