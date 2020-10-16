/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.note

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.ArticleInsertion
import jp.toastkid.article_viewer.databinding.PopupNoteBinding
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.SlidingTapListener
import java.util.Calendar

/**
 * Floating preview.
 *
 * @param context Use for obtaining [PopupWindow], [WebView], and so on...
 * @author toastkidjp
 */
class NotePopup(context: Context) {

    private val popupWindow = PopupWindow(context)

    private val resources = context.resources

    private val heightPixels = resources.displayMetrics.heightPixels

    private val headerHeight =
            resources.getDimensionPixelSize(R.dimen.note_popup_header_height)

    private val swipeLimit = heightPixels - (headerHeight / 2)

    private val insertion = ArticleInsertion(context)

    private val binding: PopupNoteBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, null, false)
        binding.popup = this

        popupWindow.contentView = binding.root

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        popupWindow.isClippingEnabled = false
        popupWindow.isOutsideTouchable = false

        // TODO popupWindow.animationStyle = R.style.PopupWindowVisibilityAnimation

        val preferenceApplier = PreferenceApplier(context)
        binding.store.setColorFilter(preferenceApplier.color)
        binding.close.setColorFilter(preferenceApplier.color)
        binding.inputContent.setBackgroundColor(preferenceApplier.editorBackgroundColor())
        binding.inputContent.setTextColor(preferenceApplier.editorFontColor())

        binding.inputTitle.setText("Note: ${Calendar.getInstance().time}")

        setSlidingListener()
    }

    fun store() {
        val title = binding.inputTitle.text?.toString() ?: return
        if (title.isEmpty()) {
            return
        }
        val contentText = binding.inputContent.text?.toString() ?: return

        insertion(title, contentText)

        Snackbar.make(binding.root, "Saved.", Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Invoke floating preview.
     *
     * @param parent Parent [View] for Snackbar
     */
    fun show(parent: View) {
        if (popupWindow.isShowing) {
            hide()
            return
        }
        popupWindow.showAtLocation(parent, Gravity.TOP, 0, 0)
        popupWindow.isFocusable = true
        popupWindow.update()
        binding.inputContent.requestFocus()
    }

    /**
     * Hide this preview.
     */
    fun hide() {
        popupWindow.isFocusable = false
        popupWindow.update()
        popupWindow.dismiss()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSlidingListener() {
        val slidingTouchListener = SlidingTapListener(binding.contentPanel)
        slidingTouchListener.setCallback(object : SlidingTapListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                if (y > swipeLimit) {
                    return
                }
                popupWindow.update(-1, -(y.toInt() - headerHeight), -1, -1)
            }
        })
        binding.header.setOnTouchListener(slidingTouchListener)
    }

    private fun isVisible() = popupWindow.isShowing

    // TODO call it.
    fun onBackPressed(): Boolean {
        if (popupWindow.isShowing.not()) {
            return false
        }
        if (isVisible()) {
            hide()
            return true
        }
        return false
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.popup_note

    }
}