package jp.toastkid.jitte.search.favorite

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

/**
 * @author toastkidjp
 */
@Table
class FavoriteSearch {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var category: String? = null

    @Column
    var query: String? = null
}