/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.gesture

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppBarGestureMemoBinding
import jp.toastkid.yobidashi.databinding.FragmentGestureMemoBinding
import jp.toastkid.lib.AppBarViewModel

/*+
 * @author toastkidjp
 */
class GestureMemoFragment : Fragment() {

    private lateinit var binding: FragmentGestureMemoBinding

    private lateinit var appBarBinding: AppBarGestureMemoBinding

    private val canvasStoreScenario = CanvasStoreScenario()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_gesture_memo, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appBarBinding.touchListenerChoices.setOnCheckedChangeListener { _, checkedId ->
            binding.canvas.isTouchEventListenerEnabled = false
            binding.canvas.clear()
            when (checkedId) {
                R.id.defaultTouch -> setUpDefaultTouchListener()
                R.id.gestureDetector -> setUpGestureDetector()
            }
        }
    }

    private fun setUpDefaultTouchListener() {
        binding.canvas.setOnTouchListener(null)
        binding.canvas.isTouchEventListenerEnabled = true
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun setUpGestureDetector() {
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onShowPress(event: MotionEvent?) {
            }

            override fun onSingleTapUp(event: MotionEvent?): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                return true
            }

            override fun onDown(event: MotionEvent?): Boolean {
                return true
            }

            override fun onFling(event1: MotionEvent?, event2: MotionEvent?, velocityX: Float,
                                 velocityY: Float): Boolean {
                 return true
            }

            override fun onScroll(event1: MotionEvent?, event2: MotionEvent?, distanceX: Float,
                                  distanceY: Float): Boolean {
                return true
            }

            override fun onLongPress(event: MotionEvent?) {
            }

            override fun onDoubleTap(event: MotionEvent?): Boolean {
                return true
            }

            override fun onDoubleTapEvent(event: MotionEvent?): Boolean {
                return true
            }

            override fun onContextClick(event: MotionEvent?): Boolean {
                return true
            }
        }

        val gestureDetector = GestureDetector(requireContext(), gestureListener)

        binding.canvas.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()
        ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java)
                .replace(appBarBinding.root)
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