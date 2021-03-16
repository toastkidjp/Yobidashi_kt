/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.planning_poker

import androidx.fragment.app.FragmentManager
import jp.toastkid.yobidashi.R

class CardFragmentAttachingUseCase(private val fragmentManager: FragmentManager) {

    operator fun invoke(text: String?) {
        if (text.isNullOrBlank()) {
            return
        }

        val transaction = fragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
        transaction.add(R.id.content, CardFragment.makeWithNumber(text))
        transaction.addToBackStack(CardFragment::class.java.canonicalName)
        transaction.commit()
    }

}