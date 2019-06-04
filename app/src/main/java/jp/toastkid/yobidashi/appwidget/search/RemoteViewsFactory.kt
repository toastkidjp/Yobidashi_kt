package jp.toastkid.yobidashi.appwidget.search

import android.content.Context
import androidx.annotation.ColorInt
import android.widget.RemoteViews

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.intent.PendingIntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * App Widget's RemoteViews factory.
 *
 * @author toastkidjp
 */
internal object RemoteViewsFactory {

    /**
     * Method name.
     */
    private const val METHOD_NAME_SET_COLOR_FILTER = "setColorFilter"

    /**
     * Method name.
     */
    private const val METHOD_NAME_SET_BACKGROUND_COLOR = "setBackgroundColor"

    /**
     * Layout ID.
     */
    private const val APPWIDGET_LAYOUT_ID = R.layout.appwidget_layout

    /**
     * Make RemoteViews.
     *
     * @param context
     *
     * @return RemoteViews
     */
    fun make(context: Context): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, APPWIDGET_LAYOUT_ID)
        setTapActions(context, remoteViews)

        val preferenceApplier = PreferenceApplier(context)

        setBackgroundColor(remoteViews, preferenceApplier.color)

        setFontColor(remoteViews, preferenceApplier.fontColor)

        return remoteViews
    }

    /**
     * Set background color to remote views.
     *
     * @param remoteViews
     * @param backgroundColor
     */
    private fun setBackgroundColor(
            remoteViews: RemoteViews,
            @ColorInt backgroundColor: Int
    ) {
        remoteViews.setInt(
                R.id.widget_background, METHOD_NAME_SET_BACKGROUND_COLOR, backgroundColor)
    }

    /**
     * Set font color to remote views.
     *
     * @param remoteViews
     * @param fontColor
     */
    private fun setFontColor(
            remoteViews: RemoteViews,
            @ColorInt fontColor: Int
    ) {
        remoteViews.setInt(R.id.widget_search_border, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
        remoteViews.setInt(R.id.widget_search_image, METHOD_NAME_SET_COLOR_FILTER, fontColor)
        remoteViews.setInt(R.id.widget_launcher, METHOD_NAME_SET_COLOR_FILTER, fontColor)
        remoteViews.setInt(R.id.widget_barcode_reader, METHOD_NAME_SET_COLOR_FILTER, fontColor)

        remoteViews.setTextColor(R.id.widget_search_text, fontColor)
    }

    /**
     * Set pending intents.
     *
     * @param context
     * @param remoteViews
     */
    private fun setTapActions(context: Context, remoteViews: RemoteViews) {
        remoteViews.setOnClickPendingIntent(
                R.id.widget_search, PendingIntentFactory.makeSearchLauncher(context))
        remoteViews.setOnClickPendingIntent(
                R.id.widget_launcher, PendingIntentFactory.launcher(context))
        remoteViews.setOnClickPendingIntent(
                R.id.widget_barcode_reader, PendingIntentFactory.barcode(context))
    }

}