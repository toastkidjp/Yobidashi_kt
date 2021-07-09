package jp.toastkid.yobidashi.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.licence.LicensesHtmlLoader
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import jp.toastkid.yobidashi.databinding.FragmentAboutBinding
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets

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
        binding?.settingsAppVersion?.text = BuildConfig.VERSION_NAME

        val context = context ?: return binding?.root
        preferenceApplier = PreferenceApplier(context)

        return binding?.root
    }

    /**
     * Show licenses dialog.
     * @param view
     */
    fun licenses(view: View) {
        binding?.licenseContent?.let {
            it.isVisible = !it.isVisible
            if (it.isGone || it.childCount != 0) {
                it.get(0).scrollTo(0, 0)
                return@let
            }

            val readUtf8 =
                LicensesHtmlLoader(view.context.assets).invoke().source().use { source ->
                    source.buffer().readUtf8()
                }
            val webView = WebViewFactory().make(it.context)
            it.addView(webView)
            webView.loadDataWithBaseURL(
                null,
                readUtf8,
                "text/html",
                StandardCharsets.UTF_8.name(),
                null
            )
        }
    }

    fun checkUpdate() {
        startActivity(IntentFactory.googlePlay(BuildConfig.APPLICATION_ID))
    }

    fun privacyPolicy() {
        val browserViewModel =
                activity?.let { ViewModelProvider(it).get(BrowserViewModel::class.java) } ?: return

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
