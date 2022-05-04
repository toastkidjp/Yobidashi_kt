package jp.toastkid.yobidashi.libs

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Simple toasting utilities.
 * TODO Delete it.
 * @author toastkidjp
 */
object Toaster {

    /**
     * Short toasting.
     *
     * @param context
     * @param messageId
     */
    fun tShort(context: Context, @StringRes messageId: Int) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
    }

    /**
     * Short toasting.
     *
     * @param context
     * @param message
     */
    fun tShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}