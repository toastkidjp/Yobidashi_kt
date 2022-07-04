/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.annotation.TargetApi
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.webkit.WebViewFeature
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.suggestion.RssAddingSuggestion
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.history.ViewHistoryInsertion
import jp.toastkid.yobidashi.browser.tls.TlsErrorMessageGenerator
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.usecase.RedirectionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WebViewClientFactory(
    private val contentViewModel: ContentViewModel?,
    private val adRemover: AdRemover,
    private val faviconApplier: FaviconApplier,
    private val preferenceApplier: PreferenceApplier,
    private val browserViewModel: BrowserViewModel? = null,
    private val rssAddingSuggestion: RssAddingSuggestion? = null,
    private val currentView: () -> WebView? = { null }
) {

    /**
     * Add onPageFinished and onPageStarted.
     */
    operator fun invoke(): WebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            if (view == currentView()) {
                browserViewModel?.updateProgress(0)
                browserViewModel?.nextUrl(url)
            }

            rssAddingSuggestion?.invoke(view, url)
            browserViewModel?.setBackButtonIsEnabled(view.canGoBack())
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)

            val title = view.title ?: ""
            val urlStr = url ?: ""

            val tabId = GlobalWebViewPool.getTabId(view)
            if (tabId?.isNotBlank() == true) {
                CoroutineScope(Dispatchers.Main).launch {
                    browserViewModel?.finished(tabId, title, urlStr)
                }
            }

            browserViewModel?.updateProgress(100)
            browserViewModel?.stopProgress(true)

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
            browserViewModel?.stopProgress(true)
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

            ViewModelProvider(context).get(BrowserViewModel::class.java)
                .setError(TlsErrorMessageGenerator().invoke(context, error))
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(request.url.toString())
                } else {
                    super.shouldInterceptRequest(view, request)
                }

        @Suppress("OverridingDeprecatedMember")
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(url)
                } else {
                    @Suppress("DEPRECATION")
                    super.shouldInterceptRequest(view, url)
                }

        @Suppress("DEPRECATION")
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                shouldOverrideUrlLoading(view, request?.url?.toString())

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                url?.let {
                    val context: Context? = view?.context
                    val uri: Uri = Uri.parse(url)

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
                            super.shouldOverrideUrlLoading(view, url)
                        }
                    }
                } ?: super.shouldOverrideUrlLoading(view, url)

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
            true
        } catch (e: ActivityNotFoundException) {
            Timber.w(e)

            context?.let {
                contentViewModel?.snackShort(context.getString(R.string.message_cannot_launch_app))
            }
            true
        }
}