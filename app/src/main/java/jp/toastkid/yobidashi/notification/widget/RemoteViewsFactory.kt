package jp.toastkid.yobidashi.notification.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
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
    private const val METHOD_NAME_SET_BACKGROUND_COLOR: String = "setBackgroundColor"

    /**
     * Layout ID.
     */
    @LayoutRes
    private const val APPWIDGET_LAYOUT_ID: Int = R.layout.notification_functions

    private val iconIds = arrayOf(
            R.id.text_random_wikipedia,
            R.id.text_bookmark,
            R.id.text_barcode_reader,
            R.id.text_search,
            R.id.text_browser,
            R.id.text_launcher,
            R.id.text_setting
    )

    private val dividerIds = arrayOf(
            R.id.divider1,
            R.id.divider2,
            R.id.divider3,
            R.id.divider4,
            R.id.divider5,
            R.id.divider6
    )

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
        IconInitializer(context).let {
            it(remoteViews, fontColor, R.drawable.ic_wikipedia_white, R.id.icon_random_wikipedia)
            it(remoteViews, fontColor, R.drawable.ic_bookmark, R.id.icon_bookmark)
            it(remoteViews, fontColor, R.drawable.ic_barcode, R.id.icon_barcode_reader)
            it(remoteViews, fontColor, R.drawable.ic_search_white, R.id.icon_search)
            it(remoteViews, fontColor, R.drawable.ic_web, R.id.icon_browser)
            it(remoteViews, fontColor, R.drawable.ic_launcher, R.id.icon_launcher)
            it(remoteViews, fontColor, R.drawable.ic_settings, R.id.icon_setting)
        }
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
        iconIds.forEach { remoteViews.setTextColor(it, fontColor) }
        dividerIds.forEach { remoteViews.setInt(it, METHOD_NAME_SET_BACKGROUND_COLOR, fontColor) }
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