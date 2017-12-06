package jp.toastkid.yobidashi.libs.clip

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context

import android.content.Context.CLIPBOARD_SERVICE

/**
 * Clipboard shortcut.
 *
 * @author toastkidjp
 */
object Clipboard {

    /**
     * Use for insert primary clip.
     */
    private val TEXT_MIME_TYPES = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

    /**
     * Use for insert primary clip.
     */
    private val TEXT_DATA = ClipDescription("text_data", TEXT_MIME_TYPES)

    /**
     * Insert passed text to primary clip.
     *
     * @param context
     * @param text
     */
    fun clip(context: Context, text: String) {
        val cm = clipboardManager(context)
        cm?.primaryClip = ClipData(TEXT_DATA, ClipData.Item(text))
    }

    /**
     * Get primary cliped item.
     *
     * @param context
     */
    fun getPrimary(context: Context): CharSequence? =
            clipboardManager(context)?.primaryClip?.getItemAt(0)?.text

    /**
     * Get clipboard.
     *
     * @param context
     */
    private fun clipboardManager(context: Context): ClipboardManager? =
            context.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

}
