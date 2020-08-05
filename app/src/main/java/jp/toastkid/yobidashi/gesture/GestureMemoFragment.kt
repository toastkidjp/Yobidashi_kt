/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.gesture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentGestureMemoBinding

/*+
 * @author toastkidjp
 */
class GestureMemoFragment : Fragment() {

    private lateinit var binding: FragmentGestureMemoBinding

    private val canvasStoreScenario = CanvasStoreScenario()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_gesture_memo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_canvas -> {
                binding.canvas.clear()
                true
            }
            R.id.save_canvas -> {
                val activity = activity ?: return true
                canvasStoreScenario.invoke(activity, binding.canvas)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_gesture_memo

    }
}