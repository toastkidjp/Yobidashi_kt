package jp.toastkid.yobidashi.search.history

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

/**
 * Search history.

 * @author toastkidjp
 */
@Table
class SearchHistory {

    @PrimaryKey
    var key: String? = null

    @Column
    var category: String? = null

    @Column
    var query: String? = null

    @Column(indexed = true)
    var timestamp: Long = 0

}
