package jp.toastkid.yobidashi.browser.page_search

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.TabAdapter
import timber.log.Timber

/**
 * Module for find in page.
 *
 * @param binding [ModuleSearcherBinding]
 * @param tabs [TabAdapter]
 * @author toastkidjp
 */
class PageSearcherModule(
        private val binding: ModuleSearcherBinding,
        private val tabs: TabAdapter
) {

    private val context = binding.root.context

    private val height = context.resources.getDimension(R.dimen.toolbar_height)

    /**
     * Use for open software keyboard.
     */
    private val editText: EditText

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        binding.module = this

        val colorPair = PreferenceApplier(context).colorPair()
        val bgColor = colorPair.bgColor()
        binding.close.setColorFilter(bgColor)
        binding.sipClear.setColorFilter(bgColor)
        binding.sipUpward.setColorFilter(bgColor)
        binding.sipDownward.setColorFilter(bgColor)

        binding.inputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                tabs.find(s.toString())
            }

            override fun afterTextChanged(s: Editable) = Unit
        })
        editText = binding.inputLayout.editText as EditText
        hide()
    }

    /**
     * Implement for Data Binding.
     */
    fun findUp() {
        tabs.findUp(editText.text.toString())
        Inputs.hideKeyboard(editText)
    }

    /**
     * Implement for Data Binding.
     */
    fun findDown() {
        tabs.findDown(editText.text.toString())
        Inputs.hideKeyboard(editText)
    }

    /**
     * Implement for Data Binding.
     */
    fun clearInput() {
        editText.setText("")
    }

    fun isVisible() = binding.root.isVisible

    /**
     * Show module with opening software keyboard.
     *
     * @param activity [Activity]
     */
    fun show(activity: Activity) {
        binding.root.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { switchVisibility(View.GONE, View.VISIBLE) }
                    .withEndAction {
                        editText.requestFocus()
                        Inputs.toggle(activity)
                    }
                    .start()
        }
    }

    /**
     * Hide module.
     */
    fun hide() {
        binding.root.animate()?.let {
            it.cancel()
            it.translationY(-height)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { Inputs.hideKeyboard(editText) }
                    .withEndAction { switchVisibility(View.VISIBLE, View.GONE) }
                    .start()
        }
    }

    @SuppressLint("CheckResult")
    private fun switchVisibility(from: Int, to: Int) {
        Maybe.fromCallable { binding.root.visibility == from }
                .subscribeOn(Schedulers.computation())
                .filter { visible -> visible }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { binding.root.visibility = to },
                        Timber::e
                )
    }

    companion object {
        private const val ANIMATION_DURATION = 250L
    }
}
