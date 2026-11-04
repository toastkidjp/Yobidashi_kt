/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.chat.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerativeAiModelTest {

    @Test
    fun image() {
        assertFalse(GenerativeAiModel.GEMINI_2_5_FLASH.image())

        assertTrue(
            GenerativeAiModel.entries
                .none(GenerativeAiModel::image)
        )
    }

}
