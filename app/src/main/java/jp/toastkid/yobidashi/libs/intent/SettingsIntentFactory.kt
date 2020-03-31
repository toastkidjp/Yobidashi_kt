package jp.toastkid.yobidashi.libs.intent

import android.content.Intent
import android.provider.Settings

/**
 * Settings intent factory.

 * @author toastkidjp
 */
class SettingsIntentFactory {

    /**
     * Make launch settings intent.
     * @return [Intent]
     */
    fun makeLaunch() = Intent(Settings.ACTION_SETTINGS)

    /**
     * Make launch Wi-Fi settings intent.
     * @return [Intent]
     */
    fun wifi() = Intent(Settings.ACTION_WIFI_SETTINGS)

    /**
     * Make launch wireless settings intent.
     * @return [Intent]
     */
    fun wireless() = Intent(Settings.ACTION_WIRELESS_SETTINGS)

    /**
     * Make launch all apps settings intent.
     * @return [Intent]
     */
    fun allApps() = Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)

    /**
     * Make launch Date and Time settings intent.
     * @return [Intent]
     */
    fun dateAndTime() = Intent(Settings.ACTION_DATE_SETTINGS)

    /**
     * Make launch display settings intent.
     * @return [Intent]
     */
    fun display() = Intent(Settings.ACTION_DISPLAY_SETTINGS)
}
