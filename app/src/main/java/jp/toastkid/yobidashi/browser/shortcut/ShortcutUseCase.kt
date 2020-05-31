/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.shortcut

import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.pm.ShortcutManagerCompat
import jp.toastkid.yobidashi.main.MainActivity


/**
 * @author toastkidjp
 */
class ShortcutUseCase(private val context: Context) {

    operator fun invoke(uri: Uri, title: String, bitmap: Bitmap?) {
        val shortcutIntent = Intent(Intent.ACTION_VIEW)
        shortcutIntent.setClass(context.applicationContext, MainActivity::class.java)
        shortcutIntent.data = uri

        val applicationContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val icon: Icon = Icon.createWithBitmap(bitmap)
            val shortcut = ShortcutInfo.Builder(applicationContext, title)
                    .setShortLabel(title)
                    .setLongLabel(title)
                    .setIcon(icon)
                    .setIntent(shortcutIntent)
                    .build()
            val shortcutManager = getSystemService(applicationContext, ShortcutManager::class.java)
            shortcutManager?.requestPinShortcut(shortcut, null)
        } else {
            val intent = Intent()
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            applicationContext.sendBroadcast(intent)
        }
    }
}