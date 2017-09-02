package jp.toastkid.yobidashi.browser.history

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

/**
 * ViewHistory model.
 *
 * @author toastkidjp
 */
@Table
class ViewHistory {

    @PrimaryKey(autoincrement = true)
    var _id: Long = 0

    @Column
    var title: String = "";

    @Column(unique = true)
    var url: String = "";

    @Column
    var favicon: String = ""

    @Column
    var view_count: Int = 0

    @Column
    var last_viewed: Long = 0
}

