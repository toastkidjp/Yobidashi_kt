package jp.toastkid.yobidashi.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentHomeBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.ToolbarAction
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import timber.log.Timber

/**
 * Home fragment.
 *
 * @author toastkidjp
 */
class HomeFragment : Fragment(), CommonFragmentAction {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentHomeBinding

    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Callback.
     */
    private var action: FragmentReplaceAction? = null

    /**
     * For hiding toolbar.
     */
    private var toolbarAction: ToolbarAction? = null

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        action = context as FragmentReplaceAction?
        toolbarAction = context as ToolbarAction?
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this

        context?.let {
            preferenceApplier = PreferenceApplier(it)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.root.setBackgroundColor(
                if (preferenceApplier.hasBackgroundImagePath())
                    Color.TRANSPARENT
                else
                    activity?.let { ContextCompat.getColor(it, R.color.darkgray_scale) } ?: Color.TRANSPARENT
        )

        val colorPair = preferenceApplier.colorPair()
        @ColorInt val fontColor: Int = colorPair.fontColor()

        binding.mainTitle.setTextColor(colorPair.fontColor())
        binding.searchAction.setTextColor(fontColor)

        @ColorInt val bgColor:   Int = colorPair.bgColor()
        binding.searchInput.setTextColor(bgColor)
        binding.searchActionBackground.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
        binding.searchIcon.setColorFilter(bgColor)
        binding.voiceSearch.setColorFilter(bgColor)
        binding.searchInputBorder.setBackgroundColor(bgColor)

        toolbarAction?.hideToolbar()
    }

    /**
     * Open search.
     */
    fun search() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.searchBar.transitionName = "share"
        }

        action?.action(Command.OPEN_SEARCH)
    }

    /**
     * Open voice search.
     */
    fun voiceSearch() {
        activity?.let {
            try {
                startActivityForResult(VoiceSearch.makeIntent(it), VoiceSearch.REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
                VoiceSearch.suggestInstallGoogleApp(binding.root, preferenceApplier.colorPair())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        when (requestCode) {
            VoiceSearch.REQUEST_CODE -> {
                activity?.let {
                    VoiceSearch.processResult(it, data).addTo(disposables)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    @StringRes
    override fun titleId(): Int = R.string.title_home

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_home

    }
}
