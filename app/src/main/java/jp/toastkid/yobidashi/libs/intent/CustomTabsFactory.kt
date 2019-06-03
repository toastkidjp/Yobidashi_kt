package jp.toastkid.yobidashi.libs.intent

import android.content.Context
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Custom tabs factory.
 *
 * @author toastkidjp
 */
object CustomTabsFactory {

    /**
     * Make [CustomTabsIntent].
     *
     * @param context
     * @param colorPair
     *
     * @return [CustomTabsIntent]
     */
    fun make(
            context: Context,
            colorPair: ColorPair
    ): CustomTabsIntent.Builder = make(context, colorPair.bgColor(), colorPair.fontColor())

    /**
     * Make [CustomTabsIntent].
     *
     * @param context
     * @param backgroundColor
     * @param fontColor
     *
     * @return [CustomTabsIntent]
     */
    fun make(
            context: Context,
            @ColorInt backgroundColor: Int,
            @ColorInt fontColor: Int
    ): CustomTabsIntent.Builder = CustomTabsIntent.Builder()
                    .setToolbarColor(backgroundColor)
                    .setShowTitle(true)
                    .setSecondaryToolbarColor(fontColor)
                    .setStartAnimations(context, 0, 0)
                    .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .addDefaultShareMenuItem()

}
