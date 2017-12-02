package jp.toastkid.yobidashi.home

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
internal enum class Menu private constructor(@param:StringRes val titleId: Int, @param:DrawableRes val iconId: Int) {

    CODE_READER(R.string.title_code_reader, R.drawable.ic_barcode),

    LAUNCHER(R.string.title_apps_launcher, R.drawable.ic_launcher),

    BROWSER(R.string.title_browser, R.drawable.ic_web),

    PLANNING_POKER(R.string.title_planning_poker, R.drawable.ic_card),

    SETTING(R.string.title_settings, R.drawable.ic_settings),

    COLOR_SETTING(R.string.title_settings_color, R.drawable.ic_palette),

    BACKGROUND_SETTING(R.string.title_background_image_setting, R.drawable.ic_photo),

    WIFI_SETTING(R.string.title_settings_wifi, R.drawable.ic_wifi),

    EXIT(R.string.exit, R.drawable.ic_exit)

}
