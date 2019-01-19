package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.TabAdapter

/**
 * Module for find in page.
 *
 * @param binding
 * @author toastkidjp
 */
class PageSearcherModule(
        binding: ModuleSearcherBinding,
        val tabs: TabAdapter
) : BaseModule(binding.root) {

    /** Use for open software keyboard.  */
    private val editText: EditText

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        binding.setModule(this)

        val colorPair = PreferenceApplier(binding.inputLayout.context).colorPair()
        val bgColor = colorPair.bgColor()
        binding.close.setColorFilter(bgColor)
        binding.sipClear.setColorFilter(bgColor)
        binding.sipUpward.setColorFilter(bgColor)
        binding.sipDownward.setColorFilter(bgColor)

        binding.inputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // NOP.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                tabs.find(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
                // NOP.
            }
        })
        editText = binding.inputLayout.editText as EditText
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
     * @param activity
     */
    fun show(activity: Activity) {
        show()
        editText.requestFocus()
        Inputs.toggle(activity)
    }

    /**
     * Hide module.
     */
    override fun hide() {
        super.hide()
        Inputs.hideKeyboard(editText)
    }
}
