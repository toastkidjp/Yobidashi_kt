package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.ViewStubProxy
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
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
 * TODO Clean up code.
 * @author toastkidjp
 */
class PageSearcherModule(private val viewStubProxy: ViewStubProxy) {

    private lateinit var binding: ModuleSearcherBinding

    /**
     * This value is used by show/hide animation.
     */
    private var height = 0f

    private val channel = Channel<String>()

    fun isVisible() = viewStubProxy.isInflated && viewStubProxy.root?.isVisible == true

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
     * @param activity [Activity] TODO remove it.
     */
    fun show() {
        if (!viewStubProxy.isInflated) {
            initialize()
        }
        viewStubProxy.root?.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { switchVisibility(View.GONE, View.VISIBLE) }
                    .withEndAction {
                        val editText = binding.inputLayout?.editText ?: return@withEndAction
                        editText.requestFocus()
                        (binding.root.context as? Activity)?.also { activity ->
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
        viewStubProxy.root?.animate()?.let {
            it.cancel()
            it.translationY(-height)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction {
                        clearInput()
                        Inputs.hideKeyboard(binding.inputLayout?.editText)
                    }
                    .withEndAction { switchVisibility(View.VISIBLE, View.GONE) }
                    .start()
        }
    }

    private fun switchVisibility(from: Int, to: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (viewStubProxy.root?.visibility == from) {
                viewStubProxy.root?.visibility = to
            }
        }
    }

    fun initialize() {
        val context = viewStubProxy.viewStub?.context ?: return

        viewStubProxy.viewStub?.inflate()

        binding = viewStubProxy.binding as? ModuleSearcherBinding ?: return

        val viewModel = (context as? FragmentActivity)?.let {
            ViewModelProviders.of(it).get(PageSearcherViewModel::class.java)
        }

        binding.viewModel = viewModel

        binding.inputLayout.editText?.also {
            it.setOnEditorActionListener { input, _, _ ->
                viewModel?.findDown(input.text.toString())
                Inputs.hideKeyboard(it)
                true
            }

            it.addTextChangedListener(object : TextWatcher {
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
                                viewModel?.find(it)
                            }
                        }
            }
        }

        (context as? FragmentActivity)?.let { activity ->
            viewModel?.clear?.observe(activity, Observer {
                clearInput()
            })

            viewModel?.close?.observe(activity, Observer { hide() })
        }

        if (height == 0f) {
            height = binding.root.context.resources.getDimension(R.dimen.toolbar_height)
        }

        setBackgroundColor(binding)
    }

    private fun clearInput() {
        binding.inputLayout?.editText?.setText("")
    }

    /**
     * Set background color to views.
     */
    private fun setBackgroundColor(binding: ModuleSearcherBinding) {
        PreferenceApplier(binding.root.context).colorPair().bgColor().also {
            binding.close.setColorFilter(it)
            binding.sipClear.setColorFilter(it)
            binding.sipUpward.setColorFilter(it)
            binding.sipDownward.setColorFilter(it)
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
