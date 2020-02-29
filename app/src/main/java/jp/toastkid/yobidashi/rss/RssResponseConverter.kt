/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

import jp.toastkid.yobidashi.rss.model.Parser
import jp.toastkid.yobidashi.rss.model.Rss
import okhttp3.ResponseBody
import retrofit2.Converter

/**
 * @author toastkidjp
 */
class RssResponseConverter : Converter<ResponseBody, Rss> {

    private val parser = Parser()

    override fun convert(responseBody: ResponseBody): Rss? {
        val bodyString = responseBody.string()
        return parser.parse(bodyString.split(findSplitter(bodyString)))
    }

    private fun findSplitter(body: String) =
            if (body.contains(LINE_SEPARATOR)) LINE_SEPARATOR else "\n"

    companion object {
        private val LINE_SEPARATOR = System.getProperty("line.separator")
    }
}