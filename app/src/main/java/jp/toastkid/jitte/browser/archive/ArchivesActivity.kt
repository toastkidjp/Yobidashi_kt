package jp.toastkid.jitte.browser.archive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import io.reactivex.functions.Consumer

import jp.toastkid.jitte.BaseActivity
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ActivityArchivesBinding
import jp.toastkid.jitte.libs.Toaster

/**
 * @author toastkidjp
 */
class ArchivesActivity : BaseActivity() {

    private var binding: ActivityArchivesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityArchivesBinding>(this, LAYOUT_ID)
        binding!!.archivesView.layoutManager = LinearLayoutManager(this)
        val adapter = Adapter(
                this,
                Consumer<String> { filePath ->
                    val intent = Intent()
                    intent.putExtra("FILE_NAME", filePath)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
        )
        if (adapter.itemCount == 0) {
            finish()
            Toaster.tShort(this, R.string.message_empty_archives)
        }
        binding!!.archivesView.adapter = adapter
    }

    override fun titleId(): Int {
        return R.string.title_archives
    }

    companion object {

        private val LAYOUT_ID = R.layout.activity_archives

        /**
         * Make launcher intent.
         * @param context
         * *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, ArchivesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
