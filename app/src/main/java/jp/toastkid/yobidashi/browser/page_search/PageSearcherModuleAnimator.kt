/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.view.View
import android.widget.EditText
import androidx.annotation.UiThread
import jp.toastkid.lib.input.Inputs

class PageSearcherModuleAnimator {

    @UiThread
    fun show(view: View?, editText: EditText) {
        view?.animate()?.let {
            it.cancel()
            it.translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .withStartAction { switchVisibility(view, View.GONE, View.VISIBLE) }
                .withEndAction {
                    editText.requestFocus()
                    (view.context as? Activity)?.also { activity ->
                        Inputs.showKeyboard(activity, editText)
                    }
                }
                .start()
        }
    }

    @UiThread
    fun hide(view: View?, editText: EditText?, height: Float) {
        view?.animate()?.let {
            it.cancel()
            it.translationY(-height)
                .setDuration(ANIMATION_DURATION)
                .withStartAction {
                    editText?.setText("")
                    Inputs.hideKeyboard(editText)
                }
                .withEndAction { switchVisibility(view, View.VISIBLE, View.GONE) }
                .start()
        }
    }

    private fun switchVisibility(view: View?, from: Int, to: Int) {
        if (view?.visibility == from) {
            view.visibility = to
        }
    }

    companion object {

        /**
         * Animation duration (ms).
         */
        private const val ANIMATION_DURATION = 250L
    }

}