/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi

import android.app.Application
import org.robolectric.TestLifecycleApplication
import java.lang.reflect.Method

/**
 * @author toastkidjp
 */
class TestApplication : Application(), TestLifecycleApplication {

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
    }

    override fun beforeTest(method: Method?) = Unit

    override fun prepareTest(test: Any?) = Unit

    override fun afterTest(method: Method?) = Unit
}