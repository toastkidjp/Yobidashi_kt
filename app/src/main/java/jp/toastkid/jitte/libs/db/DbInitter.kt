package jp.toastkid.jitte.libs.db

import android.content.Context
import jp.toastkid.jitte.browser.bookmark.model.OrmaDatabase

/**
 * @author toastkidjp
 */
object DbInitter {

    private var orma: OrmaDatabase? = null

    operator fun get(context: Context): OrmaDatabase {
        if (orma == null) {
            orma = OrmaDatabase.builder(context).name("jitte.db").build()
        }
        return orma as OrmaDatabase
    }
}