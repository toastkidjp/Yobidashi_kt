package jp.toastkid.yobidashi.libs.db
import androidx.room.Database
import androidx.room.RoomDatabase
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchRepository
import jp.toastkid.yobidashi.search.history.SearchHistory
import jp.toastkid.yobidashi.search.history.SearchHistoryRepository
import jp.toastkid.yobidashi.settings.color.SavedColor
import jp.toastkid.yobidashi.settings.color.SavedColorRepository

/**
 * @author toastkidjp
 */
@Database(
        entities = [FavoriteSearch::class, SearchHistory::class, SavedColor::class],
        version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteSearchRepository(): FavoriteSearchRepository

    abstract fun searchHistoryRepository(): SearchHistoryRepository

    abstract fun savedColorRepository(): SavedColorRepository
}