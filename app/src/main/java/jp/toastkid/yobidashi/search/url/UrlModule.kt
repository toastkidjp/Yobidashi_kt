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
import jp.toastkid.yobidashi.databinding.ModuleSearchUrlBinding
import jp.toastkid.yobidashi.libs.clip.Clipboard
import timber.log.Timber

/**
 * @author toastkidjp
 */
class UrlModule(private val binding: ModuleSearchUrlBinding) {

    private var enable: Boolean = true

    init {
        binding.share.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            share.putExtra(Intent.EXTRA_SUBJECT, "Share link")
            val currentText = getCurrentText()
            share.putExtra(Intent.EXTRA_TEXT, currentText)

            val makeShare = Intent.createChooser(share, "Share link $currentText")
            it.context.startActivity(makeShare)
        }

        binding.clip.setOnClickListener {
            Clipboard.clip(it.context, getCurrentText())
            // TODO Add snackbar feedback.
        }
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