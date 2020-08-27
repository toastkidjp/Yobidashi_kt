/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main.launch

import android.content.Context
import android.content.Intent
import android.net.Uri
import jp.toastkid.yobidashi.main.MainActivity

/**
 * @author toastkidjp
 */
class MainActivityIntentFactory {

    /**
     * Make browser intent.
     *
     * @param context
     * @param uri
     *
     * @return [Intent]
     */
    fun browser(context: Context, uri: Uri) = Intent(context, MainActivity::class.java)
            .also {
                it.action = Intent.ACTION_VIEW
                it.data = uri
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

    fun barcodeReader(context: Context) = withAction(context, BARCODE_READER)

    fun launcher(context: Context) = withAction(context, APP_LAUNCHER)

    fun bookmark(context: Context) = withAction(context, BOOKMARK)

    fun search(context: Context) = withAction(context, SEARCH)

    fun setting(context: Context) = withAction(context, SETTING)

    private fun withAction(context: Context, action: String) =
            Intent(context, MainActivity::class.java)
                    .also {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.action = action
                    }

}