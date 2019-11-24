/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.notification.widget

import android.content.Context
import android.widget.RemoteViews
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.intent.PendingIntentFactory

/**
 * @author toastkidjp
 */
class TapActionInitializer {

    /**
     * Set pending intents.
     *
     * @param context
     * @param remoteViews
     */
    operator fun invoke(context: Context, remoteViews: RemoteViews) {
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