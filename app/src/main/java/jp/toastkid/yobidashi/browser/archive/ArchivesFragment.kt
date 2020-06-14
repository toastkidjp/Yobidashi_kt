package jp.toastkid.yobidashi.browser.archive

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.databinding.FragmentArchivesBinding
import jp.toastkid.yobidashi.libs.Toaster
import java.io.File

/**
 * List fragment of archives.
 *
 * @author toastkidjp
 */
class ArchivesFragment : Fragment() {

    private var binding: FragmentArchivesBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding?.archivesView?.layoutManager = LinearLayoutManager(requireContext())
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val browserViewModel =
                ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java)

        val adapter = Adapter(view.context) { filePath ->
            popBackStack()
            browserViewModel.open(Uri.fromFile(File(filePath)))
        }

        if (adapter.itemCount == 0) {
            popBackStack()
            Toaster.tShort(view.context, R.string.message_empty_archives)
        }

        binding?.archivesView?.adapter = adapter
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_archives

    }
}
