package jp.toastkid.yobidashi.settings.color

import android.content.Context

import jp.toastkid.yobidashi.search.favorite.OrmaDatabase

/**
 * @author toastkidjp
 */
internal object DbInitter {

    private var orma: OrmaDatabase? = null

    operator fun get(context: Context): OrmaDatabase {
        if (orma == null) {
            orma = OrmaDatabase.builder(context)
                    .name("saved_color.db")
                    .build()
        }
        return orma
    }
}
