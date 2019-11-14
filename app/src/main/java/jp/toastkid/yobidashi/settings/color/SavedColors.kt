package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.graphics.Color
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import java.util.*

/**
 * @author toastkidjp
 */
object SavedColors {

    /**
     * Insert random colors.
     *
     * @param context
     */
    fun insertRandomColors(context: Context): Disposable {

        val random = Random()

        val bg = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )

        val font = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )

        return Completable.fromAction {
            DatabaseFinder().invoke(context)
                    .savedColorRepository()
                    .add(SavedColor.make(bg, font))
        }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

}
