/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.url

import android.content.Intent
import android.view.View
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchUrlBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * @author toastkidjp
 */
class UrlModule(private val binding: ModuleSearchUrlBinding) {

    private val preferenceApplier = PreferenceApplier(binding.root.context)

    private var enable: Boolean = true

    init {
        binding.module = this
    }

    fun clipUrl(view: View) {
        val text = getCurrentText()
        val context = view.context
        Clipboard.clip(context, text)
        Toaster.snackShort(
                view,
                context.getString(R.string.message_clip_to, text),
                preferenceApplier.colorPair()
        )
    }

    fun shareUrl(view: View) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        share.putExtra(Intent.EXTRA_SUBJECT, "Share link")
        val currentText = getCurrentText()
        share.putExtra(Intent.EXTRA_TEXT, currentText)

        val makeShare = Intent.createChooser(share, "Share link $currentText")
        view.context.startActivity(makeShare)
    }

    /**
     * Show this module.
     */
    fun show(): Disposable {
        if (binding.root.visibility == View.GONE && enable) {
            return runOnMainThread { binding.root.visibility = View.VISIBLE }
        }
        return Disposables.disposed()
    }

    /**
     * Hide this module.
     */
    fun hide(): Disposable {
        if (binding.root.visibility == View.VISIBLE) {
            return runOnMainThread { binding.root.visibility = View.GONE }
        }
        return Disposables.disposed()
    }

    /**
     * Is visible this module visible.
     */
    fun isVisible() = binding.root.visibility == View.VISIBLE

    private fun getCurrentText() = binding.text.text.toString()

    /**
     * Set open link and icon.
     *
     * @param link Link URL(string)
     */
    fun setLink(link: String) {
        binding.text.text = link
    }

    private fun runOnMainThread(action: () -> Unit) =
            Completable.fromAction { action() }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            Timber::e
                    )
}