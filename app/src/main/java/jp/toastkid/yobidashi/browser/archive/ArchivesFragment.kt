package jp.toastkid.yobidashi.browser.archive

import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.ui.list.SwipeToDismissItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import timber.log.Timber
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

/**
 * List fragment of archives.
 *
 * @author toastkidjp
 */
class ArchivesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = activity ?: return super.onCreateView(inflater, container, savedInstanceState)
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            ArchivesUi()
        }
        return composeView
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ArchivesUi() {
        val makeNew = Archive.makeNew(LocalContext.current)
        val items = remember { makeNew.listFiles() }

        if (items.isEmpty()) {
            popBackStack()
            Toaster.tShort(LocalContext.current, R.string.message_empty_archives)
            return
        }

        val browserViewModel =
            activity?.let { ViewModelProvider(it).get(BrowserViewModel::class.java) } ?: return

        val preferenceApplier = PreferenceApplier(LocalContext.current)

        MaterialTheme {
            LazyColumn(
                modifier = Modifier.nestedScroll(rememberViewInteropNestedScrollConnection())
            ) {
                items(items) { archiveFile ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                try {
                                    archiveFile.delete()
                                } catch (e: IOException) {
                                    Timber.e(e)
                                }
                            }
                            true
                        }
                    )
                    SwipeToDismissItem(
                        dismissState = dismissState,
                        dismissContent = {
                            Row(
                                Modifier
                                    .padding(start = 16.dp, end = 16.dp)
                                    .clickable {
                                        popBackStack()
                                        browserViewModel.open(Uri.fromFile(archiveFile))
                                    }) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_archive),
                                    contentDescription = stringResource(id = R.string.image),
                                    colorFilter = ColorFilter.tint(
                                        Color(preferenceApplier.color),
                                        BlendMode.SrcIn
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .width(40.dp)
                                        .fillMaxHeight()
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(start = 8.dp, end = 8.dp)
                                ) {
                                    Text(text = archiveFile.name, maxLines = 1, fontSize = 16.sp)
                                    Text(
                                        text = "${toLastModifiedText(archiveFile.lastModified())} / ${
                                            toKiloBytes(
                                                archiveFile.length()
                                            )
                                        }[KB]", maxLines = 1, fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Convert milliseconds to text.
     *
     * @param lastModifiedMs milliseconds
     */
    private fun toLastModifiedText(lastModifiedMs: Long) =
        DateFormat.format("yyyyMMdd HH:mm:ss", lastModifiedMs)

    /**
     * Convert file byte length to KB text.
     *
     * @param length file byte length
     */
    private fun toKiloBytes(length: Long): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).format(length / 1024)

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

}
