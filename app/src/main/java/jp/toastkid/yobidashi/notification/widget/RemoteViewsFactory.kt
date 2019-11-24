package jp.toastkid.yobidashi.notification.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
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
    private const val METHOD_NAME_SET_COLOR_FILTER: String = "setColorFilter"

    /**
     * Method name.
     */
    private const val METHOD_NAME_SET_BACKGROUND_COLOR: String = "setBackgroundColor"

    /**
     * Layout ID.
     */
    @LayoutRes
    private const val APPWIDGET_LAYOUT_ID: Int = R.layout.notification_functions

    /**
     * Make RemoteViews.
     *
     * @param context
     * @return RemoteViews
     */
    fun make(context: Context): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, APPWIDGET_LAYOUT_ID)
        setTapActions(context, remoteViews)

        val preferenceApplier = PreferenceApplier(context)

        setBackgroundColor(remoteViews, preferenceApplier.color)

        setIcons(remoteViews, context, preferenceApplier.fontColor)

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
    ) = remoteViews.setInt(R.id.background, METHOD_NAME_SET_BACKGROUND_COLOR, backgroundColor)

    private fun setIcons(remoteViews: RemoteViews, context: Context, fontColor: Int) {
        setIcon(context, remoteViews, fontColor, R.drawable.ic_wikipedia_white, R.id.icon_random_wikipedia)
        setIcon(context, remoteViews, fontColor, R.drawable.ic_bookmark, R.id.icon_bookmark)
        setIcon(context, remoteViews, fontColor, R.drawable.ic_barcode, R.id.icon_barcode_reader)
        setIcon(context, remoteViews, fontColor, R.drawable.ic_search_white, R.id.icon_search)
        setIcon(context, remoteViews, fontColor, R.drawable.ic_web, R.id.icon_browser)
        setIcon(context, remoteViews, fontColor, R.drawable.ic_launcher, R.id.icon_launcher)
        setIcon(context, remoteViews, fontColor, R.drawable.ic_settings, R.id.icon_setting)
    }

    /**
     * TODO: refactor with function object pattern.
     */
    private fun setIcon(context: Context, remoteViews: RemoteViews, fontColor: Int, iconResourceId: Int, viewId: Int) {
        val bitmap = vectorToBitmap(context, iconResourceId)
        remoteViews.setImageViewBitmap(viewId, bitmap)
        remoteViews.setInt(viewId, METHOD_NAME_SET_COLOR_FILTER, fontColor)
    }

    private fun vectorToBitmap(context: Context, @DrawableRes resVector: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, resVector)
        val b = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight())
        drawable.draw(c)
        return b
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
        remoteViews.setTextColor(R.id.text_random_wikipedia, fontColor)
        remoteViews.setTextColor(R.id.text_bookmark, fontColor)
        remoteViews.setTextColor(R.id.text_barcode_reader, fontColor)
        remoteViews.setTextColor(R.id.text_search, fontColor)
        remoteViews.setTextColor(R.id.text_browser, fontColor)
        remoteViews.setTextColor(R.id.text_launcher, fontColor)
        remoteViews.setTextColor(R.id.text_setting, fontColor)

        remoteViews.setInt(R.id.divider1, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
        remoteViews.setInt(R.id.divider2, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
        remoteViews.setInt(R.id.divider3, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
        remoteViews.setInt(R.id.divider4, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
        remoteViews.setInt(R.id.divider5, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
        remoteViews.setInt(R.id.divider6, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor)
    }

    /**
     * Set pending intents.
     *
     * @param context
     * @param remoteViews
     */
    private fun setTapActions(context: Context, remoteViews: RemoteViews) {
        remoteViews.setOnClickPendingIntent(
                R.id.random_wikipedia, PendingIntentFactory.randomWikipedia(context))
        remoteViews.setOnClickPendingIntent(
                R.id.search, PendingIntentFactory.makeSearchLauncher(context))
        remoteViews.setOnClickPendingIntent(
                R.id.bookmark, PendingIntentFactory.bookmark(context))
        remoteViews.setOnClickPendingIntent(
                R.id.browser, PendingIntentFactory.browser(context))
        remoteViews.setOnClickPendingIntent(
                R.id.launcher, PendingIntentFactory.launcher(context))
        remoteViews.setOnClickPendingIntent(
                R.id.barcode_reader, PendingIntentFactory.barcode(context))
        remoteViews.setOnClickPendingIntent(
                R.id.setting, PendingIntentFactory.setting(context))
    }

}