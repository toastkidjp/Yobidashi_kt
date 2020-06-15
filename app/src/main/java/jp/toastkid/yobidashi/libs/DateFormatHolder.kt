package jp.toastkid.yobidashi.libs

import android.content.Context
import jp.toastkid.yobidashi.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.WeakHashMap

/**
 * @author toastkidjp
 */
object DateFormatHolder {

    private val holder = WeakHashMap<Context, DateFormat>()

    operator fun invoke(context: Context): DateFormat? {
        if (holder.containsKey(context)) {
            return holder.get(context)
        }

        val newInstance = SimpleDateFormat(
                context.getString(R.string.date_format),
                Locale.getDefault()
        )
        holder.put(context, newInstance)
        return newInstance
    }

}