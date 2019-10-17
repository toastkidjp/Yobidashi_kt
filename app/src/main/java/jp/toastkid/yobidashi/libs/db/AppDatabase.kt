package jp.toastkid.yobidashi.libs.db
import androidx.room.Database
import androidx.room.RoomDatabase
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchRepository

/**
 * @author toastkidjp
 */
@Database(entities = [FavoriteSearch::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteSearchRepository(): FavoriteSearchRepository
}