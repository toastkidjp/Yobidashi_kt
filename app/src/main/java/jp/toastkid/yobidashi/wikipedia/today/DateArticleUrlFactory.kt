/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.today

import android.content.Context
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.LocaleWrapper
import java.text.MessageFormat

/**
 * @author toastkidjp
 */
class DateArticleUrlFactory {

    /**
     * Format resource ID.
     */
    private val FORMAT_ID = R.string.format_date_link

    /**
     g* Make Wikipedia article's url.
     * @param context context
     * @param month 0-11
     * @param dayOfMonth 1-31
     * @return Wikipedia article's url
     */
    operator fun invoke(context: Context, month: Int, dayOfMonth: Int): String {
        if (month < 0 || month >= 12) {
            return ""
        }
        if (dayOfMonth <= 0 || dayOfMonth >= 31) {
            return ""
        }
        return if (LocaleWrapper.isJa(context.getResources().getConfiguration())) {
            MessageFormat.format(context.getString(FORMAT_ID), month + 1, dayOfMonth)
        } else MessageFormat.format(context.getString(FORMAT_ID), Month().get(month), dayOfMonth)
    }
}