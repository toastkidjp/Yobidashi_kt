/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup.playback.speed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * @author toastkidjp
 */
class PlaybackSpeedAdapter(
        private val layoutInflater: LayoutInflater,
        private val colorPair: ColorPair
) : BaseAdapter() {

    private val playingSpeeds = PlayingSpeed.values()

    override fun getCount(): Int = playingSpeeds.size

    override fun getItem(position: Int): PlayingSpeed = playingSpeeds[position]

    override fun getItemId(position: Int): Long = playingSpeeds[position].textId.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val playingSpeed = playingSpeeds[position]

        if (convertView == null) {
            val view = layoutInflater.inflate(
                    R.layout.item_playback_speed_spinner,
                    parent,
                    false
            )

            val viewHolder = ViewHolder(view.findViewById(R.id.text))
            view.tag = viewHolder
            viewHolder.bind(playingSpeed, colorPair)
            return view
        }

        val viewHolder = convertView.tag as? ViewHolder?
        viewHolder?.bind(playingSpeed, colorPair)
        return convertView
    }

}
