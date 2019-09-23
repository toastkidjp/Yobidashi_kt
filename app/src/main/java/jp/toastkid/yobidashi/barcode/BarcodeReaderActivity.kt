package jp.toastkid.yobidashi.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.toastkid.yobidashi.R

/**
 * Barcode reader activity.
 *
 * @author toastkidjp
 */
class BarcodeReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_reader)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
                R.id.content,
                BarcodeReaderFragment(),
                BarcodeReaderFragment::class.java.simpleName
        )
        transaction.commitAllowingStateLoss()
    }

    companion object {

        /**
         * Make this activity's intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BarcodeReaderActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
