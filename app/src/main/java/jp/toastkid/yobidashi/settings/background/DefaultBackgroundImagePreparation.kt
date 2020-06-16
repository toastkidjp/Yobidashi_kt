package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.content.res.Resources
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
import kotlinx.coroutines.withContext
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
            CoroutineScope(Dispatchers.Default).launch {
                val filesDir = FilesDir(context, DisplayingSettingFragment.getBackgroundDirectory())

                val defaultFile = filesDir.assignNewFile("rose")

                copyImageToFilesDir(filesDir, context.resources, "rose", R.mipmap.rose)
                copyImageToFilesDir(filesDir, context.resources, "night_of_tokyo", R.mipmap.night_of_tokyo)

                PreferenceApplier(context).backgroundImagePath = defaultFile.absolutePath
            }

    private suspend fun copyImageToFilesDir(
            filesDir: FilesDir,
            resources: Resources?,
            fileName: String,
            imageResource: Int
    ) {
        withContext(Dispatchers.IO) {
            FileOutputStream(filesDir.assignNewFile(fileName)).use {
                BitmapFactory.decodeResource(resources, imageResource)
                        .compress(Bitmap.CompressFormat.WEBP, 100, it)
            }
        }
    }

}