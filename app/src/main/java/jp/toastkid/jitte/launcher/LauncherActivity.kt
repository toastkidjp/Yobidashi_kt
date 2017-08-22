package jp.toastkid.jitte.launcher

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem

import jp.toastkid.jitte.BaseActivity
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ActivityLauncherBinding
import jp.toastkid.jitte.libs.Colors
import jp.toastkid.jitte.libs.ImageLoader
import jp.toastkid.jitte.libs.Inputs

/**
 * App Launcher.

 * @author toastkidjp
 */
class LauncherActivity : BaseActivity() {

    /** Binding object.  */
    private var binding: ActivityLauncherBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityLauncherBinding>(this, LAYOUT_ID)

        initToolbar(binding!!.toolbar)
        binding!!.toolbar.inflateMenu(R.menu.launcher)

        binding!!.appItemsView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val adapter = Adapter(this, binding!!.toolbar)
        binding!!.appItemsView.adapter = adapter
        binding!!.appItemsView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (!binding!!.filter.hasFocus()) {
                    return false
                }
                Inputs.hideKeyboard(binding!!.filter)
                binding!!.appItemsView.requestFocus()
                return false
            }
        }

        initInput(adapter)
    }

    private fun initInput(adapter: Adapter) {
        val editText = binding!!.filter
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // NOP.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
                // NOP.
            }
        })
    }

    override fun onResume() {
        super.onResume()
        applyColorToToolbar(binding!!.toolbar)
        val fontColor = colorPair().fontColor()
        Colors.setEditTextColor(binding!!.filter, fontColor)
        binding!!.inputBorder.setBackgroundColor(fontColor)
        ImageLoader.setImageToImageView(binding!!.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId

        val itemCount = binding!!.appItemsView.adapter.itemCount

        if (itemId == R.id.to_top) {
            if (itemCount > 30) {
                binding!!.appItemsView.scrollToPosition(0)
                return true
            }
            binding!!.appItemsView.smoothScrollToPosition(0)
            return true
        }

        if (itemId == R.id.to_bottom) {
            if (itemCount > 30) {
                binding!!.appItemsView.scrollToPosition(itemCount - 1)
                return true
            }
            binding!!.appItemsView.smoothScrollToPosition(itemCount)
            return true
        }

        return super.clickMenu(item)
    }

    override fun onPause() {
        super.onPause()
        Inputs.hideKeyboard(binding!!.filter)
    }

    override fun titleId(): Int {
        return R.string.title_apps_launcher
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_launcher

        /**
         * Make launcher intent.
         * @param context
         * *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, LauncherActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
