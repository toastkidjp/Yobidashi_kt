package jp.toastkid.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.about.view.LicensesDialogFragment
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.intent.GooglePlayIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * About this app.
 *
 * @author toastkidjp
 */
class AboutThisAppFragment : Fragment(), ContentScrollable {

    private lateinit var preferenceApplier: PreferenceApplier

    private var scrollState: ScrollState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AboutThisAppUi()
            }
        }
    }

    @Composable
    fun AboutThisAppUi() {
        val scrollState = rememberScrollState()
        this.scrollState = scrollState

        Column(
            Modifier
                .background(color = Color(0xbbFFFFFF))
                .verticalScroll(scrollState)
        ) {
            Text(
                text = getString(R.string.message_about_this_app),
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp)
                    .clickable(onClick = { checkUpdate() })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_store_black),
                    contentDescription = getString(R.string.title_go_google_play)
                )
                Text(
                    text = getString(R.string.title_go_google_play),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp)
                    .clickable(onClick = { privacyPolicy() })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_privacy),
                    contentDescription = getString(R.string.privacy_policy)
                )
                Text(
                    text = getString(R.string.privacy_policy),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp)
                    .clickable(onClick = { licenses() })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_license_black),
                    contentDescription = getString(R.string.title_licenses)
                )
                Text(
                    text = getString(R.string.title_licenses),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = getString(R.string.title_app_version) + arguments?.getString("version_name"),
                    fontSize = 16.sp
                )
            }

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp)
                    .clickable(onClick = { aboutAuthorApp() })
            ) {
                Text(
                    text = getString(R.string.copyright),
                    fontSize = 16.sp
                )
            }
        }
    }

    @Composable
    private fun InsetDivider() {
        Divider(
            color = colorResource(R.color.gray_500_dd),
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(start = 16.dp, end = 16.dp)
        )
    }

    /**
     * Show licenses dialog.
     */
    private fun licenses() {
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
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollTo(0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollTo(scrollState?.maxValue ?: 0)
        }
    }

    companion object {

        fun makeWith(versionName: String): Fragment =
            AboutThisAppFragment().also {
                it.arguments = bundleOf("version_name" to versionName)
            }

    }
}
