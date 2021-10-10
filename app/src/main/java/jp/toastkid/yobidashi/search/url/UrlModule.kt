/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.url

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchUrlBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class UrlModule
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val preferenceApplier = PreferenceApplier(context)

    private var binding: ModuleSearchUrlBinding? = null

    private var insertAction: ((String) -> Unit)? = null

    var enable: Boolean = true

    init {
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(inflater, R.layout.module_search_url, this, true)
        binding?.module = this
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

    fun setInsertAction(action: (String) -> Unit) {
        insertAction = action
    }

    fun edit() {
        insertAction?.invoke(binding?.text?.text.toString())
    }

    /**
     * This function is called from data-binding.
     *TODO delete useless argument
     * @param view [View]
     */
    fun shareUrl(view: View) {
        view.context.startActivity(makeShareUrl(getCurrentText()))
    }

    /**
     * Make sharing URL intent.
     *
     * @param url URL
     */
    private fun makeShareUrl(url: String): Intent {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        share.putExtra(Intent.EXTRA_SUBJECT, "Share link")
        share.putExtra(Intent.EXTRA_TEXT, url)
        return Intent.createChooser(share, "Share link $url")
    }

    /**
     * Switch visibility and content.
     *
     * @param title site's title
     * @param url URL
     */
    fun switch(title: String?, url: String?) =
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
    private fun show() {
        if (this.visibility == View.GONE && enable) {
            runOnMainThread { this.visibility = View.VISIBLE }
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (this.visibility == View.VISIBLE) {
            runOnMainThread { this.visibility = View.GONE }
        }
    }

    /**
     * Is visible this module visible.
     */
    fun isVisible() = this.visibility == View.VISIBLE

    fun onResume() {
        val color = IconColorFinder.from(this).invoke()
        binding?.clip?.setColorFilter(color)
        binding?.share?.setColorFilter(color)
        binding?.edit?.setColorFilter(color)
    }

    private fun getCurrentText() = binding?.text?.text.toString()

    private fun setTitle(title: String?) {
        binding?.title?.text = title
    }

    /**
     * Set open link and icon.
     *
     * @param link Link URL(string)
     */
    private fun setLink(link: String) {
        binding?.text?.text = link
    }

    private fun clearContent() {
        binding?.title?.text = ""
        binding?.text?.text = ""
    }

    private fun runOnMainThread(action: () -> Unit) =
            CoroutineScope(Dispatchers.Main).launch { action() }

}