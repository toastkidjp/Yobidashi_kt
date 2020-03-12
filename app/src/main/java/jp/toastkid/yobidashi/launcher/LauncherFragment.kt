package jp.toastkid.yobidashi.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentLauncherBinding
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Inputs
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
    private val adapter by lazy {
        Adapter(requireContext(), binding.toolbar)
    }

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.appItemsView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.appItemsView.adapter = adapter
        binding.appItemsView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (!binding.filter.hasFocus()) {
                    return false
                }
                Inputs.hideKeyboard(binding.filter)
                binding.appItemsView.requestFocus()
                return false
            }
        }
        initInput(adapter)

        return binding.root
    }

    /**
     * Initialize input.
     *
     * @param adapter [Adapter]
     */
    private fun initInput(adapter: Adapter) {
        binding.filter.addTextChangedListener(object : TextWatcher {
            var prev: String = ""
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (TextUtils.equals(prev, s)) {
                    return
                }
                prev = s.toString()
                adapter.filter(s.toString())
                binding.appItemsView.scheduleLayoutAnimation()
            }

            override fun afterTextChanged(s: Editable) = Unit
        })
    }

    override fun onResume() {
        super.onResume()
        val colorPair = preferenceApplier.colorPair()
        val fontColor = colorPair.fontColor()
        EditTextColorSetter().invoke(binding.filter, fontColor)
        binding.inputBorder.setBackgroundColor(fontColor)
        ImageLoader.setImageToImageView(binding.background, preferenceApplier.backgroundImagePath)
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.appItemsView, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.appItemsView, adapter.itemCount)
    }

    override fun onPause() {
        super.onPause()
        Inputs.hideKeyboard(binding.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_launcher

        /**
         * Make launcher intent.
         * TODO Delete it
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, LauncherFragment::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
