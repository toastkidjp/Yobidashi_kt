/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.ad

import android.content.Context
import com.google.android.gms.ads.AdView
import jp.toastkid.yobidashi.BuildConfig

/**
 * @author toastkidjp
 */
class AdViewFactory {

    operator fun invoke(context: Context): AdView {
        val adView = AdView(context)
        adView.adUnitId =
                if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111"
                else "ca-app-pub-5751262573448755/3489764085"
        return adView
    }
}