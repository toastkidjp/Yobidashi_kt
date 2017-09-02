package jp.toastkid.yobidashi.libs.db

import android.content.Context
import jp.toastkid.yobidashi.browser.bookmark.model.OrmaDatabase

/**
 * @author toastkidjp
 */
object DbInitter {

    private var orma: OrmaDatabase? = null

    fun init(context: Context): OrmaDatabase {
        if (orma == null) {
            orma = OrmaDatabase.builder(context).name("yobidashi.db").build()
        }
        return orma as OrmaDatabase
    }
}