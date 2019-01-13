package jp.toastkid.yobidashi.settings

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingsBinding

/**
 * Settings top fragment.
 *
 * @author toastkidjp
 */
class SettingsTopFragment : BaseFragment() {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        fragmentManager?.let {
            binding.container.adapter = PagerAdapter(it)
            binding.container.offscreenPageLimit = 3
        }
        return binding.root
    }

    override fun titleId(): Int = R.string.title_settings

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_settings
    }
}
