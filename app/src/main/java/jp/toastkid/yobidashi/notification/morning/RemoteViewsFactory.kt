/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.notification.morning

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import timber.log.Timber
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author toastkidjp
 */
class RemoteViewsFactory {

    private val dateArticleUrlFactory = DateArticleUrlFactory()

    /**
     * Make RemoteViews.
     *
     * @param context
     * @return RemoteViews
     */
    operator fun invoke(context: Context): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, APPWIDGET_LAYOUT_ID)

        val calendar = Calendar.getInstance()
        remoteViews.setOnClickPendingIntent(
                R.id.what_happened,
                PendingIntent.getActivity(
                        context,
                        0,
                        MainActivity.makeBrowserIntent(
                                context,
                                dateArticleUrlFactory(
                                        context,
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                ).toUri()
                        ),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        )

        val countDownLatch = CountDownLatch(1)
        RandomWikipedia().fetchWithAction { title, uri ->
            Timber.i("random $title, $uri")
            remoteViews.setTextViewText(R.id.today_wikipedia1, "Today's Wikipedia article - '$title'")
            remoteViews.setOnClickPendingIntent(
                    R.id.today_wikipedia1,
                    PendingIntent.getActivity(
                            context,
                            1,
                            MainActivity.makeBrowserIntent(context, uri),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
            )
            countDownLatch.countDown()
        }

        countDownLatch.await(30, TimeUnit.SECONDS)

        return remoteViews
    }

    companion object {

        @LayoutRes
        private const val APPWIDGET_LAYOUT_ID = R.layout.notification_morning

    }
}