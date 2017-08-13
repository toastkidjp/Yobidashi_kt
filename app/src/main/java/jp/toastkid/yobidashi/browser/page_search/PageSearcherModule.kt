package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import jp.toastkid.yobidashi.browser.tab.TabAdapter
import jp.toastkid.yobidashi.browser.tab.TabList
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Module for find in page.

 * @author toastkidjp
 */
class PageSearcherModule
/**
 * Initialize with parent.

 * @param binding
 */
(
        binding: ModuleSearcherBinding,
        tabs: TabAdapter
) : BaseModule(binding.root) {

    /** Use for open software keyboard.  */
    private val editText: EditText

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        val colorPair = PreferenceApplier(binding.inputLayout.context).colorPair()
        binding.close.setColorFilter(colorPair.fontColor())
        binding.sipUpward.setColorFilter(colorPair.fontColor())
        binding.sipDownward.setColorFilter(colorPair.fontColor())

        binding.inputLayout.editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // NOP.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                tabs.find(s.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        editText = binding.inputLayout.editText
        binding.close.setOnClickListener { v -> hide() }
        binding.sipUpward.setOnClickListener { v ->
            tabs.findUp()
            Inputs.hideKeyboard(editText)
        }
        binding.sipDownward.setOnClickListener { v ->
            tabs.findDown()
            Inputs.hideKeyboard(editText)
        }
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

    override fun hide() {
        super.hide()
        Inputs.hideKeyboard(editText)
    }
}
