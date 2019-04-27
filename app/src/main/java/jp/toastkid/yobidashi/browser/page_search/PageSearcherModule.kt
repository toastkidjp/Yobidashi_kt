package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.TabAdapter

/**
 * Module for find in page.
 *
 * @param binding [ModuleSearcherBinding]
 * @param tabs [TabAdapter]
 * @author toastkidjp
 */
class PageSearcherModule(
        binding: ModuleSearcherBinding,
        private val tabs: TabAdapter
) : BaseModule(binding.root) {

    private val height = context().resources.getDimension(R.dimen.toolbar_height)

    /**
     * Use for open software keyboard.
     */
    private val editText: EditText

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        binding.module = this

        val colorPair = PreferenceApplier(binding.inputLayout.context).colorPair()
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

    /**
     * Show module with opening software keyboard.
     *
     * @param activity [Activity]
     */
    fun show(activity: Activity) {
        moduleView.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { super.show() }
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
    override fun hide() {
        moduleView.animate()?.let {
            it.cancel()
            it.translationY(-height)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { Inputs.hideKeyboard(editText) }
                    .withEndAction { super.hide() }
                    .start()
        }
    }

    companion object {
        private const val ANIMATION_DURATION = 250L
    }
}
