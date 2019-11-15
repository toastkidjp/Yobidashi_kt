package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * Module for find in page.
 *
 * @author toastkidjp
 */
class PageSearcherModule(
        fragment: Fragment,
        private val binding: ModuleSearcherBinding,
        private val find: (String) -> Unit,
        private val findDown: (String) -> Unit,
        private val findUp: (String) -> Unit
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

    /**
     * Use for disposing subscriptions.
     */
    private val disposables = CompositeDisposable()

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        val viewModel = ViewModelProviders.of(fragment).get(PageSearcherViewModel::class.java)

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
                    find(p0.toString())
                }

            })
        }

        viewModel.upward.observe(fragment, Observer { keyword ->
            findUp(keyword ?: editText.text.toString())
        })

        viewModel.downward.observe(fragment, Observer { keyword ->
            findDown(keyword ?: editText.text.toString())
        })

        viewModel.clear.observe(fragment, Observer {
            editText.setText("")
        })

        viewModel.close.observe(fragment, Observer {
            binding.root.animate().let {
                it.cancel()
                it.translationY(-height)
                        .setDuration(ANIMATION_DURATION)
                        .withStartAction { Inputs.hideKeyboard(editText) }
                        .withEndAction { switchVisibility(View.VISIBLE, View.GONE) }
                        .start()
            }
        })

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
                        Inputs.showKeyboard(activity, editText)
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
        Maybe.fromCallable { binding.root.visibility == from }
                .subscribeOn(Schedulers.computation())
                .filter { visible -> visible }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { binding.root.visibility = to },
                        Timber::e
                )
                .addTo(disposables)
    }

    /**
     * Close subscriptions.
     */
    fun dispose() {
        disposables.clear()
    }

    companion object {

        /**
         * Animation duration (ms).
         */
        private const val ANIMATION_DURATION = 250L
    }
}
