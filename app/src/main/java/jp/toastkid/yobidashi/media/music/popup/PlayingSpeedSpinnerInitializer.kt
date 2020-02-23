/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class PlayingSpeedSpinnerInitializer {

    operator fun invoke(spinner: Spinner, category: PlayingSpeed = PlayingSpeed.getDefault()) {
        spinner.adapter = makeBaseAdapter(spinner.context)
        spinner.setSelection(category.findIndex() ?: 0)
    }

    private fun makeBaseAdapter(context: Context): BaseAdapter {
        val inflater = LayoutInflater.from(context)

        val playingSpeeds = PlayingSpeed.values()

        return object : BaseAdapter() {
            override fun getCount(): Int = playingSpeeds.size

            override fun getItem(position: Int): PlayingSpeed = playingSpeeds[position]

            override fun getItemId(position: Int): Long = playingSpeeds[position].textId.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val playingSpeed = playingSpeeds[position]

                val view = inflater.inflate(R.layout.item_playback_speed_spinner, parent, false)
                view.findViewById<TextView>(R.id.text).also {
                    it.setText(playingSpeed.textId)
                    val colorPair = PreferenceApplier(it.context).colorPair()
                    it.setBackgroundColor(colorPair.bgColor())
                    it.setTextColor(colorPair.fontColor())
                }
                return view
            }
        }
    }

}