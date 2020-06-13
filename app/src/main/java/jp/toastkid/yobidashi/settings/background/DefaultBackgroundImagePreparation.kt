package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    operator fun invoke(context: Context): Job =
            CoroutineScope(Dispatchers.IO).launch {
                val filesDir = FilesDir(context, DisplayingSettingFragment.getBackgroundDirectory())

                val defaultFile = filesDir.assignNewFile("sakura")
                FileOutputStream(defaultFile).use {
                    BitmapFactory.decodeResource(context.resources, R.mipmap.sakura)
                            .compress(Bitmap.CompressFormat.WEBP, 100, it)
                }

                FileOutputStream(filesDir.assignNewFile("toast")).use {
                    BitmapFactory.decodeResource(context.resources, R.mipmap.toast)
                            .compress(Bitmap.CompressFormat.WEBP, 100, it)
                }

                FileOutputStream(filesDir.assignNewFile("rose")).use {
                    BitmapFactory.decodeResource(context.resources, R.mipmap.rose)
                            .compress(Bitmap.CompressFormat.WEBP, 100, it)
                }

                FileOutputStream(filesDir.assignNewFile("night_of_tokyo")).use {
                    BitmapFactory.decodeResource(context.resources, R.mipmap.night_of_tokyo)
                            .compress(Bitmap.CompressFormat.WEBP, 100, it)
                }

                PreferenceApplier(context).backgroundImagePath = defaultFile.absolutePath
            }
}