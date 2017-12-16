package jp.toastkid.yobidashi.browser.tab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.squareup.moshi.Moshi
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.tab.model.WebTab
import jp.toastkid.yobidashi.databinding.ActivityTabHistoryBinding
import jp.toastkid.yobidashi.libs.Toaster

/**
 * WebTab history viewer.
 *
 * @author toastkidjp
 */
class TabHistoryActivity : BaseActivity() {

    /** Data binding object.  */
    private var binding: ActivityTabHistoryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabJson = intent.getStringExtra(EXTRA_KEY_TAB)

        val adapter = TabHistoryAdapter(this, JSON_ADAPTER.fromJson(tabJson) ?: WebTab()) { index ->
            val intent = Intent()
            intent.putExtra(EXTRA_KEY_INDEX, index)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        if (adapter.itemCount == 0) {
            finish()
            Toaster.tShort(this, R.string.message_empty_histories)
            return
        }

        binding = DataBindingUtil.setContentView<ActivityTabHistoryBinding>(this, LAYOUT_ID)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding?.recyclerView?.adapter = adapter
        binding?.root?.setOnClickListener { _ -> finish() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    public override fun titleId(): Int = R.string.title_tab_histories

    companion object {

        /**
         *  Layout ID.
         */
        private val LAYOUT_ID = R.layout.activity_tab_history

        /**
         * Use for deserialize tab object from JSON.
         */
        private val JSON_ADAPTER = Moshi.Builder().build().adapter(WebTab::class.java)

        /**
         * Key of extra tab.
         */
        private val EXTRA_KEY_TAB = "tab"

        /**
         * Key of extra tab history index.
         */
        val EXTRA_KEY_INDEX = "index"

        /**
         * Request code.
         */
        val REQUEST_CODE: Int = 24

        /**
         * Make launcher intent.
         *
         * @param context
         * @param tab
         *
         * @return
         */
        internal fun makeIntent(context: Context, tab: WebTab): Intent {
            val intent = Intent(context, TabHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(EXTRA_KEY_TAB, JSON_ADAPTER.toJson(tab))
            return intent
        }
    }
}
