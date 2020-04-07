/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup.playback.speed

import androidx.annotation.Keep
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
@Keep
enum class PlayingSpeed(@StringRes val textId: Int, val speed: Float) {
    S_0_1(R.string.title_playing_speed_0_5, 0.5f),
    S_0_7(R.string.title_playing_speed_0_7, 0.7f),
    S_0_8(R.string.title_playing_speed_0_8, 0.8f),
    S_0_9(R.string.title_playing_speed_0_9, 0.9f),
    NORMAL(R.string.title_playing_speed_1, 1f),
    S_1_1(R.string.title_playing_speed_1_1, 1.1f),
    S_1_2(R.string.title_playing_speed_1_2, 1.2f),
    S_1_5(R.string.title_playing_speed_1_5, 1.5f),
    S_2(R.string.title_playing_speed_2, 2f),
    S_3(R.string.title_playing_speed_3, 3f),
    S_5(R.string.title_playing_speed_5, 5f);

    fun findIndex(): Int {
        return ordinal
    }

    companion object {
        fun findById(id: Long): PlayingSpeed {
            return values().firstOrNull { it.textId.toLong() == id } ?: NORMAL
        }

        fun getDefault() = NORMAL
    }
}