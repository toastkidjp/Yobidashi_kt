package jp.toastkid.jitte.libs.intent

import android.content.Intent
import android.provider.Settings

/**
 * Settings intent factory.

 * @author toastkidjp
 */
object SettingsIntentFactory {

    /**
     * Make launch settings intent.
     * @return [Intent]
     */
    fun makeLaunch(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_SETTINGS
        return intent
    }

    /**
     * Make launch Wi-Fi settings intent.
     * @return [Intent]
     */
    fun wifi(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_WIFI_SETTINGS
        return intent
    }

    /**
     * Make launch wireless settings intent.
     * @return [Intent]
     */
    fun wireless(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_WIRELESS_SETTINGS
        return intent
    }

    /**
     * Make launch all apps settings intent.
     * @return [Intent]
     */
    fun allApps(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS
        return intent
    }

    /**
     * Make launch Date and Time settings intent.
     * @return [Intent]
     */
    fun dateAndTime(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_DATE_SETTINGS
        return intent
    }
}
