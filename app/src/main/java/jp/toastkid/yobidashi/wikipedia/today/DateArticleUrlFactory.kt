/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.today

import android.content.Context
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.LocaleWrapper
import java.text.MessageFormat

/**
 * @author toastkidjp
 */
class DateArticleUrlFactory {

    /**
     * Make Wikipedia article's url.
     *
     * @param context Use for extracting current configuration and title template
     * @param month <b>0</b>-11
     * @param dayOfMonth 1-31
     *
     * @return Wikipedia article's url
     */
    operator fun invoke(context: Context, month: Int, dayOfMonth: Int): String {
        if (month < 0 || month >= 12) {
            return ""
        }
        if (dayOfMonth <= 0 || dayOfMonth >= 31) {
            return ""
        }

        val monthString =
                if (LOCALE_WRAPPER.isJa(context.resources.configuration)) "${month + 1}"
                else Month().get(month)

        return MessageFormat.format(context.getString(FORMAT_ID), monthString, dayOfMonth)
    }

    companion object {

        private val LOCALE_WRAPPER = LocaleWrapper()

        /**
         * Format resource ID.
         */
        @StringRes
        private const val FORMAT_ID = R.string.format_date_link

    }
}