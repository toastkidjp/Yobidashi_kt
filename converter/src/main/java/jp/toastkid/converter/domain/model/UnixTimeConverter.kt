/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.domain.model

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class UnixTimeConverter : TwoStringConverter {

    private val dateFormatter = "yyyy-MM-dd HH:mm:ss"

    private val simpleDateFormat = SimpleDateFormat(dateFormatter, Locale.getDefault())

    override fun title(): String {
        return "Unix time Converter"
    }

    override fun firstInputLabel(): String {
        return "Unix time"
    }

    override fun secondInputLabel(): String {
        return "Date time"
    }

    override fun defaultFirstInputValue(): String =
        System.currentTimeMillis().toString()

    override fun defaultSecondInputValue(): String =
        DateFormat.format(dateFormatter, System.currentTimeMillis()).toString()

    override fun firstInputAction(input: String): String? {
        return try {
            val toLong = input.toLongOrNull() ?: return null
            DateFormat.format(dateFormatter, toLong).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun secondInputAction(input: String): String? {
        return try {
            simpleDateFormat.parse(input)?.time?.toString()
        } catch (e: Exception) {
            // > /dev/null
            null
        }
    }

}