package jp.toastkid.yobidashi.libs.db

import android.content.Context

import jp.toastkid.yobidashi.search.favorite.OrmaDatabase

/**
 * @author toastkidjp
 */
object DbInitter {

    private var orma: OrmaDatabase? = null

    operator fun get(context: Context): OrmaDatabase {
        if (orma == null) {
            orma = OrmaDatabase.builder(context).name("yobidashi.db").build()
        }
        return orma
    }
}