package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import java.io.FileOutputStream

/**
 * Default background image preparation.
 *
 * @author toastkidjp
 */
class DefaultBackgroundImagePreparation {

    /**
     * Invoke this class action.
     *
     * @param context [Context]
     */
    operator fun invoke(context: Context): Disposable = Completable.fromAction {
            val filesDir = FilesDir(context, BackgroundSettingActivity.BACKGROUND_DIR)
            val defaultFile = filesDir.assignNewFile("sakura")
            BitmapFactory.decodeResource(context.resources, R.mipmap.sakura)
                    .compress(Bitmap.CompressFormat.PNG, 100,
                            FileOutputStream(defaultFile))
            BitmapFactory.decodeResource(context.resources, R.mipmap.toast)
                    .compress(Bitmap.CompressFormat.PNG, 100,
                            FileOutputStream(filesDir.assignNewFile("toast")))
            PreferenceApplier(context).backgroundImagePath = defaultFile.absolutePath
        }.subscribeOn(Schedulers.io())
                .subscribe()
}