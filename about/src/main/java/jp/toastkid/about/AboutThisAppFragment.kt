package jp.toastkid.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.about.databinding.FragmentAboutBinding
import jp.toastkid.about.view.LicensesDialogFragment
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.intent.GooglePlayIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * About this app.
 *
 * @author toastkidjp
 */
class AboutThisAppFragment : Fragment(), ContentScrollable {

    /**
     * Data Binding.
     */
    private var binding: FragmentAboutBinding? = null

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding?.fragment = this
        binding?.settingsAppVersion?.text = arguments?.getString("version_name")

        val context = context ?: return binding?.root
        preferenceApplier = PreferenceApplier(context)

        return binding?.root
    }

    /**
     * Show licenses dialog.
     */
    fun licenses() {
        LicensesDialogFragment()
            .show(parentFragmentManager, LicensesDialogFragment::class.java.canonicalName)
    }

    fun checkUpdate() {
        val packageName = context?.applicationContext?.packageName ?: return
        startActivity(GooglePlayIntentFactory()(packageName))
    }

    fun privacyPolicy() {
        val browserViewModel =
            activity?.let { ViewModelProvider(it).get(BrowserViewModel::class.java) } ?: return

        popBackStack()
        browserViewModel.open(getString(R.string.link_privacy_policy).toUri())
    }

    fun aboutAuthorApp() {
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .also { it.data = Uri.parse("market://search?q=pub:toastkidjp") }
        )
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun toTop() {
        binding?.aboutScroll?.smoothScrollTo(0, 0)
    }

    override fun toBottom() {
        binding?.aboutScroll?.smoothScrollTo(0, binding?.root?.measuredHeight ?: 0)
    }

    override fun onDetach() {
        binding = null
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_about

        fun makeWith(versionName: String): Fragment =
            AboutThisAppFragment().also {
                it.arguments = bundleOf("version_name" to versionName)
            }

    }
}
