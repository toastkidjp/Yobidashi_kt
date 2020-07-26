/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup.playback.speed

import android.widget.TextView
import jp.toastkid.lib.preference.ColorPair

/**
 * @author toastkidjp
 */
class ViewHolder(private val text: TextView) {

    fun bind(playingSpeed: PlayingSpeed, colorPair: ColorPair) {
        text.setText(playingSpeed.textId)
        colorPair.setTo(text)
    }
}