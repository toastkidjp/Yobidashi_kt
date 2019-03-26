package jp.toastkid.yobidashi.browser.archive

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityArchivesBinding
import jp.toastkid.yobidashi.libs.Toaster

/**
 * Activity of archives.
 *
 * @author toastkidjp
 */
class ArchivesActivity : BaseActivity() {

    private var binding: ActivityArchivesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityArchivesBinding>(this, LAYOUT_ID)
        binding?.archivesView?.layoutManager = LinearLayoutManager(this)
        val adapter = Adapter(
                this,
                { filePath ->
                    val intent = Intent()
                    intent.putExtra(EXTRA_KEY_FILE_NAME, filePath)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
        )
        if (adapter.itemCount == 0) {
            finish()
            Toaster.tShort(this, R.string.message_empty_archives)
        }
        binding?.archivesView?.adapter = adapter
    }

    @StringRes override fun titleId(): Int = R.string.title_archives

    companion object {

        const val REQUEST_CODE = 0x0040

        private const val LAYOUT_ID = R.layout.activity_archives

        val EXTRA_KEY_FILE_NAME = "FILE_NAME"

        /**
         * Make launcher intent.
         * @param context
         *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, ArchivesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
