/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * @author toastkidjp
 */
class RssConverterFactory private constructor(): Converter.Factory() {

    override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation>?,
            retrofit: Retrofit?
    ): Converter<ResponseBody, *> = RssResponseConverter()

    companion object {

        fun create() = RssConverterFactory()
    }
}