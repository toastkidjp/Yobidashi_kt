/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import jp.toastkid.lib.tab.TabUiFragment
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class FragmentReplacingUseCase(private val supportFragmentManager: FragmentManager) {

    operator fun invoke(
            fragment: Fragment,
            withAnimation: Boolean = true,
            withSlideIn: Boolean = false
    ) {
        val currentFragment = findFragment()
        if (currentFragment == fragment) {
            return
        }

        val fragments = supportFragmentManager.fragments
        if (fragments.size != 0 && fragments.contains(fragment)) {
            fragments.remove(fragment)
        }

        val transaction = supportFragmentManager.beginTransaction()
        if (withAnimation) {
            transaction.setCustomAnimations(
                    if (withSlideIn) R.anim.slide_in_right else R.anim.slide_up,
                    0,
                    0,
                    if (withSlideIn) android.R.anim.slide_out_right else R.anim.slide_down
            )
        }

        transaction.replace(R.id.content, fragment, fragment::class.java.canonicalName)

        if (fragment !is TabUiFragment) {
            transaction.addToBackStack(fragment::class.java.canonicalName)
        }
        transaction.commitAllowingStateLoss()
    }

    private fun findFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.content)
    }

}