/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.url

import android.view.View
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchUrlBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * @author toastkidjp
 */
class UrlModule(
        private val binding: ModuleSearchUrlBinding,
        private val insert: (String) -> Unit
) {

    private val preferenceApplier = PreferenceApplier(binding.root.context)

    var enable: Boolean = true

    init {
        binding.module = this
    }

    /**
     * This function is called from data-binding.
     *
     * @param view [View]
     */
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

    fun edit() {
        insert(binding.text.text.toString())
    }

    /**
     * This function is called from data-binding.
     *
     * @param view [View]
     */
    fun shareUrl(view: View) {
        view.context.startActivity(IntentFactory.makeShareUrl(getCurrentText()))
    }

    /**
     * Switch visibility and content.
     *
     * @param title site's title
     * @param url URL
     * @return [Disposable]
     */
    fun switch(title: String?, url: String?): Disposable =
            if (url.isNullOrBlank() || !enable) {
                clearContent()
                hide()
            } else {
                setTitle(title)
                setLink(url)
                show()
            }

    /**
     * Show this module.
     */
    private fun show(): Disposable {
        if (binding.root.visibility == View.GONE && enable) {
            return runOnMainThread { binding.root.visibility = View.VISIBLE }
        }
        return Disposables.disposed()
    }

    /**
     * Hide this module.
     */
    private fun hide(): Disposable {
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

    private fun setTitle(title: String?) {
        binding.title.text = title
    }

    /**
     * Set open link and icon.
     *
     * @param link Link URL(string)
     */
    private fun setLink(link: String) {
        binding.text.text = link
    }

    private fun clearContent() {
        binding.title.text = ""
        binding.text.text = ""
    }

    private fun runOnMainThread(action: () -> Unit) =
            Completable.fromAction { action() }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            Timber::e
                    )
}