/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.setting.application.intent

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
