package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Module for find in page.
 *
 * @author toastkidjp
 */
class PageSearcherModule(
        fragmentActivity: FragmentActivity,
        private val binding: ModuleSearcherBinding
) {

    /**
     * View context.
     */
    private val context = binding.root.context

    /**
     * This value is used by show/hide animation.
     */
    private val height = context.resources.getDimension(R.dimen.toolbar_height)

    /**
     * Use for open software keyboard.
     */
    private val editText: EditText

    private val channel = Channel<String>()

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        val viewModel = ViewModelProviders.of(fragmentActivity).get(PageSearcherViewModel::class.java)

        binding.viewModel = viewModel

        setBackgroundColor()

        editText = binding.inputLayout.editText as EditText

        editText.let { editText ->
            editText.setOnEditorActionListener { input, _, _ ->
                viewModel.findDown(input.text.toString())
                Inputs.hideKeyboard(editText)
                true
            }

            editText.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) = Unit

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    CoroutineScope(Dispatchers.Default).launch {
                        channel.send(p0.toString())
                    }
                }
            })

            CoroutineScope(Dispatchers.Default).launch {
                channel.receiveAsFlow()
                        .debounce(1000)
                        .collect {
                            withContext(Dispatchers.Main) {
                                viewModel.find(it)
                            }
                        }
            }
        }

        (context as? FragmentActivity)?.let { activity ->
            viewModel.clear.observe(activity, Observer {
                editText.setText("")
            })

            viewModel.close.observe(activity, Observer {
                binding.root.animate().let {
                    it.cancel()
                    it.translationY(-height)
                            .setDuration(ANIMATION_DURATION)
                            .withStartAction { Inputs.hideKeyboard(editText) }
                            .withEndAction { switchVisibility(View.VISIBLE, View.GONE) }
                            .start()
                }
            })
        }

        hide()
    }

    /**
     * Set background color to views.
     */
    private fun setBackgroundColor() {
        PreferenceApplier(context).colorPair().bgColor().also {
            binding.close.setColorFilter(it)
            binding.sipClear.setColorFilter(it)
            binding.sipUpward.setColorFilter(it)
            binding.sipDownward.setColorFilter(it)
        }
    }

    fun isVisible() = binding.root.isVisible

    fun switch() {
        if (isVisible()) {
            hide()
        } else {
            show()
        }
    }

    /**
     * Show module with opening software keyboard.
     *
     * @param activity [Activity]
     */
    fun show() {
        binding.root.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { switchVisibility(View.GONE, View.VISIBLE) }
                    .withEndAction {
                        editText.requestFocus()
                        (context as? Activity)?.also { activity ->
                            Inputs.showKeyboard(activity, editText)
                        }
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

    private fun switchVisibility(from: Int, to: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (binding.root.visibility == from) {
                binding.root.visibility = to
            }
        }
    }

    /**
     * Close subscriptions.
     */
    fun dispose() {
        channel.cancel()
    }

    companion object {

        /**
         * Animation duration (ms).
         */
        private const val ANIMATION_DURATION = 250L
    }
}
