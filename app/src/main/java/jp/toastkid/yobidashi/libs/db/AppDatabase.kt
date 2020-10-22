package jp.toastkid.yobidashi.libs.db
import androidx.room.Database
import androidx.room.RoomDatabase
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistory
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
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
        entities = [
            FavoriteSearch::class,
            SearchHistory::class,
            SavedColor::class,
            ViewHistory::class,
            Bookmark::class
        ],
        version = 1,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteSearchRepository(): FavoriteSearchRepository

    abstract fun searchHistoryRepository(): SearchHistoryRepository

    abstract fun savedColorRepository(): SavedColorRepository

    abstract fun viewHistoryRepository(): ViewHistoryRepository

    abstract fun bookmarkRepository(): BookmarkRepository

}