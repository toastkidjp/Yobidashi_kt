package jp.toastkid.yobidashi.launcher

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentLauncherBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.ContentScrollable

/**
 * App Launcher.
 *
 * @author toastkidjp
 */
class LauncherFragment : Fragment(), ContentScrollable {

    /**
     * Binding object.
     */
    private lateinit var binding: FragmentLauncherBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: Adapter

    private lateinit var preferenceApplier: PreferenceApplier

    private var prev = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.appItemsView.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        adapter = Adapter(context, binding.root)
        binding.appItemsView.adapter = adapter
        binding.appItemsView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                binding.appItemsView.requestFocus()
                return false
            }
        }

        activity?.also {
            val viewModel = ViewModelProviders.of(it).get(PageSearcherViewModel::class.java)
            viewModel.find.observe(it, Observer {
                if (TextUtils.equals(prev, it)) {
                    return@Observer
                }
                prev = it.toString()
                adapter.filter(prev)
                binding.appItemsView.scheduleLayoutAnimation()
            })
        }

        return binding.root
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.appItemsView, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.appItemsView, adapter.itemCount)
    }

    override fun onDetach() {
        adapter.dispose()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_launcher

    }
}
