package jp.toastkid.setting.application.color

import android.annotation.SuppressLint
import android.content.Context
import androidx.collection.ArraySet
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.yobidashi.settings.color.SavedColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Default colors.
 *
 * @author toastkidjp
 */
class DefaultColorInsertion {

    private val map = mapOf(
        0xFF000044 to 0xDDFFC200,
        0xAAD50000 to 0xEEFDD835,
        0xDD4b1f12 to 0xDDf16971,
        0xAA4DD0E1 to 0xFFFFFFFF,
        0xDDffcdd2 to 0xFF000B00,
        0xDDFFF59D to 0xFF000B00,
        0xFF000B00 to 0xDDFFC200,
        0xDD9FA8DA to 0xFF000B00,
        0xFFFFFFFF to 0xFF000B00,
        0xDDFF80AB to 0xFFFFFFFF,
        0xCCCDDC39 to 0xFFFFFFFF,
        0xCC9C27B0 to 0xFFFFFFFF,
        0xBB388E3C to 0xCCFFFFFF,
        0xDD81D4FA to 0xFFFFFFFF,
        0xDD009688 to 0xFFFFFFFF,
        0xDD9E9E9E to 0xFFFFFFFF,
        0xDDFF5722 to 0xFFFFFFFF
    )

    /**
     * Insert default colors.
     *
     * @param context
     */
    @SuppressLint("CheckResult")
    fun insert(context: Context, ioDispatcher: CoroutineDispatcher = Dispatchers.IO): Job =
            CoroutineScope(ioDispatcher).launch {
                val repository = RepositoryFactory().savedColorRepository(context)
                make().forEach { repository.add(it) }
            }

    private fun make(): ArraySet<SavedColor> {
        val models = ArraySet<SavedColor>()
        map.map {
            SavedColor.make(
                Color(it.key).toArgb(),
                Color(it.value).toArgb()
            )
        }.forEach { models.add(it) }
        return models
    }

}
