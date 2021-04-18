/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.intent

import org.junit.Before
import org.junit.Test

class SettingsIntentFactoryTest {

    private lateinit var settingsIntentFactory: SettingsIntentFactory

    @Before
    fun setUp() {
        settingsIntentFactory = SettingsIntentFactory()
    }

    @Test
    fun testMakeLaunch() {
        settingsIntentFactory.makeLaunch()
    }

    @Test
    fun wifi() {
        settingsIntentFactory.wifi()
    }

    @Test
    fun wireless() {
        settingsIntentFactory.wireless()
    }

    @Test
    fun allApps() {
        settingsIntentFactory.allApps()
    }

    @Test
    fun dateAndTime() {
        settingsIntentFactory.dateAndTime()
    }

    @Test
    fun display() {
        settingsIntentFactory.display()
    }

}