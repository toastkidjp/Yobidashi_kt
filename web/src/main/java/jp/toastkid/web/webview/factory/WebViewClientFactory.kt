/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.factory

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelStoreOwner
import androidx.webkit.WebViewFeature
import jp.toastkid.web.view.WebTabUiViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.web.FaviconApplier
import jp.toastkid.web.block.AdRemover
import jp.toastkid.web.block.SiteNameChecker
import jp.toastkid.web.history.ViewHistoryInsertion
import jp.toastkid.web.rss.suggestion.RssAddingSuggestion
import jp.toastkid.web.tls.TlsErrorMessageGenerator
import jp.toastkid.web.webview.CustomWebView
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.web.webview.usecase.ApproachFallbackUrlExtractor
import jp.toastkid.web.webview.usecase.RedirectionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WebViewClientFactory(
    private val contentViewModel: ContentViewModel?,
    private val adRemover: AdRemover,
    private val faviconApplier: FaviconApplier,
    private val preferenceApplier: PreferenceApplier,
    private val browserViewModel: WebTabUiViewModel? = null,
    private val rssAddingSuggestion: RssAddingSuggestion? = null,
    private val currentView: () -> WebView? = { null },
    private val siteNameChecker: SiteNameChecker = SiteNameChecker()
) {

    /**
     * Add onPageFinished and onPageStarted.
     */
    operator fun invoke(): WebViewClient = object : WebViewClient() {

        private var lastStartedMs = false

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            (view as? CustomWebView)?.enablePullToRefresh = false

            lastStartedMs = true

            if (view == currentView()) {
                browserViewModel?.updateProgress(0)
                browserViewModel?.nextUrl(url)
                browserViewModel?.resetIcon()

                CoroutineScope(Dispatchers.IO).launch {
                    faviconApplier.load(url.toUri())?.let {
                        browserViewModel?.newIcon(it)
                    }
                }
            }

            rssAddingSuggestion?.invoke(view, url)
            browserViewModel?.setBackButtonIsEnabled(view.canGoBack())
            browserViewModel?.setForwardButtonIsEnabled(view.canGoForward())
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)

            if (!lastStartedMs) {
                return
            }
            lastStartedMs = false

            val title = view.title ?: ""
            val urlStr = url ?: ""

            val tabId = GlobalWebViewPool.getTabId(view)
            if (tabId?.isNotBlank() == true) {
                CoroutineScope(Dispatchers.Main).launch {
                    contentViewModel?.finished(tabId, title, urlStr)
                }
            }

            browserViewModel?.updateProgress(100)

            try {
                if (view == currentView()) {
                    browserViewModel?.nextTitle(title)
                    browserViewModel?.nextUrl(urlStr)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            if (preferenceApplier.saveViewHistory
                    && title.isNotEmpty()
                    && urlStr.isNotEmpty()
            ) {
                ViewHistoryInsertion
                        .make(
                                view.context,
                                title,
                                urlStr,
                                faviconApplier.makePath(urlStr)
                        )
                        .invoke()
            }
        }

        override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)

            browserViewModel?.updateProgress(100)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)

            handler?.cancel()

            val context = view?.context ?: return
            if (context !is ComponentActivity
                || context !is ViewModelStoreOwner
                || context.isFinishing) {
                return
            }

            browserViewModel?.setError(TlsErrorMessageGenerator().invoke(context, error))
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(request.url.toString())
                } else {
                    super.shouldInterceptRequest(view, request)
                }

        private val approachFallbackUrlExtractor = ApproachFallbackUrlExtractor()

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
            request?.url?.toString()?.let { url ->
                    val context: Context? = view?.context
                    val uri: Uri = Uri.parse(url)

                if (siteNameChecker(uri.host)) {
                    view?.stopLoading()
                    contentViewModel?.snackShort("It has canceled load inappropriate Web site. : $uri")
                    return@let true
                }

                if (approachFallbackUrlExtractor.isTarget(uri.host)) {
                    approachFallbackUrlExtractor.invoke(uri) {
                        view?.stopLoading()
                        view?.loadUrl(it)
                    }
                    return@let false
                }

                    if (RedirectionUseCase.isTarget(uri)) {
                        RedirectionUseCase().invoke(view, uri)
                        return@let false
                    }

                    when (uri.scheme) {
                        "market", "intent" -> {
                            startOtherAppWithIntent(
                                context,
                                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            )
                            true
                        }
                        "tel" -> {
                            startOtherAppWithIntent(
                                context,
                                Intent(Intent.ACTION_DIAL, uri)
                            )
                            view?.reload()
                            true
                        }
                        "mailto" -> {
                            startOtherAppWithIntent(
                                context,
                                Intent(Intent.ACTION_SENDTO, uri)
                            )
                            view?.reload()
                            true
                        }
                        else -> {
                            super.shouldOverrideUrlLoading(view, request)
                        }
                    }
                } ?: super.shouldOverrideUrlLoading(view, request)

        override fun onSafeBrowsingHit(
            view: WebView?,
            request: WebResourceRequest?,
            threatType: Int,
            callback: SafeBrowsingResponse?
        ) {
            // The "true" argument indicates that your app reports incidents like
            // this one to Safe Browsing.
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY)) {
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                callback?.backToSafety(true)
            } else {
                view?.goBack()
            }

            contentViewModel?.snackShort("Unsafe web page blocked.")
        }

    }

    private fun startOtherAppWithIntent(context: Context?, intent: Intent?) =
        try {
            context?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.w(e)

            contentViewModel?.snackShort(jp.toastkid.lib.R.string.message_cannot_launch_app)
        }

    companion object {
        fun forBackground(
            context: Context,
            contentViewModel: ContentViewModel,
            preferenceApplier: PreferenceApplier
        ) = WebViewClientFactory(
            contentViewModel,
            AdRemover.make(context.assets),
            FaviconApplier(context),
            preferenceApplier
        )
    }
}