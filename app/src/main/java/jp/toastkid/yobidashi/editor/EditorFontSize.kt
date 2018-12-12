/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

/**
 * Editor's font size definition.
 *
 * @author toastkidjp
 */
enum class EditorFontSize(val size: Int) {
    FONT_8(8),
    FONT_9(9),
    FONT_10(10),
    FONT_11(11),
    FONT_12(12),
    FONT_14(14),
    FONT_16(16),
    FONT_18(18),
    FONT_20(20),
    FONT_24(24),
    FONT_28(28);

    companion object {

        /**
         * Find index by size.
         *
         * @param size size
         */
        fun findIndex(size: Int) = values().find { it.size == size }?.ordinal ?: 6
    }
}