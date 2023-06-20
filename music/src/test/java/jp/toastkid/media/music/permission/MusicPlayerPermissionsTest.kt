/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.music.permission

import android.Manifest
import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.util.ReflectionHelpers

class MusicPlayerPermissionsTest {

    @InjectMockKs
    private lateinit var musicPlayerPermissions: MusicPlayerPermissions

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.TIRAMISU)

        val permissions = musicPlayerPermissions.invoke()

        assertTrue(permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
        assertTrue(permissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    fun invokeUnderT() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.S)

        val permissions = musicPlayerPermissions.invoke()

        assertTrue(permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
        assertFalse(permissions.contains(Manifest.permission.POST_NOTIFICATIONS))
    }

}