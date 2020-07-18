/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.ad

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import jp.toastkid.yobidashi.main.AppBarViewModel

/**
 * @author toastkidjp
 */
class AdService(contextSupplier: () -> Context) {

    private var adView = AdViewFactory()(contextSupplier())

    init {
        adView.adSize = AdSize.LARGE_BANNER
    }

    fun load() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    fun sendWith(viewModel: AppBarViewModel) {
        viewModel.replace(adView)
    }

    fun destroy() {
        adView.destroy()
    }
}