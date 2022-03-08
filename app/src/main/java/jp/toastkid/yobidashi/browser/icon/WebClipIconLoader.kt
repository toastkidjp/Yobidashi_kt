/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.icon

import android.content.Context
import android.graphics.BitmapFactory
import jp.toastkid.api.DownloadApi
import jp.toastkid.lib.Urls
import jp.toastkid.lib.image.BitmapCompressor
import jp.toastkid.lib.image.BitmapScaling
import jp.toastkid.yobidashi.browser.FaviconApplier
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

class WebClipIconLoader(
    private val faviconApplier: FaviconApplier,
    private val downloadApi: DownloadApi = DownloadApi(),
    private val bitmapScaling: BitmapScaling = BitmapScaling(),
    private val bitmapCompressor: BitmapCompressor = BitmapCompressor()
) {

    operator fun invoke(urlString: String) {
        if (Urls.isInvalidUrl(urlString)) {
            return
        }

        val file = faviconApplier.assignFile(urlString)
        if (file.exists() && file.lastModified() < TimeUnit.DAYS.toMillis(1)) {
            return
        }

        val url = URL(urlString)

        val webClipUrl = fetchDocument(url)?.getElementsByTag("link")
            ?.filter {
                it.getElementsByAttribute("rel").text() == "apple-touch-icon"
                        && it.hasAttr("href")
            }
            ?.map {
                val webClipUrl = it.getElementsByAttribute("href").text()

                return@map url.toURI().resolve(webClipUrl)
            }
            ?.firstOrNull()
            ?: return

        downloadApi.invoke(webClipUrl.toString(), object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Timber.e(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val longer = if (bitmap.width > bitmap.height) bitmap.width else bitmap.height
                    val sampling = 128.0 / longer.toDouble()
                    bitmapCompressor
                        .invoke(bitmapScaling.invoke(bitmap, sampling, sampling), file)
                }
            }

        })
    }

    private fun fetchDocument(url: URL) = try {
        Jsoup.parse(url, 3000)
    } catch (e: IOException) {
        Timber.w(e)
        null
    }

    companion object {
        fun from(context: Context) = WebClipIconLoader(FaviconApplier(context))
    }

}