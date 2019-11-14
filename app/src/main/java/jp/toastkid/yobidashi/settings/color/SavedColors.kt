package jp.toastkid.yobidashi.settings.color

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber
import java.util.*

/**
 * @author toastkidjp
 */
object SavedColors {

    /**
     * Make saved color.
     *
     * @param bgColor
     * @param fontColor
     */
    internal fun makeSavedColor(
            @ColorInt bgColor: Int,
            @ColorInt fontColor: Int
    ) = SavedColor().also {
        it.bgColor = bgColor
        it.fontColor = fontColor
    }

    /**
     * Insert default colors.
     *
     * @param context
     */
    @SuppressLint("CheckResult")
    fun insertDefaultColors(context: Context) {
        Completable.fromAction {
            val repository = DatabaseFinder().invoke(context).savedColorRepository()
            DefaultColors.make(context).forEach { repository.add(it) }
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {},
                        Timber::e
                )
    }

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
                    .add(makeSavedColor(bg, font))
        }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

}
