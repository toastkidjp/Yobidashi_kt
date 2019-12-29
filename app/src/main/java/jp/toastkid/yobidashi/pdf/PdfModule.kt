package jp.toastkid.yobidashi.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModulePdfBinding
import jp.toastkid.yobidashi.databinding.ModulePdfHeaderBinding
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.HeaderViewModel
import timber.log.Timber

/**
 * PDF Module.
 *
 * @param context
 * @param parent
 *
 * @author toastkidjp
 */
class PdfModule(
        private val context: Context,
        private val parent: ViewGroup
) {

    private val layoutInflater = LayoutInflater.from(context)

    /**
     * Data binding object.
     */
    private val binding = DataBindingUtil.inflate<ModulePdfBinding>(
            layoutInflater,
            R.layout.module_pdf,
            parent,
            false
    )

    private val headerBinding = DataBindingUtil.inflate<ModulePdfHeaderBinding>(
            layoutInflater,
            R.layout.module_pdf_header,
            parent,
            false
    )

    /**
     * Adapter.
     */
    private val adapter = Adapter(LayoutInflater.from(context), context.contentResolver)

    /**
     * LayoutManager.
     */
    private val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    private val disposables = CompositeDisposable()

    private var headerViewModel: HeaderViewModel? = null

    init {
        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(binding.pdfImages)

        headerBinding.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = p0?.progress ?: 0
                headerBinding.input.setText((progress + 1).toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
        headerBinding.input.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(inputText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputText?.let {
                    val newIndex = try {
                        Integer.parseInt(it.toString()) - 1
                    } catch (e: NumberFormatException) {
                        -1
                    }

                    if (newIndex == -1) {
                        return@let
                    }

                    scrollTo(newIndex)
                }
            }

        })

        (context as? FragmentActivity)?.let {
            headerViewModel = ViewModelProviders.of(it).get(HeaderViewModel::class.java)
        }
    }

    /**
     * Apply color to views.
     *
     * @param colorPair
     */
    fun applyColor(colorPair: ColorPair) {
        headerBinding.header.setBackgroundColor(colorPair.bgColor())
        headerBinding.seek.progressDrawable.colorFilter =
                PorterDuffColorFilter(colorPair.fontColor(), PorterDuff.Mode.SRC_IN)
        EditTextColorSetter().invoke(headerBinding.input, colorPair.fontColor())
    }

    /**
     * Load PDF content from [Uri].
     *
     * @param uri
     */
    fun load(uri: Uri) {
        if (parent.childCount == 0) {
            parent.addView(binding.root)
        }
        adapter.load(uri)
        binding.pdfImages.scheduleLayoutAnimation()
        headerBinding.seek.max = adapter.itemCount - 1
    }

    /**
     * Scroll to specified position.
     *
     * @param position
     */
    fun scrollTo(position: Int) {
        layoutManager.scrollToPosition(getSafeIndex(position))
    }

    /**
     * Assign new thumbnail image.
     */
    fun makeThumbnail(): Bitmap? {
        binding.pdfImages.invalidate()
        binding.pdfImages.buildDrawingCache()
        return binding.pdfImages.drawingCache
    }

    /**
     * Get safe index.
     *
     * @param index
     */
    private fun getSafeIndex(index: Int): Int =
            if (index < 0 || adapter.itemCount < index) 0 else index

    /**
     * Return current item position.
     *
     * @return current item position.
     */
    fun currentItemPosition(): Int = layoutManager.findFirstVisibleItemPosition()

    /**
     * Animate root view.
     *
     * @param animation
     */
    fun animate(animation: Animation) {
        binding.root.startAnimation(animation)
    }

    /**
     * Move to first page.
     */
    internal fun pageUp() {
        RecyclerViewScroller.toTop(binding.pdfImages, adapter.itemCount)
    }

    /**
     * Move to last page.
     */
    internal fun pageDown() {
        RecyclerViewScroller.toBottom(binding.pdfImages, adapter.itemCount)
    }

    fun isVisible() = binding.root.isVisible

    fun show() {
        if (binding.root.visibility == View.GONE) {
            Completable.fromAction {
                binding.root.visibility = View.VISIBLE
            }.subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, Timber::e)
                    .addTo(disposables)
        }
        headerViewModel?.replace(headerBinding.root)
    }

    fun hide() {
        if (binding.root.visibility == View.VISIBLE) {
            Completable.fromAction {
                binding.root.visibility = View.GONE
            }.subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, Timber::e)
        }
    }

    fun dispose() {
        disposables.clear()
    }

}