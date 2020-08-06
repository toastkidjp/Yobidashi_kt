package jp.toastkid.yobidashi.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentAboutBinding
import jp.toastkid.yobidashi.libs.intent.IntentFactory

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
        preferenceApplier = PreferenceApplier(requireContext())

        binding?.settingsAppVersion?.text = BuildConfig.VERSION_NAME
        return binding?.root
    }

    /**
     * Show licenses dialog.
     * @param view
     */
    fun licenses(view: View) {
        val intent = Intent(requireContext(), OssLicensesMenuActivity::class.java)
        OssLicensesMenuActivity.setActivityTitle(view.context.getString(R.string.title_licenses))
        startActivity(intent)
    }

    fun checkUpdate() {
        startActivity(IntentFactory.googlePlay(BuildConfig.APPLICATION_ID))
    }

    fun privacyPolicy() {
        val browserViewModel =
                ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java)

        popBackStack()
        browserViewModel.open(getString(R.string.link_privacy_policy).toUri())
    }

    fun aboutAuthorApp() {
        startActivity(IntentFactory.authorsApp())
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

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_about

    }
}
